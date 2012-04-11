package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.ActionBar.OnNavigationListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.prasanna.android.stacknetwork.intent.TagFaqIntentService;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.FetchTagsAsyncTask;

public class QuestionsActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();

    private Intent tagFaqIntent;

    private QuestionsFragment currentFragment = null;

    public class FetchUserTagsCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<String>>
    {
	@Override
	public void notifyOnCompletion(ArrayList<String> result)
	{
	    tags.add(StringConstants.FRONT_PAGE);
	    tags.addAll(result);

	    initActionBarAndSpinner();
	}
    }

    public abstract class QuestionsFragment extends Fragment
    {
	public abstract void startIntentService();
    }

    public class FrontPageFragment extends QuestionsFragment
    {
	private int currentPage = 0;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);

	    currentPage = 0;

	    Object lastSavedObject = null;

	    if (savedInstanceState != null)
	    {
		lastSavedObject = savedInstanceState.getSerializable(StringConstants.QUESTIONS);
		tags = (ArrayList<String>) savedInstanceState.getSerializable(StringConstants.TAGS);
		Log.d(TAG, "Retrieving saved instance: " + lastSavedObject + ", tags" + tags);
	    }

	    registerReceiverAndStartService(lastSavedObject);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
	    Log.d(TAG, "onSaveInstanceState");

	    if (questions != null && questions.isEmpty() == false)
	    {
		Log.d(TAG, "Saving questions");
		outState.putSerializable(StringConstants.QUESTIONS, questions);
	    }

	    super.onSaveInstanceState(outState);
	}

	@Override
	public void startIntentService()
	{
	    questionsIntent = new Intent(getApplicationContext(), UserQuestionsIntentService.class);
	    questionsIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
	    questionsIntent.putExtra(StringConstants.PAGE, ++currentPage);
	    startService(questionsIntent);
	    serviceRunning = true;
	}
    }

    public class TagFaqFragment extends QuestionsFragment
    {
	private int currentPage = 0;

	private final String tag;

	private ProgressDialog loadingQuestionsDialog;

	private BroadcastReceiver tagFaqReceiver = new BroadcastReceiver()
	{
	    @SuppressWarnings("unchecked")
	    @Override
	    public void onReceive(Context context, Intent intent)
	    {
		questions.addAll((ArrayList<Question>) intent.getSerializableExtra(QuestionIntentAction.TAGS_FAQ
		                .getExtra()));

		Log.d(TAG, "Received tag faq: " + questions.size());

		if (loadingQuestionsDialog != null)
		{
		    loadingQuestionsDialog.dismiss();
		    loadingQuestionsDialog = null;
		}
		processQuestions();
	    }
	};

	public TagFaqFragment(String tag)
	{
	    this.tag = tag;
	    currentPage = 0;
	    questions.clear();
	    lastDisplayQuestionIndex = 0;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    if (tag == null)
	    {
		finish();
	    }

	    // Object lastSavedObject = null;
	    // if (savedInstanceState != null)
	    // {
	    // lastSavedObject =
	    // savedInstanceState.getSerializable(StringConstants.QUESTIONS);
	    // }

	    registerTagFaqReceiver();

	    startIntentService();

	    // loadIfLastInstanceWasSaved(null);
	}

	@Override
	public void onDestroy()
	{
	    super.onDestroy();
	    stopTagFaqServiceAndUnregisterReceiver();
	}

	@Override
	public void onStop()
	{
	    super.onStop();

	    stopTagFaqServiceAndUnregisterReceiver();
	}

	private void stopTagFaqServiceAndUnregisterReceiver()
	{
	    if (tagFaqIntent != null)
	    {
		stopService(tagFaqIntent);
	    }

	    try
	    {
		unregisterReceiver(tagFaqReceiver);
	    }
	    catch (IllegalArgumentException e)
	    {
		Log.d(getLogTag(), e.getMessage());
	    }
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
	    if (questions != null && questions.isEmpty() == false)
	    {
		outState.putSerializable(StringConstants.QUESTIONS, questions);
	    }

	    super.onSaveInstanceState(outState);
	}

	@Override
	public void startIntentService()
	{
	    loadingQuestionsDialog = ProgressDialog.show(getCurrentAppContext(), "", getString(R.string.loading));
	    tagFaqIntent = new Intent(getApplicationContext(), TagFaqIntentService.class);
	    tagFaqIntent.setAction(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
	    tagFaqIntent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), tag);
	    tagFaqIntent.putExtra(StringConstants.PAGE, ++currentPage);
	    startService(tagFaqIntent);
	    serviceRunning = true;
	}

	protected void registerTagFaqReceiver()
	{
	    IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
	    filter.addCategory(Intent.CATEGORY_DEFAULT);
	    registerReceiver(tagFaqReceiver, filter);
	}
    }

    protected void initActionBarAndSpinner()
    {
	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

	OnNavigationListener callback = new OnNavigationListener()
	{
	    @Override
	    public boolean onNavigationItemSelected(int itemPosition, long itemId)
	    {
		if (itemPosition >= 0 && itemPosition < tags.size())
		{
		    openTagFragment(itemPosition);
		    return true;
		}

		return false;
	    }
	};

	spinnerAdapter = new ArrayAdapter<String>(this, R.layout.action_bar_spinner, R.id.spinnertAdapterItem, tags);
	getActionBar().setListNavigationCallbacks(spinnerAdapter, callback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	if (tags == null || tags.isEmpty())
	{
	    FetchTagsAsyncTask fetchUserAsyncTask = new FetchTagsAsyncTask(new FetchUserTagsCompletionNotifier());
	    fetchUserAsyncTask.execute(1);
	}
    }

    private void registerReceiverAndStartService(Object lastSavedObject)
    {
	registerQuestionsReceiver();

	loadIfLastInstanceWasSaved(lastSavedObject);
    }

    @Override
    protected void startQuestionsService()
    {
	currentFragment.startIntentService();
    }

    @Override
    protected void registerQuestionsReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(receiver, filter);
    }

    @Override
    public void refresh()
    {
	stopServiceAndUnregisterReceiver();
	questionsLinearLayout.removeAllViews();
	registerReceiverAndStartService(null);
    }

    @Override
    public Context getCurrentAppContext()
    {
	return QuestionsActivity.this;
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    @Override
    protected QuestionIntentAction getReceiverIntentAction()
    {
	return QuestionIntentAction.QUESTIONS;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
	outState.putSerializable(StringConstants.TAGS, tags);

	super.onSaveInstanceState(outState);
    }

    protected void openTagFragment(int itemPosition)
    {
	String fragmentTag = null;

	if (itemPosition == 0)
	{
	    currentFragment = new FrontPageFragment();
	    fragmentTag = "Home";
	}
	else
	{
	    currentFragment = new TagFaqFragment(tags.get(itemPosition));
	    fragmentTag = tags.get(itemPosition);
	}

	if (currentFragment != null)
	{
	    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
	    /*
	     * Without removing existing views, new fragment's questions gets
	     * appended to existing fragment's question even after calling
	     * replace on fragment transaction. R.id.questionsDisplay is the
	     * fragment container id. Why?
	     */
	    questionsLinearLayout.removeAllViews();
	    fragmentTransaction.replace(R.id.questionsDisplay, currentFragment, fragmentTag);
	    fragmentTransaction.commit();
	}
    }
}

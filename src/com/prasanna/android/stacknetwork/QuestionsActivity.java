package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.FetchTagsAsyncTask;

public class QuestionsActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();

    private ArrayList<String> tags = new ArrayList<String>();

    private ArrayAdapter<String> spinnerAdapter;

    public class FetchUserTagsCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<String>>
    {
	@Override
	public void notifyOnCompletion(ArrayList<String> result)
	{
	    tags.add(StringConstants.FRONT_PAGE);
	    tags.addAll(result);

	    initActionBarAndSpinner(tags);
	}
    }

    private void initActionBarAndSpinner(ArrayList<String> tags)
    {
	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

	OnNavigationListener callback = new OnNavigationListener()
	{
	    @Override
	    public boolean onNavigationItemSelected(int itemPosition, long itemId)
	    {
		if (itemPosition > 0)
		{
		    String tag = spinnerAdapter.getItem(itemPosition);
		    Intent tagFaqIntent = new Intent(getApplicationContext(), TagFaqActivity.class);
		    tagFaqIntent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), tag);
		    startActivity(tagFaqIntent);
		    return true;
		}
		return false;
	    }
	};

	spinnerAdapter = new ArrayAdapter<String>(this, R.layout.action_bar_spinner, R.id.spinnertAdapterItem, tags);
	getActionBar().setListNavigationCallbacks(spinnerAdapter, callback);
    }

    @Override
    protected void onStart()
    {
	super.onStart();

	if (isAuthenticatedRealm() == true)
	{
	    FetchTagsAsyncTask fetchUserAsyncTask = new FetchTagsAsyncTask(
		            new FetchUserTagsCompletionNotifier());
	    fetchUserAsyncTask.execute(1);
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	Object lastSavedObject = null;
	if (savedInstanceState != null)
	{
	    lastSavedObject = savedInstanceState.getSerializable(StringConstants.QUESTIONS);
	}

	registerReceiverAndStartService(lastSavedObject);
    }

    private void registerReceiverAndStartService(Object lastSavedObject)
    {
	registerQuestionsReceiver();

	loadIfLastInstanceWasSaved(lastSavedObject);
    }

    @Override
    protected void startQuestionsService()
    {
	questionsIntent = new Intent(this, UserQuestionsIntentService.class);
	questionsIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
	questionsIntent.putExtra(StringConstants.PAGE, ++page);
	startService(questionsIntent);
	serviceRunning = true;
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
    protected QuestionIntentAction getIntentAction()
    {
	return QuestionIntentAction.QUESTIONS;
    }
}

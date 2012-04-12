package com.prasanna.android.stacknetwork;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.intent.SearchForQuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;

public class QuestionSearchResultsActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = QuestionSearchResultsActivity.class.getSimpleName();

    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	query = getIntent().getStringExtra(SearchManager.QUERY);

	Log.d(TAG, "started for query: " + query);

	questionsLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.items_fragment_container, null);
	scrollView.addView(questionsLinearLayout);

	registerQuestionsReceiver();

	fetchingQuestionsDialog = ProgressDialog.show(QuestionSearchResultsActivity.this, "",
	                getString(R.string.loading));

	startQuestionsService();
    }

    @Override
    public boolean onQueryTextSubmit(String paramString)
    {
	if (serviceRunning == false)
	{
	    query = paramString;
	    startQuestionsService();
	    return true;
	}

	return false;
    }

    @Override
    protected void processQuestions()
    {
	questionsLinearLayout.removeAllViews();

	super.processQuestions();
    }

    @Override
    public boolean onQueryTextChange(String paramString)
    {
	return false;
    }

    @Override
    protected void startQuestionsService()
    {
	questionsIntent = new Intent(this, SearchForQuestionsIntentService.class);
	questionsIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.name());
	questionsIntent.putExtra(SearchManager.QUERY, query);
	questionsIntent.putExtra(StringConstants.PAGE, ++page);
	startService(questionsIntent);
	serviceRunning = true;
    }

    @Override
    public void refresh()
    {
	if (serviceRunning == false && query != null)
	{
	    startQuestionsService();
	}
    }

    @Override
    public Context getCurrentContext()
    {
	return getApplicationContext();
    }

    @Override
    protected void registerQuestionsReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(receiver, filter);
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    @Override
    protected QuestionIntentAction getReceiverIntentAction()
    {
	return QuestionIntentAction.QUESTION_SEARCH;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
	outState.putSerializable(StringConstants.QUESTIONS, questions);
	super.onSaveInstanceState(outState);
    }

    @Override
    protected void onScrollToBottom()
    {
	if (serviceRunning == false)
	{
	    loadingProgressView = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress, null);
	    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT);
	    layoutParams.setMargins(0, 15, 0, 15);
	    questionsLinearLayout.addView(loadingProgressView, layoutParams);
	    startQuestionsService();
	}
    }

}

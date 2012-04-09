package com.prasanna.android.stacknetwork;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

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

	fetchingQuestionsDialog = ProgressDialog.show(QuestionSearchResultsActivity.this, "",
	                getString(R.string.loading));

	registerQuestionsReceiver();

	startQuestionsService();
    }

    @Override
    public boolean onQueryTextSubmit(String paramString)
    {
	if (serviceRunning == false)
	{
	    query = paramString;
	    startQuestionsService();
	}

	return false;
    }

    @Override
    protected void processQuestions()
    {
	Log.d(TAG, "Received search response " + questions);

	questionsLinearLayout.removeAllViews();

	super.processQuestions();
    }

    @Override
    public boolean onQueryTextChange(String paramString)
    {
	// TODO Auto-generated method stub
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
	// TODO Auto-generated method stub
    }

    @Override
    public Context getCurrentAppContext()
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
    protected QuestionIntentAction getIntentAction()
    {
	return QuestionIntentAction.QUESTION_SEARCH;
    }

}

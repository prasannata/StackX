package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import com.prasanna.android.stacknetwork.intent.SearchForQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class QuestionSearchResultsActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = QuestionSearchResultsActivity.class.getSimpleName();

    private int page = 0;

    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        query = getIntent().getStringExtra(SearchManager.QUERY);

        Log.d(TAG, "started for query: " + query);

        fetchingQuestionsDialog = ProgressDialog.show(QuestionSearchResultsActivity.this, "",
                getString(R.string.loading));

        registerQuestionSearchReceiver();

        startQuestionsService();
    }

    private BroadcastReceiver searchResultReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            questions.addAll((ArrayList<Question>) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.getExtra()));

            Log.d(TAG, "Received search response " + questions);

            questionsLinearLayout.removeAllViews();
            processQuestions();
        }
    };

    private void registerQuestionSearchReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(searchResultReceiver, filter);
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

}

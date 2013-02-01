package com.prasanna.android.stacknetwork.fragment;

import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;

public class SearchQuestionListFragment extends QuestionListFragment
{
    private static final String TAG = SearchQuestionListFragment.class.getSimpleName();
    private Intent intent;
    private SearchCriteria searchCriteria;

    public void search(SearchCriteria searchCriteria)
    {
        Log.d(TAG, "Running search criteria");
        if (searchCriteria != null)
        {
            itemListAdapter.clear();
            itemListAdapter.notifyDataSetChanged();
            this.searchCriteria = searchCriteria;
            startIntentService();
        }
    }

    private void prepareIntent()
    {
        if (intent == null)
        {
            intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.getAction());
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.SEARCH_ADVANCED);
            intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
            intent.putExtra(StringConstants.SEARCH_CRITERIA, searchCriteria);
        }
        else
            intent.putExtra(StringConstants.SEARCH_CRITERIA, searchCriteria.nextPage());
    }

    @Override
    protected void startIntentService()
    {
        if (isAdded())
        {
            prepareIntent();
            showProgressBar();
            startService(intent);
        }
    }

    @Override
    public void onStop()
    {
        Log.d(getLogTag(), "onStop");

        super.onStop();

        stopService(intent);
    }

    public boolean hasResults()
    {
        return itemListAdapter != null && itemListAdapter.getCount() > 0;
    }

}

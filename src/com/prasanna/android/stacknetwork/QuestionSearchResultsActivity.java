/*
    Copyright (C) 2012 Prasanna Thirumalai
    
    This file is part of StackX.

    StackX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    StackX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with StackX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prasanna.android.stacknetwork;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.intent.SearchForQuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

/* TODO: Replace this with fragment */
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

        registerQuestionsSearchReceiver();

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
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            questionsLinearLayout.removeAllViews();
            if (fetchingQuestionsDialog == null)
            {
                fetchingQuestionsDialog = ProgressDialog.show(QuestionSearchResultsActivity.this, "",
                        getString(R.string.loading));
            }
            startQuestionsService();
            return true;
        }

        return false;
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
    protected void registerQuestionsSearchReceiver()
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

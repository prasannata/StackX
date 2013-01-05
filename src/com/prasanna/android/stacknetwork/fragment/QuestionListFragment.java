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

package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionListFragment extends AbstractQuestionListFragment
{
    private static final String TAG = QuestionListFragment.class.getSimpleName();

    private Intent intent;
    private int currentPage = 0;
    private int action;
    private boolean created = false;
    private int selectedNavigationIndex;

    public static QuestionListFragment newFragment(int action)
    {
	QuestionListFragment newFragment = new QuestionListFragment();
	return newFragment.setQuestionAction(action);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	Log.d(TAG, "onCreateView");

	if (itemsContainer == null)
	{
	    itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, null);
	    itemListAdapter = new ItemListAdapter<Question>(getActivity(), R.layout.question_snippet_layout,
		            new ArrayList<Question>(), this);
	}

	return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	Log.d(TAG, "onActivityCreated");

	super.onActivityCreated(savedInstanceState);

	if (!created)
	{
	    switch (action)
	    {
		case QuestionsIntentService.GET_FRONT_PAGE:
		    getFrontPage();
		    break;
		case QuestionsIntentService.GET_FAQ_FOR_TAG:
		    getFaqsForTag();
		    break;
		case QuestionsIntentService.GET_RELATED:
		    getRelatedQuestions();
		    break;
		case QuestionsIntentService.SEARCH:
		    search(getActivity().getIntent().getStringExtra(SearchManager.QUERY));
		    break;
	    }

	    selectedNavigationIndex = getActivity().getActionBar().getSelectedNavigationIndex();
	    created = true;
	}
	else
	{
	    Log.d(TAG, "Fragment was already created. Restoring");

	    if (getActivity().getActionBar().getNavigationItemCount() > 0)
		getActivity().getActionBar().setSelectedNavigationItem(selectedNavigationIndex);
	}

    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
	Log.d(TAG, "Saving instance state");

	super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause()
    {
	Log.d(TAG, "onPause");

	super.onPause();
    }

    @Override
    public void onDestroy()
    {
	Log.d(getLogTag(), "onDestroy");

	super.onDestroy();

	stopService(intent);
    }

    @Override
    public void onStop()
    {
	Log.d(getLogTag(), "onStop");

	super.onStop();

	stopService(intent);
    }

    @Override
    protected void startIntentService()
    {
	intent.putExtra(StringConstants.PAGE, ++currentPage);
	intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
	startService(intent);
    }

    @Override
    public String getLogTag()
    {
	return TAG;
    }

    private void stopRunningServiceAndReceiver()
    {
	if (isServiceRunning())
	    getActivity().stopService(intent);
    }

    private void clean()
    {
	stopRunningServiceAndReceiver();

	itemListAdapter.clear();

	currentPage = 0;
    }

    private void getFrontPage()
    {
	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.getAction());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FRONT_PAGE);

	showProgressBar();

	startIntentService();
    }

    private void getRelatedQuestions()
    {
	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.getAction());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_RELATED);
	intent.putExtra(StringConstants.QUESTION_ID, getBundle().getLong(StringConstants.QUESTION_ID, 0));

	showProgressBar();

	startIntentService();
    }

    private void getFaqsForTag()
    {
	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.TAGS_FAQ.getAction());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FAQ_FOR_TAG);
	intent.putExtra(QuestionIntentAction.TAGS_FAQ.getAction(), getBundle().getString(StringConstants.TAG, null));

	showProgressBar();

	startIntentService();
    }

    public void search(String query)
    {
	clean();

	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTION_SEARCH.getAction());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.SEARCH);
	intent.putExtra(SearchManager.QUERY, query);

	showProgressBar();

	startIntentService();
    }

    @Override
    public void refresh()
    {
	clean();

	super.refresh();
    }

    public QuestionListFragment setQuestionAction(int action)
    {
	this.action = action;
	return this;
    }
}

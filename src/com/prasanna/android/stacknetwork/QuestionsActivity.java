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

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.prasanna.android.provider.RecentQueriesProvider;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment.OnGetQuestionsListener;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment.QuestionAction;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.FetchTagsAsyncTask;

public class QuestionsActivity extends AbstractUserActionBarActivity implements OnGetQuestionsListener
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();

    private ArrayList<String> tags = new ArrayList<String>();
    private ArrayAdapter<String> spinnerAdapter;

    public class FetchUserTagsCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<String>>
    {
	@Override
	public void notifyOnCompletion(ArrayList<String> result)
	{
	    if (result != null)
	    {
		tags.add(StringConstants.FRONT_PAGE);
		tags.addAll(result);

		initActionBarSpinner();
	    }
	}
    }

    private void initActionBarSpinner()
    {
	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

	OnNavigationListener callback = new OnNavigationListener()
	{
	    @Override
	    public boolean onNavigationItemSelected(int itemPosition, long itemId)
	    {
		if (itemPosition >= 0 && itemPosition < tags.size())
		{
		    if (itemPosition == 0)
			onGetQuestions(QuestionAction.FRONT_PAGE, null);
		    else
			onGetQuestions(QuestionAction.FAQS_TAG, tags.get(itemPosition));

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

	setContentView(R.layout.scroll_fragment);

	QuestionListFragment questionsFragment = (QuestionListFragment) getFragmentManager().findFragmentById(
	                R.id.questionsFragment);

	if (Intent.ACTION_SEARCH.equals(getIntent().getAction()))
	{
	    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RecentQueriesProvider.AUTHORITY,
		            RecentQueriesProvider.MODE);
	    suggestions.saveRecentQuery(getIntent().getStringExtra(SearchManager.QUERY), null);
	    questionsFragment.setSearch(true);
	}
	else if (getIntent().getBooleanExtra(StringConstants.RELATED, false))
	{
	    questionsFragment.setFetchRelatedQuestions(true);
	}
	else
	{
	    loadFrontPage(savedInstanceState);
	}
    }

    @SuppressWarnings("unchecked")
    private void loadFrontPage(Bundle savedInstanceState)
    {
	if (savedInstanceState != null)
	{
	    tags = (ArrayList<String>) savedInstanceState.getSerializable(StringConstants.TAGS);
	    int lastSavedPosition = savedInstanceState.getInt(StringConstants.ITEM_POSITION);
	    initActionBarSpinner();

	    if (lastSavedPosition > 0)
	    {
		getActionBar().setSelectedNavigationItem(lastSavedPosition);
	    }
	}
	else
	{
	    FetchTagsAsyncTask fetchUserAsyncTask = new FetchTagsAsyncTask(new FetchUserTagsCompletionNotifier(),
		            AppUtils.inRegisteredSite(getCacheDir()));
	    fetchUserAsyncTask.execute(1);
	}
    }

    @Override
    public void refresh()
    {
	QuestionListFragment questionsFragment = (QuestionListFragment) getFragmentManager().findFragmentById(
	                R.id.questionsFragment);
	questionsFragment.refresh();
    }

    @Override
    public Context getCurrentContext()
    {
	return this;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
	Log.d(TAG, "Saving activity instance");
	outState.putSerializable(StringConstants.TAGS, tags);

	super.onSaveInstanceState(outState);
    }

    @Override
    public void onGetQuestions(QuestionAction questionAction, String term)
    {
	QuestionListFragment questionsFragment = (QuestionListFragment) getFragmentManager().findFragmentById(
	                R.id.questionsFragment);

	switch (questionAction)
	{
	    case FRONT_PAGE:
		questionsFragment.getFrontPage();
		break;
	    case FAQS_TAG:
		questionsFragment.getFaqsForTag(term);
		break;
	    case SEARCH:
		// questionsFragment.search(term);
		break;
	    default:
		Log.d(TAG, "Invalid question action");
		break;
	}
    }
}

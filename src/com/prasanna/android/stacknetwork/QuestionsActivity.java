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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.prasanna.android.stacknetwork.fragment.QuestionsFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionsFragment.OnGetQuestionsListener;
import com.prasanna.android.stacknetwork.fragment.QuestionsFragment.QuestionAction;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.FetchTagsAsyncTask;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class QuestionsActivity extends AbstractUserActionBarActivity implements OnGetQuestionsListener
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();

    protected boolean serviceRunning = false;

    protected Intent questionsIntent;

    private ScrollViewWithNotifier scrollView;

    private ArrayList<String> tags = new ArrayList<String>();

    private int itemPosition;

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

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.scroll_fragment);

	scrollView = (ScrollViewWithNotifier) findViewById(R.id.itemScroller);
	scrollView.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
	{
	    @Override
	    public void onScrollToBottom(View view)
	    {
		QuestionsFragment questionsFragment = (QuestionsFragment) getFragmentManager().findFragmentById(
		                R.id.questionsFragment);
		questionsFragment.onScrollToBottom();
	    }
	});

	int lastSavedPosition = -1;

	if (savedInstanceState != null)
	{
	    tags = (ArrayList<String>) savedInstanceState.getSerializable(StringConstants.TAGS);
	    lastSavedPosition = savedInstanceState.getInt(StringConstants.ITEM_POSITION);
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
	QuestionsFragment questionsFragment = (QuestionsFragment) getFragmentManager().findFragmentById(
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
	outState.putInt(StringConstants.ITEM_POSITION, itemPosition);

	super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
	onGetQuestions(QuestionAction.SEARCH, query);
	return true;
    }

    @Override
    public void onGetQuestions(QuestionAction questionAction, String term)
    {
	QuestionsFragment questionsFragment = (QuestionsFragment) getFragmentManager().findFragmentById(
	                R.id.questionsFragment);

	switch (questionAction)
	{
	    case FRONT_PAGE:
		questionsFragment.loadFrontPage();
		break;
	    case FAQS_TAG:
		questionsFragment.getFaqsForTag(term);
		break;
	    case SEARCH:
		questionsFragment.search(term);
		break;
	    default:
		break;
	}

    }
}

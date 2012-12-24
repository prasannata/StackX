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
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.prasanna.android.stacknetwork.fragment.FrontPageFragment;
import com.prasanna.android.stacknetwork.fragment.ItemDisplayFragment;
import com.prasanna.android.stacknetwork.fragment.TagFaqFragment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.FetchTagsAsyncTask;

public class QuestionsActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();

    private static String HOME_FRAGMENT_TAG = "home";

    private ItemDisplayFragment<Question> currentFragment = null;

    private int itemPosition;

    private ArrayAdapter<String> spinnerAdapter;

    public class FetchUserTagsCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<String>>
    {
	@Override
	public void notifyOnCompletion(ArrayList<String> result)
	{
	    tags.add(StringConstants.FRONT_PAGE);
	    tags.addAll(result);

	    initActionBarSpinner();
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
		    openTagFragment(itemPosition);
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

	int lastSavedPosition = -1;

	if (savedInstanceState != null)
	{
	    tags = (ArrayList<String>) savedInstanceState.getSerializable(StringConstants.TAGS);
	    lastSavedPosition = savedInstanceState.getInt(StringConstants.ITEM_POSITION);
	}

	if (tags == null || tags.isEmpty())
	{
	    FetchTagsAsyncTask fetchUserAsyncTask = new FetchTagsAsyncTask(new FetchUserTagsCompletionNotifier(),
		            AppUtils.inRegisteredSite(getCacheDir()));
	    fetchUserAsyncTask.execute(1);
	}
	else
	{
	    initActionBarSpinner();

	    if (lastSavedPosition > 0)
	    {
		getActionBar().setSelectedNavigationItem(lastSavedPosition);
	    }
	}
    }

    @Override
    protected void startQuestionsService()
    {
	if (currentFragment != null)
	{
	    currentFragment.startIntentService();
	}
    }

    @Override
    protected void registerQuestionsSearchReceiver()
    {
    }

    @Override
    public void refresh()
    {
	if (currentFragment != null)
	{
	    currentFragment.refresh();
	}
    }

    @Override
    public Context getCurrentContext()
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
	outState.putInt(StringConstants.ITEM_POSITION, itemPosition);

	super.onSaveInstanceState(outState);
    }

    protected void openTagFragment(int itemPosition)
    {
	String fragmentTag = null;

	if (itemPosition == 0)
	{
	    currentFragment = new FrontPageFragment();
	    fragmentTag = HOME_FRAGMENT_TAG;
	}
	else
	{
	    currentFragment = new TagFaqFragment();
	    ((TagFaqFragment) currentFragment).setqTag(tags.get(itemPosition));
	    fragmentTag = tags.get(itemPosition);
	}

	this.itemPosition = itemPosition;

	if (currentFragment != null)
	{
	    Log.d(TAG, "Replacing fragment");

	    currentFragment.setRetainInstance(true);

	    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
	    fragmentTransaction.replace(R.id.itemScroller, currentFragment, fragmentTag);
	    fragmentTransaction.commit();
	}
    }

    @Override
    protected void onScrollToBottom()
    {
	if (currentFragment != null)
	{
	    currentFragment.onScrollToBottom();
	}
    }
}

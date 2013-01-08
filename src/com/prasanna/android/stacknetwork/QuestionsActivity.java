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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.prasanna.android.provider.RecentQueriesProvider;
import com.prasanna.android.stacknetwork.fragment.ItemListFragment.OnContextItemSelectedListener;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetTagsAsyncTask;

public class QuestionsActivity extends AbstractUserActionBarActivity implements OnContextItemSelectedListener<Question>
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG_FRONT_PAGE = "front_page";
    private static final String FRAGMENT_TAG_SPINNER_ITEM_PREFIX = "spinner-";

    private ArrayList<String> tags = new ArrayList<String>();
    private ArrayAdapter<String> spinnerAdapter;

    public class GetUserTagsCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<String>>
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
			loadFrontPage();
		    else
			loadFaqForTag(itemPosition);

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
	Log.d(TAG, "onCreate");

	super.onCreate(savedInstanceState);

	setContentView(R.layout.fragment_container);

	if (Intent.ACTION_SEARCH.equals(getIntent().getAction()))
	{
	    saveSearchQuery();
	    beginTransaction(QuestionListFragment.newFragment(QuestionsIntentService.SEARCH), null);
	}
	else if (StringConstants.RELATED.equals(getIntent().getAction()))
	{
	    beginRelatedQuestionsFragment(getIntent().getLongExtra(StringConstants.QUESTION_ID, 0));
	}
	else if (StringConstants.TAG.equals(getIntent().getAction()))
	{
	    beginFaqForTagFragment(getIntent().getStringExtra(StringConstants.TAG));
	}
	else
	{
	    getSpinnerTags(savedInstanceState);
	}
    }

    private void saveSearchQuery()
    {
	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RecentQueriesProvider.AUTHORITY,
	                RecentQueriesProvider.MODE);
	suggestions.saveRecentQuery(getIntent().getStringExtra(SearchManager.QUERY), null);
    }

    @SuppressWarnings("unchecked")
    private void getSpinnerTags(Bundle savedInstanceState)
    {
	if (savedInstanceState != null)
	{
	    Log.d(TAG, "Restoring tags from savedInstanceState");

	    tags = (ArrayList<String>) savedInstanceState.getSerializable(StringConstants.TAGS);
	    int lastSavedPosition = savedInstanceState.getInt(StringConstants.ITEM_POSITION);

	    initActionBarSpinner();

	    if (lastSavedPosition > 0)
		getActionBar().setSelectedNavigationItem(lastSavedPosition);
	}
	else
	{
	    GetTagsAsyncTask fetchUserAsyncTask = new GetTagsAsyncTask(new GetUserTagsCompletionNotifier(),
		            AppUtils.inRegisteredSite(getApplicationContext()));
	    fetchUserAsyncTask.execute(1);
	}
    }

    @Override
    public void refresh()
    {
	QuestionListFragment questionsFragment = (QuestionListFragment) getFragmentManager().findFragmentById(
	                R.id.fragmentContainer);
	questionsFragment.refresh();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
	Log.d(TAG, "Saving activity instance");
	outState.putSerializable(StringConstants.TAGS, tags);
	super.onSaveInstanceState(outState);
    }

    private void beginFaqForTagFragment(String faqTag)
    {
	QuestionListFragment newFragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_FAQ_FOR_TAG);
	newFragment.getBundle().putString(StringConstants.TAG, faqTag);
	beginTransaction(newFragment, FRAGMENT_TAG_SPINNER_ITEM_PREFIX + faqTag);
    }

    private void beginTransaction(QuestionListFragment questionListFragment, String fragmentTag)
    {
	FragmentTransaction transaction = getFragmentManager().beginTransaction();
	transaction.replace(R.id.fragmentContainer, questionListFragment, fragmentTag);
	transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	transaction.addToBackStack(null);
	transaction.commit();
    }

    private Fragment findFragment(String fragmentTag)
    {
	return getFragmentManager().findFragmentByTag(fragmentTag);
    }

    private void beginRelatedQuestionsFragment(long questionId)
    {
	QuestionListFragment newFragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_RELATED);
	newFragment.getBundle().putLong(StringConstants.QUESTION_ID, questionId);
	beginTransaction(newFragment, QuestionsIntentService.GET_RELATED + "-" + questionId);
    }

    private void emailQuestion(String subject, String body)
    {
	Intent emailIntent = IntentUtils.createEmailIntent(subject, body);
	startActivity(Intent.createChooser(emailIntent, ""));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item, Question question)
    {
	if (item.getGroupId() == R.id.qContextMenuGroup)
	{
	    Log.d(TAG, "Context item selected: " + item.getTitle());

	    switch (item.getItemId())
	    {
		case R.id.q_ctx_comments:
		    showComments(question);
		    return true;
		case R.id.q_ctx_menu_user_profile:
		    Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
		    userProfileIntent.putExtra(StringConstants.USER_ID, question.owner.id);
		    startActivity(userProfileIntent);
		    break;
		case R.id.q_ctx_related:
		    beginRelatedQuestionsFragment(question.id);
		    return true;
		case R.id.q_ctx_menu_email:
		    emailQuestion(question.title, question.link);
		    return true;
		default:
		    return false;
	    }
	}
	else if (item.getGroupId() == R.id.qContextTagsMenuGroup)
	{
	    Log.d(TAG, "Tag selected: " + item.getTitle());
	    beginFaqForTagFragment((String) item.getTitle());
	    return true;
	}

	return false;
    }

    private void showComments(Question question)
    {
	if (question != null && question.comments != null && question.comments.size() > 0)
	    Toast.makeText(this, "Fetch comments", Toast.LENGTH_LONG).show();
	else
	    Toast.makeText(this, "No comments", Toast.LENGTH_LONG).show();
    }

    /*
     * While pressing back, when all fragments have been popped out of the
     * stack, an empty container is shown and I have no FUCKING idea as how to
     * get rid of that other than doing this shit. Having noHistory set on this
     * activity makes no effect. (non-Javadoc)
     * 
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed()
    {
	super.onBackPressed();

	if (getFragmentManager().getBackStackEntryCount() == 0)
	    finish();
    }

    private void loadFrontPage()
    {
	Log.d(TAG, "Front page selected");

	QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_FRONT_PAGE);

	if (fragment == null)
	{
	    Log.d(TAG, "Creating new fragment for front page");
	    fragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_FRONT_PAGE);
	}

	beginTransaction(fragment, FRAGMENT_TAG_FRONT_PAGE);
    }
    
    private void loadFaqForTag(int itemPosition)
    {
	Log.d(TAG, tags.get(itemPosition) + " selected");

	QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_SPINNER_ITEM_PREFIX
	                + tags.get(itemPosition));
	if (fragment == null)
	    beginFaqForTagFragment(tags.get(itemPosition));
	else
	    beginTransaction(fragment, FRAGMENT_TAG_SPINNER_ITEM_PREFIX + tags.get(itemPosition));
    }

}

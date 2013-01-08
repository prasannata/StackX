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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.listener.MenuItemClickListener;
import com.prasanna.android.provider.RecentQueriesProvider;
import com.prasanna.android.stacknetwork.fragment.ItemListFragment.OnContextItemSelectedListener;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment;
import com.prasanna.android.stacknetwork.fragment.TagListFragment;
import com.prasanna.android.stacknetwork.fragment.TagListFragment.OnTagSelectListener;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsActivity extends AbstractUserActionBarActivity implements
        OnContextItemSelectedListener<Question>, MenuItemClickListener, OnTagSelectListener
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG_PREFIX = "spinner_";
    private static final String FRAGMENT_TAG_FRONT_PAGE = FRAGMENT_TAG_PREFIX + "front_page";

    private ArrayList<String> tags = new ArrayList<String>();
    private String currentFragmentTag;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);
        setMenuItemClickListener(this);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction()))
        {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            saveSearchQuery(query);
            replaceFragment(
                    QuestionListFragment.newFragment(QuestionsIntentService.SEARCH, query, null),
                    null, true);
        }
        else if (StringConstants.RELATED.equals(getIntent().getAction()))
        {
            beginRelatedQuestionsFragment(getIntent().getLongExtra(StringConstants.QUESTION_ID, 0));
        }
        else if (StringConstants.TAG.equals(getIntent().getAction()))
        {
            addFaqFragment(getIntent().getStringExtra(StringConstants.TAG));
        }
        else
        {
            showFrontPageFragment();
        }
    }

    private void saveSearchQuery(String query)
    {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                RecentQueriesProvider.AUTHORITY, RecentQueriesProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }

    @Override
    public void refresh()
    {
        QuestionListFragment questionsFragment = (QuestionListFragment) getFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        questionsFragment.refresh();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "Saving activity instance");
        outState.putSerializable(StringConstants.TAGS, tags);
        super.onSaveInstanceState(outState);
    }

    private void addFaqFragment(String faqTag)
    {
        QuestionListFragment newFragment = QuestionListFragment.newFragment(
                QuestionsIntentService.GET_FAQ_FOR_TAG, faqTag, null);
        newFragment.getBundle().putString(StringConstants.TAG, faqTag);
        addFragment(newFragment, FRAGMENT_TAG_PREFIX + faqTag, false);
    }

    private void removeFragment(String fragmentTag, boolean addToBackStack)
    {
        Fragment fragment = getFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null)
        {
            Log.d(TAG, "Removing fragment " + fragmentTag);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(fragment);
            if (addToBackStack)
                ft.addToBackStack(fragmentTag);
            ft.commit();
        }
    }

    private void hideFragment(String fragmentTag)
    {
        Fragment fragment = getFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null)
        {
            Log.d(TAG, "hiding fragment " + fragmentTag);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(fragment);
            ft.commit();
        }
    }

    private void showFragment(String fragmentTag)
    {
        Fragment fragment = getFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null)
        {
            Log.d(TAG, "showing fragment " + fragmentTag);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.show(fragment);
            ft.commit();
        }
    }

    private void addFragment(Fragment fragment, String fragmentTag, boolean addToBackStack)
    {
        Log.d(TAG, "Adding fragment " + fragmentTag);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragmentContainer, fragment, fragmentTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (addToBackStack)
            ft.addToBackStack(fragmentTag);
        ft.commit();
        currentFragmentTag = fragmentTag;
    }

    private void replaceFragment(Fragment fragment, String fragmentTag, boolean addToBackStack)
    {
        Log.d(TAG, "Replacing current fragment with " + fragmentTag);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment, fragmentTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (addToBackStack)
            ft.addToBackStack(fragmentTag);
        ft.commit();
        currentFragmentTag = fragmentTag;
    }

    private Fragment findFragment(String fragmentTag)
    {
        return getFragmentManager().findFragmentByTag(fragmentTag);
    }

    private void beginRelatedQuestionsFragment(long questionId)
    {
        QuestionListFragment newFragment = QuestionListFragment.newFragment(
                QuestionsIntentService.GET_RELATED, null, null);
        newFragment.getBundle().putLong(StringConstants.QUESTION_ID, questionId);
        replaceFragment(newFragment, QuestionsIntentService.GET_RELATED + "-" + questionId, true);
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
            addFaqFragment((String) item.getTitle());
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

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        Log.d(TAG, "Num items in backstack: " + getFragmentManager().getBackStackEntryCount());

        for (int i = getFragmentManager().getBackStackEntryCount() - 1; i >= 0; i--)
            Log.d(TAG, "Backstack pos: " + i + " - "
                    + getFragmentManager().getBackStackEntryAt(i).getName());

        if (getFragmentManager().getBackStackEntryCount() == 0)
            finish();
    }

    private void showFrontPageFragment()
    {
        Log.d(TAG, "Front page selected");

        QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_FRONT_PAGE);

        if (fragment == null)
        {
            Log.d(TAG, "Creating new fragment for front page");
            fragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_FRONT_PAGE,
                    StringConstants.FRONT_PAGE, null);
        }

        replaceFragment(fragment, FRAGMENT_TAG_FRONT_PAGE, false);
    }

    @Override
    public boolean onClick(MenuItem menuItem)
    {
        if (menuItem.getItemId() == android.R.id.home)
        {
            TagListFragment tagListFragment = (TagListFragment) getFragmentManager()
                    .findFragmentByTag(StringConstants.TAGS);

            if (tagListFragment == null)
                tagListFragment = TagListFragment.newFragment(this);

            hideFragment(currentFragmentTag);
            addFragment(tagListFragment, StringConstants.TAGS, false);
            return true;
        }

        return false;
    }

    @Override
    public void onFrontPageSelected()
    {
        Log.d(TAG, "Front page selected");

        QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_FRONT_PAGE);

        if (fragment == null)
        {
            Log.d(TAG, "Creating new fragment for front page");
            fragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_FRONT_PAGE,
                    StringConstants.FRONT_PAGE, null);
            replaceFragment(fragment, FRAGMENT_TAG_FRONT_PAGE, true);
        }
        else
        {
            replaceFragment(fragment, FRAGMENT_TAG_FRONT_PAGE, false);
        }

    }

    @Override
    public void onTagSelected(String tag)
    {
        Log.d(TAG, tag + " selected");

        QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_PREFIX
                + tag);

        removeFragment(StringConstants.TAGS, false);

        if (fragment == null)
            addFaqFragment(tag);
        else
            showFragment(FRAGMENT_TAG_PREFIX + tag);
    }
}

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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.MenuItem;

import com.prasanna.android.listener.MenuItemClickListener;
import com.prasanna.android.provider.RecentQueriesProvider;
import com.prasanna.android.stacknetwork.fragment.ItemListFragment.OnContextItemSelectedListener;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment;
import com.prasanna.android.stacknetwork.fragment.TagListFragment;
import com.prasanna.android.stacknetwork.fragment.TagListFragment.OnTagSelectListener;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri.Sort;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsActivity extends AbstractUserActionBarActivity implements
                OnContextItemSelectedListener<Question>, MenuItemClickListener, OnTagSelectListener
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();
    private static final String TAB_TITLE_ACTIVE = "Active";
    private static final String TAB_TITLE_NEW = "New";
    private static final String TAB_TITLE_MOST_VOTED = "Most Voted";
    private static final String TAB_TITLE_FAQ = "FAQ";

    private static QuestionsActivity me;

    private boolean showTagsFragment = false;
    private TagListFragment tagListFragment;

    public static QuestionListFragment getFragment(String fragmentTag)
    {
        if (me != null)
            return (QuestionListFragment) me.getFragmentManager().findFragmentByTag(fragmentTag);
        else
            return null;
    }

    public class TabListener implements ActionBar.TabListener
    {
        private final QuestionListFragment fragment;

        public TabListener(QuestionListFragment fragment)
        {
            this.fragment = fragment;
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            ft.add(R.id.fragmentContainer, fragment, fragment.fragmentTag);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            ft.remove(fragment);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        me = this;

        setContentView(R.layout.fragment_container);
        String action = getIntent().getAction();

        if (Intent.ACTION_SEARCH.equals(action))
            showSearchFragment();
        else if (StringConstants.SIMILAR.equals(action))
            showSimilarQuestionListFragment();
        else if (StringConstants.RELATED.equals(action))
            showRelatedQuestionListFragment();
        else if (StringConstants.TAG.equals(action))
            showTagQuestionListFragment();
        else
            showHomePageForSite();
    }

    private void showHomePageForSite()
    {
        setMenuItemClickListener(this);
        showFrontPageForSite();
    }

    private void showSearchFragment()
    {
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        Log.d(TAG, "Launching search fragment for " + query);
        saveSearchQuery(query);
        replaceFragment(QuestionListFragment.newFragment(QuestionsIntentService.SEARCH, query, null), null, false);
    }

    private void showFrontPageForSite()
    {
        showTagsFragment = true;
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupActionBarTabs(QuestionsIntentService.GET_FRONT_PAGE, OperatingSite.getSite().name, true);
    }

    private void showSimilarQuestionListFragment()
    {
        String title = getIntent().getStringExtra(StringConstants.TITLE);

        getActionBar().setTitle(getString(R.string.similar) + " to " + title);

        QuestionListFragment newFragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_SIMILAR, null,
                        null);
        newFragment.getBundle().putString(StringConstants.TITLE, title);
        replaceFragment(newFragment, StringConstants.SIMILAR + "-" + title.hashCode(), false);
    }

    private void showRelatedQuestionListFragment()
    {
        long questionId = getIntent().getLongExtra(StringConstants.QUESTION_ID, 0);
        QuestionListFragment newFragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_RELATED, null,
                        null);
        newFragment.getBundle().putLong(StringConstants.QUESTION_ID, questionId);
        replaceFragment(newFragment, QuestionsIntentService.GET_RELATED + "-" + questionId, false);
    }

    private void showTagQuestionListFragment()
    {
        String tag = getIntent().getStringExtra(StringConstants.TAG);

        QuestionListFragment newFragment = QuestionListFragment.newFragment(
                        QuestionsIntentService.GET_QUESTIONS_FOR_TAG, tag, null);
        newFragment.getBundle().putString(StringConstants.TAG, tag);
        setupActionBarTabs(QuestionsIntentService.GET_QUESTIONS_FOR_TAG, tag, false);
    }

    private void setupActionBarTabs(int action, String tag, boolean frontPage)
    {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        createTab(TAB_TITLE_ACTIVE, QuestionListFragment.newFragment(action, tag, Sort.ACTIVITY));
        createTab(TAB_TITLE_NEW, QuestionListFragment.newFragment(action, tag, Sort.CREATION));
        createTab(TAB_TITLE_MOST_VOTED, QuestionListFragment.newFragment(action, tag, Sort.VOTES));

        if (!frontPage)
        {
            createTab(TAB_TITLE_FAQ,
                            QuestionListFragment.newFragment(QuestionsIntentService.GET_FAQ_FOR_TAG, tag, null));
        }
    }

    private void createTab(String title, QuestionListFragment fragment)
    {
        Tab tab = getActionBar().newTab();
        tab.setText(title).setTabListener(new TabListener(fragment));
        getActionBar().addTab(tab);
    }

    private void saveSearchQuery(String query)
    {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RecentQueriesProvider.AUTHORITY,
                        RecentQueriesProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }

    @Override
    public void refresh()
    {
        QuestionListFragment questionsFragment = (QuestionListFragment) getFragmentManager().findFragmentById(
                        R.id.fragmentContainer);
        questionsFragment.refresh();
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item, Question question)
    {
        if (item.getGroupId() == R.id.qContextMenuGroup)
        {
            Log.d(TAG, "Context item selected: " + item.getTitle());
            switch (item.getItemId())
            {
                case R.id.q_ctx_menu_user_profile:
                    showUserProfile(question.owner.id);
                    break;
                case R.id.q_ctx_similar:
                    startSimirarQuestionsActivity(question.title);
                    return true;
                case R.id.q_ctx_related:
                    startRelatedQuestionsActivity(question.id);
                    return true;
                case R.id.q_ctx_menu_email:
                    emailQuestion(question.title, question.link);
                    return true;
                default:
                    Log.d(TAG, "Unknown item in context menu: " + item.getTitle());
                    return false;
            }
        }
        else if (item.getGroupId() == R.id.qContextTagsMenuGroup)
        {
            Log.d(TAG, "Tag selected: " + item.getTitle());
            startTagQuestionsActivity((String) item.getTitle());
            return true;
        }

        return false;
    }

    @Override
    public boolean onClick(MenuItem menuItem)
    {
        Log.d(TAG, "Home button clicked");

        if (showTagsFragment && menuItem.getItemId() == android.R.id.home)
        {
            tagListFragment = (TagListFragment) getFragmentManager().findFragmentByTag(StringConstants.TAGS);

            if (tagListFragment == null)
            {
                tagListFragment = TagListFragment.newFragment(this);
                addAndHideFragment(tagListFragment, StringConstants.TAGS);
            }

            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            getActionBar().setTitle(StringConstants.TAGS);
            actionBarMenu.findItem(R.id.menu_search).setVisible(false);
            actionBarMenu.findItem(R.id.menu_refresh).setVisible(false);

            showTagFragment();

            return true;
        }

        return false;
    }

    @Override
    public void onFrontPageSelected()
    {
        Log.d(TAG, "Front page selected");

        hideTagFragmentAndSetupTabsForTag(QuestionsIntentService.GET_FRONT_PAGE, OperatingSite.getSite().name, true);
    }

    @Override
    public void onTagSelected(String tag)
    {
        Log.d(TAG, tag + " selected");

        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        hideTagFragmentAndSetupTabsForTag(QuestionsIntentService.GET_QUESTIONS_FOR_TAG, tag, false);
    }

    private void startSimirarQuestionsActivity(String title)
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.SIMILAR);
        questionsIntent.putExtra(StringConstants.TITLE, title);
        startActivity(questionsIntent);
    }

    private void startRelatedQuestionsActivity(long questionId)
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.RELATED);
        questionsIntent.putExtra(StringConstants.QUESTION_ID, questionId);
        startActivity(questionsIntent);
    }

    private void startTagQuestionsActivity(String tag)
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.TAG);
        questionsIntent.putExtra(StringConstants.TAG, tag);
        startActivity(questionsIntent);
    }

    private void showUserProfile(long userId)
    {
        Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        startActivity(userProfileIntent);
    }

    private void emailQuestion(String subject, String body)
    {
        Intent emailIntent = IntentUtils.createEmailIntent(subject, body);
        startActivity(Intent.createChooser(emailIntent, ""));
    }

    private void addAndHideFragment(TagListFragment fragment, String fragmentTag)
    {
        Log.d(TAG, "Replacing current fragment with " + fragmentTag);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragmentContainer, fragment, fragmentTag);
        ft.hide(fragment);
        ft.commit();
    }

    private void hideTagFragmentAndSetupTabsForTag(int action, String tag, boolean frontPage)
    {
        getActionBar().removeAllTabs();
        actionBarMenu.findItem(R.id.menu_search).setVisible(true);
        actionBarMenu.findItem(R.id.menu_refresh).setVisible(true);
        hideTagFragment();
        setupActionBarTabs(action, tag, frontPage);
    }

    private void showTagFragment()
    {
        Log.d(TAG, "Showing tag list fragment");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.show(tagListFragment);
        ft.commit();
    }

    private void hideTagFragment()
    {
        Log.d(TAG, "Hiding tag list fragment");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(tagListFragment);
        ft.commit();
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
    }
}

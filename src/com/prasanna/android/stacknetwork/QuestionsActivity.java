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
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.provider.RecentQueriesProvider;
import com.prasanna.android.stacknetwork.fragment.ItemListFragment.OnContextItemSelectedListener;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment;
import com.prasanna.android.stacknetwork.fragment.TagListFragment;
import com.prasanna.android.stacknetwork.fragment.TagListFragment.OnTagSelectListener;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackUri.Sort;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsActivity extends AbstractUserActionBarActivity implements
        OnContextItemSelectedListener<Question>, OnTagSelectListener
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();
    private static final String TAB_TITLE_ACTIVE = "Active";
    private static final String TAB_TITLE_NEW = "New";
    private static final String TAB_TITLE_MOST_VOTED = "Most Voted";
    private static final String TAB_TITLE_FAQ = "FAQ";
    private static final String SAVED = "saved";
    private static final String LAST_SELECTED_TAB = "last_selected_tab";

    private static QuestionsActivity me;

    private boolean showTagsFragment = false;
    private TagListFragment tagListFragment;
    private String intentAction;
    private String tag;
    private int action;

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
            Fragment existingFragment = getFragmentManager().findFragmentByTag(fragment.fragmentTag);

            if (existingFragment == null)
            {
                Log.d(TAG, "adding fragment: " + fragment.fragmentTag);
                ft.add(R.id.fragmentContainer, fragment, fragment.fragmentTag);
            }
            else
            {
                Log.d(TAG, "Attaching fragment: " + fragment.fragmentTag);
                ft.attach(existingFragment);
            }
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

        if (savedInstanceState == null)
        {
            showFragmentForIntentAction();
        }
        else
        {
            action = savedInstanceState.getInt(StringConstants.ACTION, 0);

            if (action != 0)
            {
                if (action == QuestionsIntentService.GET_QUESTIONS_FOR_TAG)
                    showLastTagQuestionListFragment(savedInstanceState);
                else
                    showFrontPageForSite();

                int lastSelectedTab = savedInstanceState.getInt(LAST_SELECTED_TAB, 0);
                if (lastSelectedTab > 0)
                    getActionBar().setSelectedNavigationItem(lastSelectedTab);
            }
            else
                showFragmentForIntentAction();
        }
    }

    private void showFragmentForIntentAction()
    {
        intentAction = getIntent().getAction();

        if (Intent.ACTION_SEARCH.equals(intentAction))
            showSearchFragment();
        else if (StringConstants.SIMILAR.equals(intentAction))
            showSimilarQuestionListFragment();
        else if (StringConstants.RELATED.equals(intentAction))
            showRelatedQuestionListFragment();
        else if (StringConstants.TAG.equals(intentAction))
            showTagQuestionListFragment();
        else
            showFrontPageForSite();
    }

    private void showFrontPageForSite()
    {
        showTagsFragment = true;
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupActionBarTabs(QuestionsIntentService.GET_FRONT_PAGE, OperatingSite.getSite().name, true);
    }

    private void showSearchFragment()
    {
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        Log.d(TAG, "Launching search fragment for " + query);
        saveSearchQuery(query);
        replaceFragment(QuestionListFragment.newFragment(QuestionsIntentService.SEARCH, query, null), null, false);
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
        Log.d(TAG, "Restoring question list fragment for tag" + tag);
        setupActionBarTabs(QuestionsIntentService.GET_QUESTIONS_FOR_TAG, tag, false);
    }

    private void showLastTagQuestionListFragment(Bundle savedInstanceState)
    {
        showTagsFragment = true;
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupActionBarTabs(action, savedInstanceState.getString(StringConstants.TAG), false);
    }

    private void setupActionBarTabs(int action, String tag, boolean frontPage)
    {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        this.action = action;
        this.tag = tag;

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
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");

        removeTagListFragment();

        if (getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
            outState.putInt(LAST_SELECTED_TAB, getActionBar().getSelectedNavigationIndex());

        outState.putBoolean(SAVED, true);
        outState.putInt(StringConstants.ACTION, action);
        outState.putString(StringConstants.TAG, tag);

        super.onSaveInstanceState(outState);
    }

    private void removeTagListFragment()
    {
        if (tagListFragment != null)
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(tagListFragment);
            ft.commit();
            tagListFragment = null;
        }
    }

    @Override
    public void onBackPressed()
    {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (currentFragment instanceof TagListFragment)
        {
            Log.d(TAG, "Tag list fragment is current fragment");

            hideTagFragment();

            toggleDisplayForTags(false);
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        if (menu != null && intentAction != null && StringConstants.TAG.equals(intentAction))
            menu.findItem(R.id.menu_new_label).setVisible(true);

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (intentAction != null && StringConstants.TAG.equals(intentAction))
        {
            if (item.getItemId() == R.id.menu_new_label)
            {
                insertTagToDb();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertTagToDb()
    {
        TagDAO tagDAO = new TagDAO(this);
        String tagLabel = getIntent().getStringExtra(StringConstants.TAG);
        try
        {
            tagDAO.open();
            tagDAO.insert(OperatingSite.getSite().apiSiteParameter, tagLabel, true);

            Toast.makeText(this, tagLabel + " added to your tags", Toast.LENGTH_LONG).show();

            SharedPreferencesUtil.setOnOff(this, TagListFragment.TAGS_DIRTY, true);
        }
        catch (SQLException e)
        {
            Toast.makeText(this, "Failed to add " + tagLabel + " to your tags", Toast.LENGTH_LONG).show();
        }
        finally
        {
            tagDAO.close();
        }
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
    protected boolean onActionBarHomeButtonClick(MenuItem menuItem)
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
            else
                tagListFragment.setOnTagSelectListener(this);

            toggleDisplayForTags(true);

            showTagFragment();

            return true;
        }

        return false;
    }

    private void toggleDisplayForTags(boolean forTags)
    {
        getActionBar().setDisplayHomeAsUpEnabled(!forTags);

        if (forTags)
        {
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            getActionBar().setTitle(StringConstants.TAGS);
        }
        else
        {
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
        actionBarMenu.findItem(R.id.menu_search).setVisible(!forTags);
        actionBarMenu.findItem(R.id.menu_refresh).setVisible(!forTags);
    }

    @Override
    public void onFrontPageSelected()
    {
        Log.d(TAG, "Front page selected");

        hideTagFragment();
        setupTabsForTag(QuestionsIntentService.GET_FRONT_PAGE, OperatingSite.getSite().name, true);
    }

    @Override
    public void onTagSelected(String tag)
    {
        Log.d(TAG, tag + " selected");

        hideTagFragment();
        setupTabsForTag(QuestionsIntentService.GET_QUESTIONS_FOR_TAG, tag, false);
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

    private void setupTabsForTag(int action, String tag, boolean frontPage)
    {
        getActionBar().removeAllTabs();
        actionBarMenu.findItem(R.id.menu_search).setVisible(true);
        actionBarMenu.findItem(R.id.menu_refresh).setVisible(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupActionBarTabs(action, tag, frontPage);
    }

    private void showTagFragment()
    {
        Log.d(TAG, "Showing tag list fragment");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
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
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.replace(R.id.fragmentContainer, fragment, fragmentTag);
        if (addToBackStack)
            ft.addToBackStack(fragmentTag);
        ft.commit();
    }
}

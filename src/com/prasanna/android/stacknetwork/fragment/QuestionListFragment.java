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

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.QuestionsActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionListFragment extends AbstractQuestionListFragment
{
    private static final String TAG = QuestionListFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG_PREFIX = "fragment_q";

    private Intent intent;
    private int currentPage = 0;
    private int action;
    private boolean created = false;

    public String sort;
    public String tag;
    public String fragmentTag;

    private SearchCriteria criteria;

    public static QuestionListFragment newFragment(int action, String tag, String sort)
    {
        String fragmentTag = null;

        if (tag != null)
            fragmentTag = FRAGMENT_TAG_PREFIX + "_" + tag.replaceAll(" ", "_");

        if (sort != null)
            fragmentTag = fragmentTag + "_" + sort;

        QuestionListFragment fragment = getFragment(fragmentTag);

        fragment.fragmentTag = fragmentTag;
        fragment.sort = sort;
        fragment.action = action;
        fragment.tag = tag;

        return fragment;
    }

    private static QuestionListFragment getFragment(String fragmentTag)
    {
        QuestionListFragment newFragment = QuestionsActivity.getFragment(fragmentTag);
        if (newFragment == null)
        {
            Log.d(TAG, "Creating new fragment: " + fragmentTag);
            newFragment = new QuestionListFragment();
        }
        return newFragment;
    }

    public static QuestionListFragment newFragment(String fragmentTag, SearchCriteria searchCriteria)
    {
        QuestionListFragment fragment = getFragment(fragmentTag);
        fragment.fragmentTag = fragmentTag;
        fragment.criteria = searchCriteria;
        fragment.action = QuestionsIntentService.SEARCH;
        return fragment;
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

        if (savedInstanceState != null)
            action = savedInstanceState.getInt(StringConstants.ACTION);

        created = false;

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        if (tag != null)
        {
            if ((action == QuestionsIntentService.GET_QUESTIONS_FOR_TAG || action == QuestionsIntentService.GET_FAQ_FOR_TAG)
                            && getActivity().getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_STANDARD)
                getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            getActivity().getActionBar().setTitle(tag);
        }

        findActionAndStartService();

    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");

        outState.putInt(StringConstants.ACTION, action);

        super.onSaveInstanceState(outState);
    }

    private void findActionAndStartService()
    {
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
                case QuestionsIntentService.GET_QUESTIONS_FOR_TAG:
                    getQuestionsForTag();
                    break;
                case QuestionsIntentService.GET_SIMILAR:
                    getSimilarQuestions();
                    break;
                case QuestionsIntentService.GET_RELATED:
                    getRelatedQuestions();
                    break;
                case QuestionsIntentService.SEARCH:
                    if (criteria == null)
                        search(getActivity().getIntent().getStringExtra(SearchManager.QUERY));
                    else
                        startSearchIntentService();
                    break;
                default:
                    Log.d(TAG, "Unknown action: " + action);
                    break;
            }

            created = true;
        }
        else
        {
            Log.d(TAG, "Fragment " + tag + " was already created. Restoring");

            if (itemListAdapter != null)
            {
                Log.d(TAG, "Notifying item list adapter");

                itemListAdapter.notifyDataSetChanged();
                itemListAdapter.notifyDataSetInvalidated();
            }
        }
    }

    @Override
    protected void startIntentService()
    {
        showProgressBar();
        intent.putExtra(StringConstants.PAGE, ++currentPage);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
        intent.putExtra(StringConstants.SORT, sort);
        startService(intent);
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }

    @Override
    public void refresh()
    {
        clean();
        removeErrorViewIfShown();
        created = false;
        findActionAndStartService();
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

        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FRONT_PAGE);
            startIntentService();
        }
    }

    private void getSimilarQuestions()
    {
        intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.getAction());
        if (intent != null)
        {

            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_SIMILAR);
            intent.putExtra(StringConstants.TITLE, getBundle().getString(StringConstants.TITLE));

            startIntentService();
        }
    }

    private void getRelatedQuestions()
    {
        intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.getAction());
        if (intent != null)
        {

            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_RELATED);
            intent.putExtra(StringConstants.QUESTION_ID, getBundle().getLong(StringConstants.QUESTION_ID, 0));

            startIntentService();
        }
    }

    private void getFaqsForTag()
    {
        intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.TAGS_FAQ.getAction());
        if (intent != null)
        {

            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FAQ_FOR_TAG);
            intent.putExtra(StringConstants.TAG, tag);

            startIntentService();
        }
    }

    private void getQuestionsForTag()
    {
        intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.TAGS_FAQ.getAction());
        if (intent != null)
        {

            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_QUESTIONS_FOR_TAG);
            intent.putExtra(StringConstants.TAG, tag);

            startIntentService();
        }
    }

    private void search(String query)
    {
        clean();

        if (criteria == null)
            buildSearchCriteria(query);
        else
            criteria = criteria.nextPage();

        if (intent != null)
            startSearchIntentService();
    }

    private void startSearchIntentService()
    {
        intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTION_SEARCH.getAction());
        intent.putExtra(StringConstants.ACTION, QuestionsIntentService.SEARCH_ADVANCED);
        intent.putExtra(StringConstants.SEARCH_CRITERIA, criteria);

        startIntentService();
    }

    private void buildSearchCriteria(String query)
    {
        criteria = SearchCriteria.newCriteria(query);

        if (SharedPreferencesUtil.isSet(getActivity(), SettingsFragment.KEY_PREF_SEARCH_IN_TITLE, false))
            criteria = criteria.queryMustInTitle();

        if (SharedPreferencesUtil.isSet(getActivity(), SettingsFragment.KEY_PREF_SEARCH_ONLY_WITH_ANSWERS, false))
            criteria = criteria.setMinAnswers(1);

        if (SharedPreferencesUtil.isSet(getActivity(), SettingsFragment.KEY_PREF_SEARCH_ONLY_ANSWERED, false))
            criteria = criteria.mustBeAnswered();

        criteria = criteria.sortBy(SearchSort.RELEVANCE).build();
    }
}

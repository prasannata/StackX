/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;
import com.prasanna.android.utils.TagsViewBuilder;
import com.prasanna.android.utils.TagsViewBuilder.DefaultOnTagClickListener;

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

    private static class OnTagClickListenerImpl extends DefaultOnTagClickListener
    {
        private final String questionTag;
        private final int intentAction;

        public OnTagClickListenerImpl(int intentAction, String questionTag)
        {
            this.intentAction = intentAction;
            this.questionTag = questionTag;
        }

        @Override
        public void onTagClick(Context context, String tag)
        {
            if (QuestionsIntentService.GET_QUESTIONS_FOR_TAG != intentAction || questionTag == null
                            || !questionTag.equals(tag))
                super.onTagClick(context, tag);
        }

    }

    public static QuestionListFragment newFragment(int action, String tag, String sort)
    {
        QuestionListFragment fragment = getFragment(getFragmentTag(tag, sort));
        fragment.sort = sort;
        fragment.action = action;
        fragment.tag = tag;
        return fragment;
    }

    public static QuestionListFragment newFragment(int action, String fragmentTag)
    {
        QuestionListFragment fragment = getFragment(fragmentTag);
        fragment.action = action;
        return fragment;
    }

    public static QuestionListFragment newFragment(String fragmentTag, SearchCriteria searchCriteria)
    {
        QuestionListFragment fragment = getFragment(fragmentTag);
        fragment.criteria = searchCriteria;
        fragment.action = QuestionsIntentService.SEARCH_ADVANCED;
        return fragment;
    }

    public static String getFragmentTag(String tag, String sort)
    {
        String fragmentTag = null;

        if (tag != null)
            fragmentTag = FRAGMENT_TAG_PREFIX + "_" + tag.replaceAll(" ", "_");

        if (sort != null)
            fragmentTag = fragmentTag + "_" + sort;

        return fragmentTag;
    }

    private static QuestionListFragment getFragment(String fragmentTag)
    {
        QuestionListFragment fragment = QuestionsActivity.getFragment(fragmentTag);
        if (fragment == null)
            fragment = new QuestionListFragment();

        fragment.fragmentTag = fragmentTag;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (itemsContainer == null)
        {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            itemListAdapter =
                            new ItemListAdapter<Question>(getActivity(), R.layout.question_snippet_layout,
                                            new ArrayList<Question>(), this);
        }

        if (savedInstanceState != null)
            action = savedInstanceState.getInt(StringConstants.ACTION);

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (tag != null)
        {
            if ((action == QuestionsIntentService.GET_QUESTIONS_FOR_TAG || action == QuestionsIntentService.GET_FAQ_FOR_TAG)
                            && getActivity().getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_STANDARD)
            {
                getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            }

            getActivity().getActionBar().setTitle(Html.fromHtml(tag));
        }
        else if (OperatingSite.getSite() != null)
            getActivity().getActionBar().setTitle(Html.fromHtml(OperatingSite.getSite().name));
        else
            getActivity().getActionBar().setTitle(R.string.app_name);

        findActionAndStartService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(StringConstants.ACTION, action);
        super.onSaveInstanceState(outState);
    }

    private void findActionAndStartService()
    {
        LogWrapper.d(TAG, "findActionAndStartService");
        
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
                    search(getActivity().getIntent().getStringExtra(SearchManager.QUERY));
                    break;
                case QuestionsIntentService.SEARCH_ADVANCED:
                    startSearchIntentService();
                    break;
                default:
                    LogWrapper.d(TAG, "Unknown action: " + action);
                    break;
            }

            created = true;
        }
        else
        {
            LogWrapper.d(TAG, "Fragment " + tag + " was already created. Restoring");

            if (itemListAdapter != null)
            {
                LogWrapper.d(TAG, "Notifying item list adapter");

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
    protected void buildTagsView(final Question question, final QuestionViewHolder holder)
    {
        TagsViewBuilder.buildView(getActivity(), holder.tagsLayout, question.tags, new OnTagClickListenerImpl(action,
                        tag));
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
        intent = getIntentForService(QuestionsIntentService.class, null);

        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FRONT_PAGE);
            startIntentService();
        }
    }

    private void getSimilarQuestions()
    {
        intent = getIntentForService(QuestionsIntentService.class, null);
        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_SIMILAR);
            intent.putExtra(StringConstants.TITLE, getBundle().getString(StringConstants.TITLE));
            startIntentService();
        }
    }

    private void getRelatedQuestions()
    {
        intent = getIntentForService(QuestionsIntentService.class, null);
        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_RELATED);
            intent.putExtra(StringConstants.QUESTION_ID, getBundle().getLong(StringConstants.QUESTION_ID, 0));
            startIntentService();
        }
    }

    private void getFaqsForTag()
    {
        intent = getIntentForService(QuestionsIntentService.class, null);
        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FAQ_FOR_TAG);
            intent.putExtra(StringConstants.TAG, tag);
            startIntentService();
        }
    }

    private void getQuestionsForTag()
    {
        intent = getIntentForService(QuestionsIntentService.class, null);
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
        buildSearchCriteria(query);
        startSearchIntentService();
    }

    private void startSearchIntentService()
    {
        intent = getIntentForService(QuestionsIntentService.class, null);
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

    @Override
    protected void loadNextPage()
    {
        if (action == QuestionsIntentService.SEARCH || action == QuestionsIntentService.SEARCH_ADVANCED)
            intent.putExtra(StringConstants.SEARCH_CRITERIA, criteria.nextPage());

        startIntentService();
    }
}

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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StackXQuickActionMenu;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.QuickActionMenu;

public class UserQuestionListFragment extends AbstractQuestionListFragment {
    private static final String TAG = UserQuestionListFragment.class.getSimpleName();
    private Intent intent;
    private int page = 0;
    private int action;

    public static UserQuestionListFragment newFragment(int action) {
        UserQuestionListFragment userQuestionListFragment = new UserQuestionListFragment();
        userQuestionListFragment.action = action;
        return userQuestionListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (itemsContainer == null) {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            itemListAdapter =
                    new ItemListAdapter<Question>(getActivity(), R.layout.question_snippet_layout,
                            new ArrayList<Question>(), this);
        }

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
            action = savedInstanceState.getInt(StringConstants.ACTION);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (itemListAdapter != null && itemListAdapter.getCount() == 0)
            startIntentService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(StringConstants.ACTION, action);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected QuickActionMenu initQuickActionMenu(final Question question) {
        if (action != UserIntentService.GET_USER_QUESTIONS)
            return super.initQuickActionMenu(question);

        StackXQuickActionMenu quickActionMenu = new StackXQuickActionMenu(getActivity());
        return quickActionMenu.addSimilarQuestionsItem(question.title).addRelatedQuickActionItem(question.id)
                .addEmailQuickActionItem(question.title, question.body).build();
    }

    @Override
    protected void startIntentService() {
        intent = getIntentForService(UserIntentService.class, null);
        if (intent != null) {
            showProgressBar();

            intent.putExtra(StringConstants.ACTION, action);
            intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
            intent.putExtra(StringConstants.USER_ID, getActivity().getIntent()
                    .getLongExtra(StringConstants.USER_ID, 0L));
            intent.putExtra(StringConstants.PAGE, ++page);
            intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

            startService(intent);
        }
    }

    @Override
    protected void setValuesForQuestionView(QuestionViewHolder holder, Question question) {
        super.setValuesForQuestionView(holder, question);

        if (action == UserIntentService.GET_USER_QUESTIONS)
            holder.owner.setText(DateTimeUtils.getElapsedDurationSince(question.creationDate));
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    @Override
    protected void loadNextPage() {
        startIntentService();
    }
}

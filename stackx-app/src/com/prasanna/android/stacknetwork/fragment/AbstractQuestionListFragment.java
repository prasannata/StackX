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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StackXQuickActionMenu;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.TagsViewBuilder;
import com.prasanna.android.views.QuickActionMenu;

public abstract class AbstractQuestionListFragment extends ItemListFragment<Question> implements ListItemView<Question>
{
    private final Bundle bundle = new Bundle();

    static class QuestionViewHolder
    {
        LinearLayout tagsLayout;
        TextView title;
        TextView score;
        TextView answerCount;
        TextView views;
        TextView owner;
        ArrayList<TextView> tagViews;
        TextView answerCountAnswered;
        ImageView quickActionImg;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        registerForContextMenu(getListView());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View getView(Question item, int position, View convertView, ViewGroup parent)
    {
        return buildView(convertView, getActivity(), item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        if (itemListAdapter != null && itemListAdapter.getCount() > position)
        {
            Intent displayQuestionIntent = new Intent(getActivity(), QuestionActivity.class);
            displayQuestionIntent.setAction(StringConstants.QUESTION);
            displayQuestionIntent.putExtra(StringConstants.QUESTION, itemListAdapter.getItem(position));
            displayQuestionIntent.putExtra(StringConstants.CACHED, false);
            startActivity(displayQuestionIntent);
        }
    }

    @Override
    public String getReceiverExtraName()
    {
        return StringConstants.QUESTIONS;
    }

    private View buildView(View convertView, final Context context, final Question question)
    {
        FrameLayout questionRowLayout = (FrameLayout) convertView;
        QuestionViewHolder holder;

        if (questionRowLayout == null)
        {
            holder = new QuestionViewHolder();

            questionRowLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(
                            R.layout.question_snippet_layout, null);
            holder.tagsLayout = (LinearLayout) questionRowLayout.findViewById(R.id.questionSnippetTags);
            holder.score = (TextView) questionRowLayout.findViewById(R.id.score);
            holder.answerCount = (TextView) questionRowLayout.findViewById(R.id.answerCount);
            holder.answerCountAnswered = (TextView) questionRowLayout.findViewById(R.id.answerCountAnswered);
            holder.title = (TextView) questionRowLayout.findViewById(R.id.itemTitle);
            holder.views = (TextView) questionRowLayout.findViewById(R.id.questionViewsValue);
            holder.owner = (TextView) questionRowLayout.findViewById(R.id.questionOwner);
            holder.quickActionImg = (ImageView) questionRowLayout.findViewById(R.id.itemContextMenu);
            questionRowLayout.setTag(holder);
        }
        else
            holder = (QuestionViewHolder) questionRowLayout.getTag();

        questionRowLayout.setId((int) question.id);
        setValuesForQuestionView(holder, question);
        TagsViewBuilder.buildView(getActivity(), holder.tagsLayout, question.tags);
        setupQuickActionMenu(question, holder);

        return questionRowLayout;
    }

    /* Shouldn't I recycle quick action menu as well? Yes, but how? */
    private void setupQuickActionMenu(final Question question, QuestionViewHolder holder)
    {
        final QuickActionMenu quickActionMenu = initQuickActionMenu(question);
        holder.quickActionImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                quickActionMenu.show(v);
            }
        });
    }

    protected QuickActionMenu initQuickActionMenu(final Question question)
    {
        StackXQuickActionMenu quickActionMenu = new StackXQuickActionMenu(getActivity());
        quickActionMenu.addUserProfileItem(question.owner.id, Html.fromHtml(question.owner.displayName).toString());
        return quickActionMenu.addSimilarQuestionsItem(question.title).addRelatedQuickActionItem(question.id)
                        .addEmailQuickActionItem(question.title, question.body).build();
    }

    protected void setValuesForQuestionView(QuestionViewHolder holder, Question question)
    {
        holder.score.setText(AppUtils.formatNumber(question.score));

        if (question.hasAcceptedAnswer)
        {
            holder.answerCountAnswered.setText(AppUtils.formatNumber(question.answerCount));
            holder.answerCountAnswered.setVisibility(View.VISIBLE);
            holder.answerCount.setVisibility(View.GONE);
        }
        else
        {
            holder.answerCount.setText(AppUtils.formatNumber(question.answerCount));
            holder.answerCount.setVisibility(View.VISIBLE);
            holder.answerCountAnswered.setVisibility(View.GONE);
        }

        holder.title.setText(Html.fromHtml(question.title));
        holder.views.setText("Views:" + AppUtils.formatNumber(question.viewCount));

        if (question.owner.displayName != null)
        {
            holder.owner.setText(DateTimeUtils.getElapsedDurationSince(question.creationDate) + " by "
                            + Html.fromHtml(question.owner.displayName));
        }
    }

    public Bundle getBundle()
    {
        return bundle;
    }
}

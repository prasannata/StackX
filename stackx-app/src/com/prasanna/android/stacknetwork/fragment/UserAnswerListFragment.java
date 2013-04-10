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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StackXQuickActionMenu;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.QuickActionMenu;

public class UserAnswerListFragment extends ItemListFragment<Answer> implements ListItemView<Answer>
{
    private static final String TAG = UserAnswerListFragment.class.getSimpleName();
    private static final int ANSWER_PREVIEW_LEN = 200;
    private static final String ANS_CONTNUES = "...";
    private static final String MULTIPLE_NEW_LINES_AT_END = "[\\n]+$";

    private int page = 1;
    private Intent intent;

    private static class ViewHolder
    {
        TextView itemTitle;
        TextView answerScore;
        TextView answerTime;
        TextView answerBody;
        ImageView acceptedAnswerCue;
        ImageView quickActionMenuImg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (itemsContainer == null)
        {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            itemListAdapter = new ItemListAdapter<Answer>(getActivity(), R.layout.answer_snippet,
                            new ArrayList<Answer>(), this);

            itemsContainer.removeView(itemsContainer.findViewById(R.id.scoreAndAns));
        }
        return itemsContainer;
    }

    @Override
    protected void startIntentService()
    {
        showProgressBar();

        intent = getIntentForService(UserIntentService.class, null);
        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_ANSWERS);
            intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
            intent.putExtra(StringConstants.USER_ID, getActivity().getIntent()
                            .getLongExtra(StringConstants.USER_ID, 0L));
            intent.putExtra(StringConstants.PAGE, page++);
            intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

            startService(intent);
        }
    }

    @Override
    public String getReceiverExtraName()
    {
        return StringConstants.ANSWERS;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        registerForContextMenu(getListView());

        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (itemListAdapter != null && itemListAdapter.getCount() == 0)
            startIntentService();
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }

    @Override
    protected ViewGroup getParentLayout()
    {
        return itemsContainer;
    }

    @Override
    public View getView(final Answer answer, int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;

        if (convertView == null)
        {
            convertView = getActivity().getLayoutInflater().inflate(R.layout.answer_snippet, null);
            viewHolder = new ViewHolder();
            viewHolder.acceptedAnswerCue = (ImageView) convertView.findViewById(R.id.acceptedAnswer);
            viewHolder.itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            viewHolder.answerScore = (TextView) convertView.findViewById(R.id.answerScore);
            viewHolder.answerTime = (TextView) convertView.findViewById(R.id.answerTime);
            viewHolder.answerBody = (TextView) convertView.findViewById(R.id.answerBodyPreview);
            viewHolder.quickActionMenuImg = (ImageView) convertView.findViewById(R.id.itemContextMenu);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        if (answer.accepted)
            viewHolder.acceptedAnswerCue.setVisibility(View.VISIBLE);
        else
            viewHolder.acceptedAnswerCue.setVisibility(View.GONE);

        viewHolder.itemTitle.setText(Html.fromHtml(answer.title));
        viewHolder.answerScore.setText("Answer Score: " + answer.score);
        viewHolder.answerTime.setText(DateTimeUtils.getElapsedDurationSince(answer.creationDate));

        if (answer.body != null)
        {
            String answerBody = answer.body.replaceAll(MULTIPLE_NEW_LINES_AT_END, "\n");

            if (answerBody.length() > ANSWER_PREVIEW_LEN)
            {
                answerBody = answerBody.substring(0, ANSWER_PREVIEW_LEN);
                viewHolder.answerBody.setText(Html.fromHtml(answerBody + ANS_CONTNUES));
            }
            else
                viewHolder.answerBody.setText(Html.fromHtml(answerBody));
        }

        setupQuickActionMenu(answer, viewHolder);
        return convertView;
    }

    /* Shouldn't I recycle quick action menu as well? Yes, but how? */
    private void setupQuickActionMenu(final Answer answer, ViewHolder holder)
    {
        final QuickActionMenu quickActionMenu = initQuickActionMenu(answer);
        holder.quickActionMenuImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                quickActionMenu.show(v);
            }
        });
    }

    protected QuickActionMenu initQuickActionMenu(final Answer answer)
    {
        return new StackXQuickActionMenu(getActivity()).addRelatedQuickActionItem(answer.questionId)
                        .addEmailQuickActionItem(answer.title, answer.body).build();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent intent = new Intent(getActivity(), QuestionActivity.class);
        intent.setAction(StringConstants.QUESTION_ID);
        intent.putExtra(StringConstants.QUESTION_ID, itemListAdapter.getItem(position).questionId);
        startActivity(intent);
    }

    @Override
    protected void loadNextPage()
    {
        startIntentService();
    }
}

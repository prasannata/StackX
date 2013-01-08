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

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.PageSelectAdapter;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.views.HtmlTextView;

public class AnswerFragment extends Fragment
{
    private static final String TAG = AnswerFragment.class.getSimpleName();
    private FrameLayout parentLayout;
    private LinearLayout answerBodyLayout;
    private Answer answer;
    private RelativeLayout answerMetaInfoLayout;
    private ImageView iv;
    private PageSelectAdapter pageSelectAdapter;

    public static AnswerFragment newFragment(Answer answer, PageSelectAdapter pageSelectAdapter)
    {
        AnswerFragment answerFragment = new AnswerFragment();
        answerFragment.setRetainInstance(true);
        answerFragment.setAnswer(answer);
        answerFragment.setPageSelectAdapter(pageSelectAdapter);
        return answerFragment;
    }

    private void setPageSelectAdapter(PageSelectAdapter pageSelectAdapter)
    {
        this.pageSelectAdapter = pageSelectAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (parentLayout == null)
        {
            parentLayout = (FrameLayout) inflater.inflate(R.layout.answer, null);
            answerBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.answerBody);
            answerMetaInfoLayout = (RelativeLayout) parentLayout.findViewById(R.id.answerMetaInfo);
            iv = (ImageView) answerMetaInfoLayout.findViewById(R.id.answerOptionsContextMenu);

            if (answer != null)
                displayAnswer();
        }

        return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(iv);
    }

    private void displayAnswer()
    {
        TextView textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerScore);
        textView.setText(AppUtils.formatNumber(answer.score));

        String acceptRate = answer.owner.acceptRate > 0 ? (answer.owner.acceptRate + "%, ") : "";
        textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerAuthor);
        textView.setText(getAutherDisplayText(acceptRate));

        final ImageView imageView = (ImageView) parentLayout.findViewById(R.id.goBackToQ);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupQuestionTitleAction(imageView);
            }
        });

        iv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AnswerFragment.this.getActivity().openContextMenu(v);
            }
        });

        for (View answerView : MarkdownFormatter.parse(getActivity(), answer.body))
            answerBodyLayout.addView(answerView);
    }

    private void setupQuestionTitleAction(final ImageView questionViewAction)
    {
        final LinearLayout layout = (LinearLayout) parentLayout.findViewById(R.id.qTitleLayout);
        layout.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_in_left));

        HtmlTextView tv = (HtmlTextView) parentLayout.findViewById(R.id.qTitle);
        tv.setText(answer.title);
        tv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pageSelectAdapter.selectQuestionPage();
                layout.setVisibility(View.GONE);
                questionViewAction.setVisibility(View.VISIBLE);
            }
        });

        ImageView ivCloseAction = (ImageView) parentLayout.findViewById(R.id.questionCloseAction);
        ivCloseAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                layout.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                        android.R.anim.slide_out_right));
                layout.setVisibility(View.GONE);
                questionViewAction.setVisibility(View.VISIBLE);
            }
        });

        layout.setVisibility(View.VISIBLE);
        questionViewAction.setVisibility(View.GONE);

    }

    private String getAutherDisplayText(String acceptRate)
    {
        return DateTimeUtils.getElapsedDurationSince(answer.creationDate) + " by "
                + Html.fromHtml(answer.owner.displayName) + " [" + acceptRate
                + AppUtils.formatReputation(answer.owner.reputation) + "]";
    }

    private void setAnswer(Answer answer)
    {
        Log.d(TAG, "answer set");

        this.answer = answer;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        Log.d(TAG, "onCreateContextMenu");

        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.question_context_menu, menu);
        menu.removeItem(R.id.q_ctx_menu_archive);
        menu.removeItem(R.id.q_ctx_related);
        menu.removeItem(R.id.q_ctx_menu_email);
        menu.removeItem(R.id.q_ctx_menu_tags);

        enableCommentsInContextMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.q_ctx_menu_user_profile);
        menuItem.setTitle(Html.fromHtml(answer.owner.displayName) + "'s profile");
    }

    private void enableCommentsInContextMenu(ContextMenu menu)
    {
        MenuItem item = menu.findItem(R.id.q_ctx_comments);
        item.setEnabled(true);
        item.setVisible(true);
    }
}

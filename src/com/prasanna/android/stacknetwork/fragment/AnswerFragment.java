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
import android.text.SpannableString;
import android.text.Spanned;
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
    private ImageView answerCtxMenuImageView;
    private PageSelectAdapter pageSelectAdapter;

    public static AnswerFragment newFragment(Answer answer, PageSelectAdapter pageSelectAdapter)
    {
        AnswerFragment answerFragment = new AnswerFragment();
        answerFragment.setRetainInstance(true);
        answerFragment.answer = answer;
        answerFragment.pageSelectAdapter = pageSelectAdapter;
        return answerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (parentLayout == null)
        {
            parentLayout = (FrameLayout) inflater.inflate(R.layout.answer, null);
            answerBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.answerBody);
            answerMetaInfoLayout = (RelativeLayout) parentLayout.findViewById(R.id.answerMetaInfo);
            answerCtxMenuImageView = (ImageView) answerMetaInfoLayout.findViewById(R.id.answerOptionsContextMenu);
        }

        return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(answerCtxMenuImageView);

        if (answer != null)
            displayAnswer();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        Log.d(TAG, "onCreateContextMenu");

        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.question_context_menu, menu);
        menu.removeItem(R.id.q_ctx_menu_archive);
        menu.removeItem(R.id.q_ctx_similar);
        menu.removeItem(R.id.q_ctx_related);
        menu.removeItem(R.id.q_ctx_menu_email);
        menu.removeItem(R.id.q_ctx_menu_tags);

        enableCommentsInContextMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.q_ctx_menu_user_profile);
        menuItem.setTitle(Html.fromHtml(answer.owner.displayName) + "'s profile");
    }

    private void displayAnswer()
    {
        TextView textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerScore);
        textView.setText(AppUtils.formatNumber(answer.score));
        if (answer.accepted)
            textView.setBackgroundColor(getResources().getColor(R.color.lichen));

        String acceptRate = answer.owner.acceptRate > 0 ? (answer.owner.acceptRate + "%, ") : "";
        textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerAuthor);
        textView.setText(getAutherDisplayText(acceptRate));

        if (answer.comments != null && !answer.comments.isEmpty())
        {
            textView = (TextView) parentLayout.findViewById(R.id.answerCommentsCount);
            textView.setText(getString(R.string.comments) + ":" + String.valueOf(answer.comments.size()));
            textView.setVisibility(View.VISIBLE);
        }

        final ImageView questionMarkImageView = (ImageView) parentLayout.findViewById(R.id.goBackToQ);
        showQuestionTitleOnClick(questionMarkImageView);
        gotoQuestionPageOnLongClick(questionMarkImageView);
        setupContextMenuForAnswer();

        for (View answerView : MarkdownFormatter.parse(getActivity(), answer.body))
            answerBodyLayout.addView(answerView);
    }

    private void setupContextMenuForAnswer()
    {
        answerCtxMenuImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AnswerFragment.this.getActivity().openContextMenu(v);
            }
        });
    }

    private void showQuestionTitleOnClick(final ImageView questionMarkImageView)
    {
        questionMarkImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupQuestionTitleAction(questionMarkImageView);
            }
        });
    }

    private void gotoQuestionPageOnLongClick(final ImageView questionMarkImageView)
    {
        questionMarkImageView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                pageSelectAdapter.selectQuestionPage();
                return true;
            }
        });
    }

    private void setupQuestionTitleAction(final ImageView questionViewAction)
    {
        final LinearLayout layout = (LinearLayout) parentLayout.findViewById(R.id.qTitleLayout);
        layout.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));

        HtmlTextView tv = (HtmlTextView) parentLayout.findViewById(R.id.qTitle);
        tv.setText(answer.title);
        tv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gotoQuestionPage(questionViewAction, layout);
            }
        });

        ImageView ivCloseAction = (ImageView) parentLayout.findViewById(R.id.questionCloseAction);
        ivCloseAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                layout.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right));
                layout.setVisibility(View.GONE);
                questionViewAction.setVisibility(View.VISIBLE);
            }
        });

        layout.setVisibility(View.VISIBLE);
        questionViewAction.setVisibility(View.GONE);

    }

    private String getAutherDisplayText(String acceptRate)
    {
        Spanned authorName = answer.owner.displayName != null ? Html.fromHtml(answer.owner.displayName)
                        : new SpannableString("");
        return DateTimeUtils.getElapsedDurationSince(answer.creationDate) + " by " + authorName + " [" + acceptRate
                        + AppUtils.formatNumber(answer.owner.reputation) + "]";
    }

    private void enableCommentsInContextMenu(ContextMenu menu)
    {
        MenuItem item = menu.findItem(R.id.q_ctx_comments);
        item.setEnabled(true);
        item.setVisible(true);
    }

    private void gotoQuestionPage(final ImageView questionViewAction, final LinearLayout layout)
    {
        pageSelectAdapter.selectQuestionPage();
        layout.setVisibility(View.GONE);
        questionViewAction.setVisibility(View.VISIBLE);
    }
}

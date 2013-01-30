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
import java.util.Iterator;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnCommentChangeListener;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.QuestionsCache;

public class QuestionFragment extends Fragment implements OnCommentChangeListener
{
    private static final String TAG = QuestionFragment.class.getSimpleName();

    private FrameLayout parentLayout;
    private Question question;
    private ContextMenu menu;
    private boolean ctxMenuSetup = false;

    private ImageView backIv;

    private ImageView ctxMenuImage;

    public static QuestionFragment newFragment()
    {
        QuestionFragment questionFragment = new QuestionFragment();
        questionFragment.setRetainInstance(true);
        questionFragment.setHasOptionsMenu(true);
        return questionFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (parentLayout == null)
            createView(inflater);

        return parentLayout;
    }

    private void createView(LayoutInflater inflater)
    {
        parentLayout = (FrameLayout) inflater.inflate(R.layout.question, null);
        ctxMenuImage = (ImageView) parentLayout.findViewById(R.id.questionOptionsContextMenu);
        ctxMenuImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                QuestionFragment.this.getActivity().openContextMenu(v);
            }
        });

        backIv = (ImageView) parentLayout.findViewById(R.id.navigateBack);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        Log.d(TAG, "onCreateContextMenu");

        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.question_context_menu, menu);
        this.menu = menu;

        setupUserProfileInContextMenu();
        setupTagsInContextMenu();
        enableCommentsInContextMenu();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (question != null)
        {
            Log.d(TAG, "Setting action bar title: " + question.title);

            getActivity().getActionBar().setTitle(Html.fromHtml(question.title));
            displayQuestion();
        }

        registerForContextMenu(ctxMenuImage);
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");

        super.onResume();

        displayQuestion();
    }

    private void setupUserProfileInContextMenu()
    {
        if (question != null && question.owner != null)
        {
            MenuItem userProfileMenuItem = menu.findItem(R.id.q_ctx_menu_user_profile);

            if (userProfileMenuItem != null)
                userProfileMenuItem.setTitle(Html.fromHtml(question.owner.displayName) + "'s profile");

            ctxMenuSetup = true;
        }
    }

    private void setupTagsInContextMenu()
    {
        if (question != null && question.tags != null)
        {
            MenuItem menuItem = menu.findItem(R.id.q_ctx_menu_tags);
            SubMenu subMenu = menuItem.getSubMenu();
            for (int idx = 0; idx < question.tags.length; idx++)
                subMenu.add(R.id.qContextTagsMenuGroup, Menu.NONE, idx, question.tags[idx]);

            ctxMenuSetup = ctxMenuSetup & true;
        }
    }

    private void displayQuestion()
    {
        if (question != null)
        {
            if (!ctxMenuSetup && menu != null)
            {
                setupTagsInContextMenu();
                setupUserProfileInContextMenu();
            }

            TextView textView = (TextView) parentLayout.findViewById(R.id.score);
            textView.setText(AppUtils.formatNumber(question.score));

            textView = (TextView) parentLayout.findViewById(R.id.answerCount);
            textView.setText(AppUtils.formatNumber(question.answerCount));

            if (question.hasAcceptedAnswer)
                textView.setBackgroundColor(getResources().getColor(R.color.lichen));

            textView = (TextView) parentLayout.findViewById(R.id.questionTitle);
            textView.setText(Html.fromHtml(question.title));

            String acceptRate = question.owner.acceptRate > 0 ? (question.owner.acceptRate + "%, ") : "";
            textView = (TextView) parentLayout.findViewById(R.id.questionOwner);
            textView.setText(getTimeAndOwnerDisplay(acceptRate));

            textView = (TextView) parentLayout.findViewById(R.id.questionViews);
            textView.setText(getString(R.string.views) + ":" + AppUtils.formatNumber(question.viewCount));

            displayNumComments();

            if (question.body != null)
                displayBody(question.body);
        }
    }

    private String getTimeAndOwnerDisplay(String acceptRate)
    {
        return DateTimeUtils.getElapsedDurationSince(question.creationDate) + " by "
                        + Html.fromHtml(question.owner.displayName) + " [" + acceptRate
                        + AppUtils.formatReputation(question.owner.reputation) + "]";
    }

    private void displayNumComments()
    {
        if (question.comments != null)
        {
            TextView textView = (TextView) parentLayout.findViewById(R.id.questionComments);
            textView.setText(getString(R.string.comments) + ":" + String.valueOf(question.comments.size()));
            textView.setVisibility(View.VISIBLE);

            enableCommentsInContextMenu();
        }
    }

    private void enableCommentsInContextMenu()
    {
        if (menu != null)
        {
            MenuItem item = menu.findItem(R.id.q_ctx_comments);
            item.setEnabled(true);
            item.setVisible(true);
        }
    }

    public void displayBody(String text)
    {
        if (text != null && parentLayout != null)
        {
            final LinearLayout questionBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.questionBody);
            questionBodyLayout.removeAllViews();
            for (final View questionBodyTextView : MarkdownFormatter.parse(getActivity(), text))
                questionBodyLayout.addView(questionBodyTextView);
        }
    }

    public void setQuestion(Question question)
    {
        this.question = question;
    }

    public void setComments(ArrayList<Comment> comments)
    {
        question.comments = comments;
        if (parentLayout != null)
            displayNumComments();
    }

    public void setAndDisplay(Question question)
    {
        setQuestion(question);

        if (parentLayout != null)
        {
            if (isAdded())
                getActivity().getActionBar().setTitle(Html.fromHtml(question.title));

            displayQuestion();
        }
    }

    public void enableNavigationBack(OnClickListener clickListener)
    {
        backIv.setVisibility(View.VISIBLE);
        backIv.setOnClickListener(clickListener);
    }

    @Override
    public void onCommentUpdate(Comment comment)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCommentDelete(Comment comment)
    {
        if (question.comments != null)
        {
            Log.d(TAG, "Removing comment: " + comment.id);

            Iterator<Comment> iterator = question.comments.iterator();
            while (iterator.hasNext())
            {
                if (iterator.next().id == comment.id)
                {
                    Log.d(TAG, "comment " + comment.id + " removed");
                    iterator.remove();
                    break;
                }
            }

            updateCacheIfNeeded();
        }

        displayNumComments();
    }

    private void updateCacheIfNeeded()
    {
        if (QuestionsCache.getInstance().containsKey(question.id))
        {
            Question cachedQuestion = QuestionsCache.getInstance().get(question.id);
            cachedQuestion.comments = question.comments;
            QuestionsCache.getInstance().add(question.id, cachedQuestion);
        }
    }
}

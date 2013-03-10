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
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;
import com.prasanna.android.utils.TagsViewBuilder;

public class QuestionFragment extends Fragment implements OnCommentChangeListener
{
    private static final String TAG = QuestionFragment.class.getSimpleName();

    private boolean ctxMenuSetup = false;

    private FrameLayout parentLayout;
    private Question question;
    private ContextMenu menu;
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
        if (parentLayout == null)
            createView(inflater);

        if (savedInstanceState != null)
            question = (Question) savedInstanceState.getSerializable(StringConstants.QUESTION);

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
            LogWrapper.d(TAG, "Setting action bar title: " + question.title);

            getActivity().getActionBar().setTitle(Html.fromHtml(question.title));
            displayQuestion();
        }

        registerForContextMenu(ctxMenuImage);
    }

    @Override
    public void onResume()
    {
        LogWrapper.d(TAG, "onResume");

        super.onResume();

        displayQuestion();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        LogWrapper.d(TAG, "onSaveInstanceState");

        if (question != null)
            outState.putSerializable(StringConstants.QUESTION, question);

        super.onSaveInstanceState(outState);
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

            setupTextViewForAnswerCount();

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

            TagsViewBuilder.buildView(getActivity(), (LinearLayout) parentLayout.findViewById(R.id.questionTags),
                            question.tags);
        }
    }

    private void setupTextViewForAnswerCount()
    {
        TextView answerCountView = (TextView) parentLayout.findViewById(R.id.answerCount);
        TextView answerCountAnsweredView = (TextView) parentLayout.findViewById(R.id.answerCountAnswered);

        if (question.hasAcceptedAnswer)
        {
            answerCountAnsweredView.setVisibility(View.VISIBLE);
            answerCountAnsweredView.setText(AppUtils.formatNumber(question.answerCount));

            answerCountView = (TextView) parentLayout.findViewById(R.id.answerCount);
            answerCountView.setVisibility(View.GONE);
        }
        else
        {
            answerCountView.setVisibility(View.VISIBLE);
            answerCountView.setText(AppUtils.formatNumber(question.answerCount));

            answerCountAnsweredView = (TextView) parentLayout.findViewById(R.id.answerCountAnswered);
            answerCountAnsweredView.setVisibility(View.GONE);
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
            textView.setVisibility(question.comments.isEmpty() ? View.GONE : View.VISIBLE);

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
            question.body = text;

            final LinearLayout questionBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.questionBody);
            if (isVisible() && questionBodyLayout != null)
            {
                questionBodyLayout.removeAllViews();
                ArrayList<View> views = MarkdownFormatter.parse(getActivity(), question.body);

                if (views != null)
                {
                    for (final View questionBodyTextView : views)
                        questionBodyLayout.addView(questionBodyTextView);
                }
            }
        }
    }

    public void setQuestion(Question question)
    {
        this.question = question;
    }

    public void setComments(ArrayList<Comment> comments)
    {
        if (question != null)
        {
            question.comments = comments;
            if (parentLayout != null)
                displayNumComments();
        }
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
    public void onCommentAdd(Comment comment)
    {
        // One reply to comments come here, add new comment is directly handled
        // by QuestionAcitivy. So no need to initialize comments if nul because
        // it can never be null here.
        if (comment != null)
        {
            if (question.comments == null)
                question.comments = new ArrayList<Comment>();

            question.comments.add(comment);
            updateCacheWithNewCommentIfExists(comment);
        }
    }

    @Override
    public void onCommentUpdate(Comment comment)
    {
        if (question.comments != null)
        {
            LogWrapper.d(TAG, "Removing comment: " + comment.id);

            Iterator<Comment> iterator = question.comments.iterator();
            while (iterator.hasNext())
            {
                if (iterator.next().id == comment.id)
                {
                    LogWrapper.d(TAG, "comment " + comment.id + " edited");
                    removeQuestionFromCache();
                    break;
                }
            }

            updateCacheIfNeeded();
        }
    }

    @Override
    public void onCommentDelete(long commentId)
    {
        if (question.comments != null)
        {
            LogWrapper.d(TAG, "Removing comment: " + commentId);

            Iterator<Comment> iterator = question.comments.iterator();
            while (iterator.hasNext())
            {
                if (iterator.next().id == commentId)
                {
                    LogWrapper.d(TAG, "comment " + commentId + " removed");
                    iterator.remove();
                    break;
                }
            }

            updateCacheIfNeeded();
        }

        displayNumComments();
    }

    private void updateCacheWithNewCommentIfExists(Comment comment)
    {
        if (QuestionsCache.getInstance().containsKey(question.id))
        {
            Question cachedQuestion = QuestionsCache.getInstance().get(question.id);

            if (cachedQuestion.comments == null)
                cachedQuestion.comments = new ArrayList<Comment>();

            if (!cachedQuestion.comments.contains(comment))
            {
                cachedQuestion.comments.add(comment);
                QuestionsCache.getInstance().add(question.id, cachedQuestion);
            }
        }
    }

    private void updateCacheIfNeeded()
    {
        if (QuestionsCache.getInstance().containsKey(question.id))
        {
            Question cachedQuestion = QuestionsCache.getInstance().get(question.id);
            if (cachedQuestion != null)
            {
                cachedQuestion.comments = question.comments;
                QuestionsCache.getInstance().add(question.id, cachedQuestion);
            }
            else
                QuestionsCache.getInstance().remove(question.id);
        }
    }

    private void removeQuestionFromCache()
    {
        if (QuestionsCache.getInstance().containsKey(question.id))
            QuestionsCache.getInstance().remove(question.id);
    }

}

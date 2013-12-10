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
import java.util.Iterator;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
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
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnCommentChangeListener;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnShowCommentsListener;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.QuestionsCache;
import com.prasanna.android.stacknetwork.utils.StackXQuickActionMenu;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;
import com.prasanna.android.views.HtmlTextView;

public class AnswerFragment extends Fragment implements OnCommentChangeListener {
  private static final String TAG = AnswerFragment.class.getSimpleName();
  private FrameLayout parentLayout;
  private LinearLayout answerBodyLayout;
  private Answer answer;
  private RelativeLayout answerMetaInfoLayout;
  private ImageView answerCtxMenuImageView;
  private PageSelectAdapter pageSelectAdapter;
  private StackXQuickActionMenu quickActionMenu;
  private OnShowCommentsListener onShowCommentsListener;

  public static AnswerFragment newFragment(Answer answer, int viewPageNumber, PageSelectAdapter pageSelectAdapter) {
    AnswerFragment answerFragment = new AnswerFragment();
    answerFragment.setRetainInstance(true);
    answerFragment.answer = answer;
    answerFragment.pageSelectAdapter = pageSelectAdapter;
    return answerFragment;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (!(activity instanceof OnShowCommentsListener))
      throw new IllegalArgumentException("Activity must implement OnShowCommentsListener");

    onShowCommentsListener = (OnShowCommentsListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (parentLayout == null) {
      parentLayout = (FrameLayout) inflater.inflate(R.layout.answer, null);
      answerBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.answerBody);
      answerMetaInfoLayout = (RelativeLayout) parentLayout.findViewById(R.id.answerMetaInfo);
      answerCtxMenuImageView = (ImageView) answerMetaInfoLayout.findViewById(R.id.answerOptionsContextMenu);
    }

    if (savedInstanceState != null)
      answer = (Answer) savedInstanceState.getSerializable(StringConstants.ANSWER);

    return parentLayout;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    registerForContextMenu(answerCtxMenuImageView);
  }

  @Override
  public void onResume() {
    super.onResume();

    displayAnswer();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (answer != null)
      outState.putSerializable(StringConstants.ANSWER, answer);

    super.onSaveInstanceState(outState);
  }

  private void displayAnswer() {
    if (answer != null) {
      if (quickActionMenu == null)
        quickActionMenu = initQuickActionMenu();

      TextView textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerScore);
      textView.setText(AppUtils.formatNumber(answer.score));
      if (answer.accepted)
        textView.setBackgroundResource(R.drawable.ans_score_answered_padded);

      String acceptRate = answer.owner.acceptRate > 0 ? (answer.owner.acceptRate + "%, ") : "";
      textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerAuthor);
      textView.setText(getAutherDisplayText(acceptRate));

      displayNumComments();

      final ImageView questionMarkImageView = (ImageView) parentLayout.findViewById(R.id.goBackToQ);
      showQuestionTitleOnClick(questionMarkImageView);
      gotoQuestionPageOnLongClick(questionMarkImageView);
      setupQuickActionMenuForAnswer();

      answerBodyLayout.removeAllViews();

      for (View answerView : MarkdownFormatter.parse(getActivity(), answer.body))
        answerBodyLayout.addView(answerView);
    }
  }

  private StackXQuickActionMenu initQuickActionMenu() {
    StackXQuickActionMenu quickActionMenu = new StackXQuickActionMenu(getActivity());
    return quickActionMenu.addCommentsItem(onShowCommentsListener).addUserProfileItem(answer.owner.id,
        Html.fromHtml(answer.owner.displayName).toString());
  }

  private void displayNumComments() {
    TextView textView = (TextView) parentLayout.findViewById(R.id.answerCommentsCount);

    if (answer.comments != null && !answer.comments.isEmpty()) {
      textView.setText(getString(R.string.comments) + ":" + String.valueOf(answer.comments.size()));
      textView.setVisibility(View.VISIBLE);
    }
    else
      textView.setVisibility(View.GONE);
  }

  private void setupQuickActionMenuForAnswer() {
    answerCtxMenuImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        quickActionMenu.build().show(v);
      }
    });
  }

  private void showQuestionTitleOnClick(final ImageView questionMarkImageView) {
    questionMarkImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setupQuestionTitleAction(questionMarkImageView);
      }
    });
  }

  private void gotoQuestionPageOnLongClick(final ImageView questionMarkImageView) {
    questionMarkImageView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        pageSelectAdapter.selectQuestionPage();
        return true;
      }
    });
  }

  private void setupQuestionTitleAction(final ImageView questionViewAction) {
    final LinearLayout layout = (LinearLayout) parentLayout.findViewById(R.id.qTitleLayout);
    layout.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));

    HtmlTextView tv = (HtmlTextView) parentLayout.findViewById(R.id.qTitle);
    tv.setText(answer.title);
    tv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        gotoQuestionPage(questionViewAction, layout);
      }
    });

    ImageView ivCloseAction = (ImageView) parentLayout.findViewById(R.id.questionCloseAction);
    ivCloseAction.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        layout.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right));
        layout.setVisibility(View.GONE);
        questionViewAction.setVisibility(View.VISIBLE);
      }
    });

    layout.setVisibility(View.VISIBLE);
    questionViewAction.setVisibility(View.GONE);

  }

  private String getAutherDisplayText(String acceptRate) {
    Spanned authorName =
        answer.owner.displayName != null ? Html.fromHtml(answer.owner.displayName) : new SpannableString("");
    return DateTimeUtils.getElapsedDurationSince(answer.creationDate) + " by " + authorName + " [" + acceptRate
        + AppUtils.formatReputation(answer.owner.reputation) + "]";
  }

  private void gotoQuestionPage(final ImageView questionViewAction, final LinearLayout layout) {
    pageSelectAdapter.selectQuestionPage();
    layout.setVisibility(View.GONE);
    questionViewAction.setVisibility(View.VISIBLE);
  }

  @Override
  public void onCommentUpdate(Comment comment) {
    if (answer.comments != null) {
      LogWrapper.d(TAG, "Removing comment: " + comment.id);

      Iterator<Comment> iterator = answer.comments.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().id == comment.id) {
          LogWrapper.d(TAG, "comment " + comment.id + " removed");
          removeQuestionFromCache();
          break;
        }
      }
    }
  }

  @Override
  public void onCommentDelete(long commentId) {
    if (answer.comments != null) {
      LogWrapper.d(TAG, "Removing comment: " + commentId);

      Iterator<Comment> iterator = answer.comments.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().id == commentId) {
          LogWrapper.d(TAG, "comment " + commentId + " removed");
          removeQuestionFromCache();
          iterator.remove();
          break;
        }
      }
    }

    displayNumComments();
  }

  private void removeQuestionFromCache() {
    if (QuestionsCache.getInstance().containsKey(answer.questionId))
      QuestionsCache.getInstance().remove(answer.questionId);
  }

  @Override
  public void onCommentAdd(Comment comment) {
    // One reply to comments come here, add new comment is directly handled
    // by QuestionAcitivy. So no need to initialize comments if null because
    // it can never be null here.
    if (comment != null) {
      if (answer.comments == null)
        answer.comments = new ArrayList<Comment>();

      answer.comments.add(comment);
      updateCacheWithNewCommentIfExists(comment);
    }

    displayNumComments();
  }

  private void updateCacheWithNewCommentIfExists(Comment comment) {
    if (QuestionsCache.getInstance().containsKey(answer.questionId)) {
      Question cachedQuestion = QuestionsCache.getInstance().get(answer.questionId);
      for (Answer answer : cachedQuestion.answers) {
        if (answer.id == comment.post_id) {
          if (answer.comments == null)
            answer.comments = new ArrayList<Comment>();

          if (!answer.comments.contains(comment)) {
            answer.comments.add(comment);
            QuestionsCache.getInstance().add(answer.questionId, cachedQuestion);
          }
          break;
        }

      }
    }
  }
}

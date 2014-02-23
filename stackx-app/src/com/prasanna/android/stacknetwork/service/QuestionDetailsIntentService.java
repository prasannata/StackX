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

package com.prasanna.android.stacknetwork.service;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Post.PostType;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.utils.QuestionsCache;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class QuestionDetailsIntentService extends AbstractIntentService {
  private static final String TAG = QuestionDetailsIntentService.class.getSimpleName();

  private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

  public static final int RESULT_CODE_Q = 0x01;
  public static final int RESULT_CODE_Q_BODY = 0x02;
  public static final int RESULT_CODE_Q_COMMENTS = 0x03;
  public static final int RESULT_CODE_ANSWERS = 0x04;
  public static final int RESULT_CODE_Q_CACHED = 0x05;
  public static final int RESULT_CODE_FAVORITE_SUCCESS = 0x10;
  public static final int RESULT_CODE_FAVORITE_UNDO_SUCCESS = 0x11;

  public static final int QA = 0x1001;
  public static final int FAVORITE = 0x1002;
  public static final int FAVORITE_UNDO = 0x1003;

  public QuestionDetailsIntentService() {
    this(TAG);
  }

  public QuestionDetailsIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
    final int action = intent.getIntExtra(StringConstants.ACTION, -1);
    final long questionId = intent.getLongExtra(StringConstants.ID, 0);
    final String site = intent.getStringExtra(StringConstants.SITE);

    try {
      switch (action) {
        case QA:
          getQuestion(intent, receiver);
          break;
        case VotingActions.UPVOTE:
          sendSerializable(receiver, VotingActions.RESULT_CODE_UPVOTE_SUCCESS, StringConstants.SCORE,
              questionService.upvote(PostType.QUESTION, site, questionId));
          break;
        case VotingActions.UPVOTE_UNDO:
          sendSerializable(receiver, VotingActions.RESULT_CODE_UPVOTE_UNDO_SUCCESS, StringConstants.SCORE,
              questionService.undoUpvote(PostType.QUESTION, site, questionId));
          break;
        case VotingActions.DOWNVOTE:
          sendSerializable(receiver, VotingActions.RESULT_CODE_DOWNVOTE_SUCCESS, StringConstants.SCORE,
              questionService.downvote(PostType.QUESTION, site, questionId));
          break;
        case VotingActions.DOWNVOTE_UNDO:
          sendSerializable(receiver, VotingActions.RESULT_CODE_DOWNVOTE_UNDO_SUCCESS, StringConstants.SCORE,
              questionService.undoDownvote(PostType.QUESTION, site, questionId));
          break;
        case VotingActions.UPVOTE_UNDO_DOWNVOTE:
          questionService.undoUpvote(PostType.QUESTION, site, questionId);
          sendSerializable(receiver, VotingActions.RESULT_CODE_UPVOTE_UNDO_DOWNVOTE_SUCCESS, StringConstants.SCORE,
              questionService.downvote(PostType.QUESTION, site, questionId));
          break;
        case VotingActions.DOWNVOTE_UNDO_UPVOTE:
          questionService.undoDownvote(PostType.QUESTION, site, questionId);
          sendSerializable(receiver, VotingActions.RESULT_CODE_DOWNVOTE_UNDO_UPVOTE_SUCCESS, StringConstants.SCORE,
              questionService.upvote(PostType.QUESTION, site, questionId));
          break;

        case FAVORITE:
          questionService.favorite(site, questionId);
          sendSerializable(receiver, RESULT_CODE_FAVORITE_SUCCESS, null, null);
          break;
        case FAVORITE_UNDO:
          questionService.undoFavorite(site, questionId);
          sendSerializable(receiver, RESULT_CODE_FAVORITE_UNDO_SUCCESS, null, null);
          break;
        default:
          sendSerializable(receiver, ERROR, "reason", "unrecognizable action");
          break;
      }

    } catch (AbstractHttpException e) {
      Bundle bundle = new Bundle();
      bundle.putSerializable(StringConstants.EXCEPTION, e);
      receiver.send(ERROR, bundle);
    }
  }

  private void getQuestion(Intent intent, ResultReceiver receiver) {
    final String action = intent.getAction();

    if (action != null) {
      if (action.equals(StringConstants.ANSWERS)) {
        long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0L);
        int page = intent.getIntExtra(StringConstants.PAGE, 0);
        final String site = intent.getStringExtra(StringConstants.SITE);

        if (questionId > 0 && page > 0) getAnswersForQuestion(receiver, site, questionId, page);
      } else {
        getQuestionDetail(receiver, intent);
      }
    }
  }

  private void getAnswersForQuestion(final ResultReceiver receiver, String site, long questionId, int page) {
    ArrayList<Answer> answers = getAnswersAndSend(receiver, site, questionId, page);

    QuestionsCache.getInstance().updateAnswersForQuestion(questionId, answers);
  }

  private void getQuestionDetail(ResultReceiver receiver, Intent intent) {
    long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);
    final String site = intent.getStringExtra(StringConstants.SITE);

    Question question = null;

    if (!intent.getBooleanExtra(StringConstants.REFRESH, false))
      question = QuestionsCache.getInstance().get(questionId);

    if (question != null) {
      LogWrapper.d(TAG, "Question " + questionId + " recovered from cache.");
      sendSerializable(receiver, RESULT_CODE_Q_CACHED, StringConstants.QUESTION, question);
    } else {
      if (StringConstants.QUESTION.equals(intent.getAction())) {
        question = (Question) intent.getSerializableExtra(StringConstants.QUESTION);
        getQuestionAndAnswers(site, receiver, question);
      } else {
        LogWrapper.d(TAG, "Get question for " + questionId + " in " + site);
        question = getQuestionMetaAndBodyAndSend(site, receiver, questionId);
      }

      QuestionsCache.getInstance().add(question.id, question);
    }
  }

  private void getQuestionAndAnswers(String site, ResultReceiver receiver, Question question) {
    question.body = questionService.getQuestionBodyForId(question.id);
    sendSerializable(receiver, RESULT_CODE_Q_BODY, StringConstants.BODY, question.body);

    getCommentsAndSend(site, receiver, question);

    if (question.answerCount > 0) question.answers = getAnswersAndSend(receiver, site, question.id, 1);
  }

  private void getCommentsAndSend(String site, ResultReceiver receiver, Question question) {
    try {
      StackXPage<Comment> commentsPage =
          questionService.getComments(StringConstants.QUESTIONS, site, String.valueOf(question.id), 1);
      if (commentsPage != null) {
        question.comments = commentsPage.items;
        sendSerializable(receiver, RESULT_CODE_Q_COMMENTS, StringConstants.COMMENTS, question.comments);
      }
    } catch (AbstractHttpException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
  }

  private Question getQuestionMetaAndBodyAndSend(String site, ResultReceiver receiver, long questionId) {
    Question question = questionService.getQuestionFullDetails(questionId, site);
    sendSerializable(receiver, RESULT_CODE_Q, StringConstants.QUESTION, question);
    getCommentsAndSend(site, receiver, question);
    return question;
  }

  private ArrayList<Answer> getAnswersAndSend(final ResultReceiver receiver, String site, long questionId, int page) {
    ArrayList<Answer> answers = questionService.getAnswersForQuestion(questionId, site, page);
    sendSerializable(receiver, RESULT_CODE_ANSWERS, StringConstants.ANSWERS, answers);
    return answers;
  }

  private void sendSerializable(ResultReceiver receiver, int resultCode, String key, Serializable value) {
    Bundle bundle = new Bundle();
    if (key != null && value != null) bundle.putSerializable(key, value);
    receiver.send(resultCode, bundle);
  }

}

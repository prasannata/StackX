/*
    Copyright (C) 2014 Prasanna Thirumalai
    
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

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.Post.PostType;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class AnswersIntentService extends AbstractIntentService {
  private static final String TAG = AnswersIntentService.class.getSimpleName();
  public static final int GET_ANSWER = 0x601;
  public static final int GET_ANSWERS = 0x602;
  public static final int ACCEPT = 0x607;
  public static final int ACCEPT_UNDO = 0x608;

  public static final int RESULT_CODE_ACCEPT_ANS_SUCCESS = 0x05;
  public static final int RESULT_CODE_ACCEPT_ANS_UNDO_SUCCESS = 0x06;

  private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

  public AnswersIntentService() {
    this("UserQuestionsService");
  }

  public AnswersIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
    final int action = intent.getIntExtra(StringConstants.ACTION, -1);
    final long id = intent.getLongExtra(StringConstants.ID, -1);
    final String site = intent.getStringExtra(StringConstants.SITE);
    Bundle bundle = new Bundle();

    try {
      super.onHandleIntent(intent);

      switch (action) {
        case GET_ANSWER:
          bundle.putSerializable(StringConstants.ANSWER, questionService.getAnswer(id, site));
          receiver.send(GET_ANSWER, bundle);
          break;
        case VotingActions.UPVOTE:
          bundle.putInt(StringConstants.SCORE, questionService.upvote(PostType.ANSWER, site, id));
          receiver.send(VotingActions.RESULT_CODE_UPVOTE_SUCCESS, bundle);
          break;
        case VotingActions.UPVOTE_UNDO:
          bundle.putInt(StringConstants.SCORE, questionService.undoUpvote(PostType.ANSWER, site, id));
          receiver.send(VotingActions.RESULT_CODE_UPVOTE_UNDO_SUCCESS, bundle);
          break;
        case VotingActions.DOWNVOTE:
          bundle.putInt(StringConstants.SCORE, questionService.downvote(PostType.ANSWER, site, id));
          receiver.send(VotingActions.RESULT_CODE_DOWNVOTE_SUCCESS, bundle);
          break;
        case VotingActions.DOWNVOTE_UNDO:
          bundle.putInt(StringConstants.SCORE, questionService.undoDownvote(PostType.ANSWER, site, id));
          receiver.send(VotingActions.RESULT_CODE_DOWNVOTE_UNDO_SUCCESS, bundle);
          break;
        case VotingActions.UPVOTE_UNDO_DOWNVOTE:
          questionService.undoUpvote(PostType.ANSWER, site, id);
          bundle.putInt(StringConstants.SCORE, questionService.downvote(PostType.ANSWER, site, id));
          receiver.send(VotingActions.RESULT_CODE_UPVOTE_UNDO_DOWNVOTE_SUCCESS, bundle);
          break;
        case VotingActions.DOWNVOTE_UNDO_UPVOTE:
          questionService.undoDownvote(PostType.ANSWER, site, id);
          bundle.putInt(StringConstants.SCORE, questionService.upvote(PostType.ANSWER, site, id));
          receiver.send(VotingActions.RESULT_CODE_DOWNVOTE_UNDO_UPVOTE_SUCCESS, bundle);
          break;
        case ACCEPT:
          questionService.acceptAnswer(id, site);
          receiver.send(RESULT_CODE_ACCEPT_ANS_SUCCESS, bundle);
          break;
        case ACCEPT_UNDO:
          questionService.undoAcceptAnswer(id, site);
          receiver.send(RESULT_CODE_ACCEPT_ANS_UNDO_SUCCESS, bundle);
          break;
        default:
          LogWrapper.d(TAG, "Unknown action: " + action);
          break;
      }
    } catch (AbstractHttpException e) {
      bundle.putSerializable(StringConstants.EXCEPTION, e);
      receiver.send(ERROR, bundle);
    }
  }
}

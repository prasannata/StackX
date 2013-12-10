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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class QuestionsIntentService extends AbstractIntentService {
  private static final String TAG = QuestionsIntentService.class.getSimpleName();
  public static final int GET_FRONT_PAGE = 0x501;
  public static final int GET_FAQ_FOR_TAG = 0x502;
  public static final int GET_RELATED = 0x503;
  public static final int GET_QUESTIONS_FOR_TAG = 0x504;
  public static final int GET_SIMILAR = 0x505;
  public static final int SEARCH = 0x506;
  public static final int SEARCH_ADVANCED = 0x507;

  private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

  public QuestionsIntentService() {
    this("UserQuestionsService");
  }

  public QuestionsIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
    final int action = intent.getIntExtra(StringConstants.ACTION, -1);
    final int page = intent.getIntExtra(StringConstants.PAGE, 1);
    final String sort = intent.getStringExtra(StringConstants.SORT);
    Bundle bundle = new Bundle();

    try {
      super.onHandleIntent(intent);

      switch (action) {
        case GET_FRONT_PAGE:
          LogWrapper.d(TAG, "Get front page");
          bundle.putSerializable(StringConstants.QUESTIONS, questionService.getAllQuestions(sort, page));
          break;
        case GET_FAQ_FOR_TAG:
          String tag = intent.getStringExtra(StringConstants.TAG);
          LogWrapper.d(TAG, "Get FAQ for " + tag);
          bundle.putSerializable(StringConstants.QUESTIONS, questionService.getFaqForTag(tag, page));
          break;
        case GET_RELATED:
          LogWrapper.d(TAG, "Get related questions");
          getRelatedQuestions(intent, page, bundle);
          break;
        case GET_QUESTIONS_FOR_TAG:
          String seachTagged = intent.getStringExtra(StringConstants.TAG);
          LogWrapper.d(TAG, "Get questions for " + seachTagged);
          bundle
              .putSerializable(StringConstants.QUESTIONS, questionService.getQuestionsForTag(seachTagged, sort, page));
          break;
        case GET_SIMILAR:
          String title = intent.getStringExtra(StringConstants.TITLE);
          LogWrapper.d(TAG, "Get questions similar to " + title);
          bundle.putSerializable(StringConstants.QUESTIONS, questionService.getSimilar(title, page));
          break;
        case SEARCH:
          String query = intent.getStringExtra(SearchManager.QUERY);
          LogWrapper.d(TAG, "Received search query: " + query);
          bundle.putSerializable(StringConstants.QUESTIONS, questionService.search(query, page));
          break;
        case SEARCH_ADVANCED:
          SearchCriteria criteria = (SearchCriteria) intent.getSerializableExtra(StringConstants.SEARCH_CRITERIA);
          bundle.putSerializable(StringConstants.QUESTIONS, questionService.advancedSearch(criteria));
          break;

        default:
          LogWrapper.d(TAG, "Unknown action: " + action);
          break;
      }

      receiver.send(0, bundle);
    }
    catch (AbstractHttpException e) {
      bundle.putSerializable(StringConstants.EXCEPTION, e);
      receiver.send(ERROR, bundle);
    }
  }

  private void getRelatedQuestions(Intent intent, final int page, Bundle bundle) {
    long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);
    if (questionId > 0)
      bundle.putSerializable(StringConstants.QUESTIONS, questionService.getRelatedQuestions(questionId, page));
  }
}

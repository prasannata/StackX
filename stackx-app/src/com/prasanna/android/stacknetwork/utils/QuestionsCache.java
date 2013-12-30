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

package com.prasanna.android.stacknetwork.utils;

import java.util.ArrayList;

import com.prasanna.android.cache.LruCache;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;

public class QuestionsCache extends LruCache<Long, Question> {
  private static int CACHE_SIZE = 15;
  private static final QuestionsCache INSTANCE = new QuestionsCache();

  private QuestionsCache() {
    super(CACHE_SIZE);
  }

  public static QuestionsCache getInstance() {
    return INSTANCE;
  }

  public void updateAnswersForQuestion(Long questionId, ArrayList<Answer> answers) {
    if (answers != null && containsKey(questionId)) {
      Question question = get(questionId);
      if (question != null) {
        if (question.answers == null) {
          question.answers = new ArrayList<Answer>();
        }

        question.answers.addAll(answers);
        add(questionId, question);
      }
    }
  }
}

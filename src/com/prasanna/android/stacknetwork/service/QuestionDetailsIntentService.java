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

package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.QuestionsCache;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionDetailsIntentService extends AbstractIntentService
{
    private static final String TAG = QuestionDetailsIntentService.class.getSimpleName();

    private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

    public static final int RESULT_CODE_Q = 0x01;
    public static final int RESULT_CODE_Q_BODY = 0x02;
    public static final int RESULT_CODE_Q_COMMENTS = 0x03;
    public static final int RESULT_CODE_Q_ANSWERS = 0x04;

    public QuestionDetailsIntentService()
    {
        this(TAG);
    }

    public QuestionDetailsIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(TAG, "Action: " + intent.getAction());

        try
        {
            handleIntent(intent);
        }
        catch (HttpErrorException e)
        {
            broadcastHttpErrorIntent(e.getError());
        }
    }

    private void handleIntent(Intent intent)
    {
        final String action = intent.getAction();
        final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);

        if (action != null)
        {
            if (action.equals(StringConstants.ANSWERS))
            {
                long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0L);
                int page = intent.getIntExtra(StringConstants.PAGE, 0);

                if (questionId > 0 && page > 0)
                {
                    ArrayList<Answer> answers = questionService.getAnswersForQuestion(questionId,
                            page);
                    if (answers != null)
                    {
                        Bundle bundle = new Bundle();
                        QuestionsCache.getInstance().updateAnswersForQuestion(questionId, answers);
                        bundle.putSerializable(StringConstants.ANSWERS, answers);
                        receiver.send(RESULT_CODE_Q_ANSWERS, bundle);
                    }
                }
            }
            else
            {

                getQuestionDetail(receiver, intent);
            }
        }
    }

    private void getQuestionDetail(ResultReceiver receiver, Intent intent)
    {
        Bundle bundle = new Bundle();
        Question cachedQuestion = null;
        long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);

        if (StringConstants.QUESTION.equals(intent.getAction()))
            cachedQuestion = QuestionsCache.getInstance().get(questionId);

        if (cachedQuestion != null)
        {
            Log.d(TAG, "Question " + questionId + " recovered from cache.");
            bundle.putSerializable(StringConstants.QUESTION, cachedQuestion);
            receiver.send(RESULT_CODE_Q, bundle);
        }
        else
        {
            if (StringConstants.QUESTION.equals(intent.getAction()))
            {
                bundle.putString(StringConstants.BODY,
                        questionService.getQuestionBodyForId(questionId));
                receiver.send(RESULT_CODE_Q_BODY, bundle);

                getCommentsAndSend(receiver, bundle, questionId);

                if (intent.getIntExtra(StringConstants.ANSWER_COUNT, 0) > 0)
                    getAnswersAndSend(receiver, bundle, questionId);
            }
            else
            {
                bundle.putSerializable(StringConstants.QUESTION,
                        questionService.getQuestionFullDetails(questionId));
                receiver.send(RESULT_CODE_Q, bundle);
            }
        }
    }

    public void getAnswersAndSend(ResultReceiver receiver, Bundle bundle, long questionId)
    {
        bundle.putSerializable(StringConstants.ANSWERS,
                questionService.getAnswersForQuestion(questionId, 1));
        receiver.send(RESULT_CODE_Q_ANSWERS, bundle);
    }

    public void getCommentsAndSend(ResultReceiver receiver, Bundle bundle, long questionId)
    {
        bundle.putSerializable(StringConstants.COMMENTS,
                questionService.getComments(StringConstants.QUESTIONS, String.valueOf(questionId)));
        receiver.send(RESULT_CODE_Q_COMMENTS, bundle);
    }
}

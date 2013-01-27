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

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.prasanna.android.http.ServerException;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.utils.QuestionsCache;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionDetailsIntentService extends AbstractIntentService
{
    private static final String TAG = QuestionDetailsIntentService.class.getSimpleName();

    private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

    public static final int RESULT_CODE_Q = 0x01;
    public static final int RESULT_CODE_Q_BODY = 0x02;
    public static final int RESULT_CODE_Q_COMMENTS = 0x03;
    public static final int RESULT_CODE_ANSWERS = 0x04;
    public static final int RESULT_CODE_Q_CACHED = 0x05;

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
        final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);

        try
        {
            handleIntent(intent, receiver);
        }
        catch (ServerException e)
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable(StringConstants.EXCEPTION, e);
            receiver.send(ERROR, bundle);
        }
    }

    private void handleIntent(Intent intent, ResultReceiver receiver)
    {
        final String action = intent.getAction();

        if (action != null)
        {
            if (action.equals(StringConstants.ANSWERS))
            {
                long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0L);
                int page = intent.getIntExtra(StringConstants.PAGE, 0);

                if (questionId > 0 && page > 0)
                    getAnswersForQuestion(receiver, questionId, page);
            }
            else
            {
                getQuestionDetail(receiver, intent);
            }
        }
    }

    private void getAnswersForQuestion(final ResultReceiver receiver, long questionId, int page)
    {
        ArrayList<Answer> answers = getAnswersAndSend(receiver, questionId, page);

        QuestionsCache.getInstance().updateAnswersForQuestion(questionId, answers);
    }

    private void getQuestionDetail(ResultReceiver receiver, Intent intent)
    {
        long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);
        Question question = null;

        if (!intent.getBooleanExtra(StringConstants.REFRESH, false))
            question = QuestionsCache.getInstance().get(questionId);

        if (question != null)
        {
            Log.d(TAG, "Question " + questionId + " recovered from cache.");
            sendSerializable(receiver, RESULT_CODE_Q_CACHED, StringConstants.QUESTION, question);
        }
        else
        {
            if (StringConstants.QUESTION.equals(intent.getAction()))
            {
                question = (Question) intent.getSerializableExtra(StringConstants.QUESTION);
                getQuestionAndAnswers(receiver, question);
            }
            else
            {
                question = getQuestionMetaAndBodyAndSend(receiver, questionId);
            }

            QuestionsCache.getInstance().add(question.id, question);
        }
    }

    private void getQuestionAndAnswers(ResultReceiver receiver, Question question)
    {
        question.body = questionService.getQuestionBodyForId(question.id);
        sendSerializable(receiver, RESULT_CODE_Q_BODY, StringConstants.BODY, question.body);

        getCommentsAndSend(receiver, question);

        if (question.answerCount > 0)
            question.answers = getAnswersAndSend(receiver, question.id, 1);
    }

    private void getCommentsAndSend(ResultReceiver receiver, Question question)
    {
        StackXPage<Comment> commentsPage = questionService.getComments(StringConstants.QUESTIONS,
                        String.valueOf(question.id), 1);
        if (commentsPage != null)
        {
            question.comments = commentsPage.items;
            sendSerializable(receiver, RESULT_CODE_Q_COMMENTS, StringConstants.COMMENTS, question.comments);
        }
    }

    private Question getQuestionMetaAndBodyAndSend(ResultReceiver receiver, long questionId)
    {
        Question question = questionService.getQuestionFullDetails(questionId);
        sendSerializable(receiver, RESULT_CODE_Q, StringConstants.QUESTION, question);
        getCommentsAndSend(receiver, question);
        return question;
    }

    private ArrayList<Answer> getAnswersAndSend(final ResultReceiver receiver, long questionId, int page)
    {
        ArrayList<Answer> answers = questionService.getAnswersForQuestion(questionId, page);
        sendSerializable(receiver, RESULT_CODE_ANSWERS, StringConstants.ANSWERS, answers);
        return answers;
    }

    private void sendSerializable(ResultReceiver receiver, int resultCode, String key, Serializable value)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, value);
        receiver.send(resultCode, bundle);
    }

}

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
		    ArrayList<Answer> answers = questionService.getAnswersForQuestion(questionId, page);
		    if (answers != null)
		    {
			QuestionsCache.getInstance().updateAnswersForQuestion(questionId, answers);

			Bundle bundle = new Bundle();
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
	long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);

	Question question = QuestionsCache.getInstance().get(questionId);

	if (question != null)
	{
	    Log.d(TAG, "Question " + questionId + " recovered from cache.");
	    bundle.putSerializable(StringConstants.QUESTION, question);
	    receiver.send(RESULT_CODE_Q, bundle);
	}
	else
	{
	    question = (Question) intent.getSerializableExtra(StringConstants.QUESTION);

	    if (StringConstants.QUESTION.equals(intent.getAction()))
	    {
		question.body = questionService.getQuestionBodyForId(questionId);
		bundle.putString(StringConstants.BODY, question.body);
		receiver.send(RESULT_CODE_Q_BODY, bundle);

		getCommentsAndSend(question, receiver, bundle, questionId);

		if (question.answerCount > 0)
		    getAnswersAndSend(question, receiver, bundle, questionId);

	    }
	    else
	    {
		question = questionService.getQuestionFullDetails(questionId);
		bundle.putSerializable(StringConstants.QUESTION, question);
		receiver.send(RESULT_CODE_Q, bundle);
	    }

	    QuestionsCache.getInstance().add(question.id, question);
	}
    }

    public void getAnswersAndSend(Question question, ResultReceiver receiver, Bundle bundle, long questionId)
    {
	question.answers = questionService.getAnswersForQuestion(questionId, 1);
	bundle.putSerializable(StringConstants.ANSWERS, question.answers);
	receiver.send(RESULT_CODE_Q_ANSWERS, bundle);
    }

    public void getCommentsAndSend(Question question, ResultReceiver receiver, Bundle bundle, long questionId)
    {
	question.comments = questionService.getComments(StringConstants.QUESTIONS, String.valueOf(questionId));
	bundle.putSerializable(StringConstants.COMMENTS, question.comments);
	receiver.send(RESULT_CODE_Q_COMMENTS, bundle);
    }
}

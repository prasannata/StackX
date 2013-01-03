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
import android.util.Log;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.QuestionsCache;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionDetailsIntentService extends AbstractIntentService
{
    private static final String TAG = QuestionDetailsIntentService.class.getSimpleName();

    private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

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
	String action = intent.getAction();

	if (action != null)
	{
	    if (action.equals(QuestionIntentAction.QUESTION_ANSWERS.getAction()))
	    {
		long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0L);
		int page = intent.getIntExtra(StringConstants.PAGE, 0);

		if (questionId > 0 && page > 0)
		{
		    ArrayList<Answer> answers = questionService.getAnswersForQuestion(questionId, page);
		    if (answers != null)
		    {
			QuestionsCache.getInstance().updateAnswersForQuestion(questionId, answers);
			broadcastAnswers(answers);
		    }
		}
	    }
	    else
	    {

		getQuestionDetail(intent);
	    }
	}
    }

    private void getQuestionDetail(Intent intent)
    {
	Question cachedQuestion = null;
	Question question = (Question) intent.getSerializableExtra(StringConstants.QUESTION);
	long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);
	boolean fromCache = intent.getBooleanExtra(StringConstants.CACHED, false);

	if (fromCache)
	{
	    if (StringConstants.QUESTION.equals(intent.getAction()))
		cachedQuestion = QuestionsCache.getInstance().get(question.id);
	    else
		cachedQuestion = QuestionsCache.getInstance().get(questionId);
	}
	if (cachedQuestion != null)
	{
	    Log.d(TAG, "Question " + question.id + " recovered from cache.");
	    broadcastQuestion(cachedQuestion);
	}
	else
	{
	    if (StringConstants.QUESTION.equals(intent.getAction()))
		question.body = questionService.getQuestionBodyForId(question.id);
	    else
		question = questionService.getQuestionFullDetails(intent.getLongExtra(StringConstants.QUESTION_ID, 0));

	    getItemsForQuestionAndBroadcast(question);
	}
    }

    private void getItemsForQuestionAndBroadcast(Question question)
    {
	broadcastQuestion(question);
	broadcoastComments(questionService.getComments(StringConstants.QUESTIONS, question.id));

	if (question.answers == null)
	    question.answers = new ArrayList<Answer>();

	if (question.answerCount > 0)
	{
	    question.answers = questionService.getAnswersForQuestion(question.id, 1);
	    broadcastAnswers(question.answers);
	}

	QuestionsCache.getInstance().add(question.id, question);
    }

    private void broadcastAnswers(ArrayList<Answer> answers)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(QuestionIntentAction.QUESTION_ANSWERS.getAction());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(QuestionIntentAction.QUESTION_ANSWERS.getAction(), answers);
	sendBroadcast(broadcastIntent);
    }

    private void broadcoastComments(ArrayList<Comment> comments)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(QuestionIntentAction.QUESTION_COMMENTS.getAction());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(QuestionIntentAction.QUESTION_COMMENTS.getAction(), comments);
	sendBroadcast(broadcastIntent);
    }

    private void broadcastQuestion(Question question)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(QuestionIntentAction.QUESTION_FULL_DETAILS.getAction());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(QuestionIntentAction.QUESTION_FULL_DETAILS.getAction(), question);
	sendBroadcast(broadcastIntent);
    }
}

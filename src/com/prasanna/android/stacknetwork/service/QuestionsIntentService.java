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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsIntentService extends AbstractIntentService
{
    private static final String TAG = QuestionsIntentService.class.getSimpleName();
    public static final int GET_FRONT_PAGE = 1;
    public static final int GET_FAQ_FOR_TAG = 2;
    public static final int SEARCH = 3;
    public static final int GET_RELATED = 4;

    private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

    public QuestionsIntentService()
    {
	this("UserQuestionsService");
    }

    public QuestionsIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
	final int action = intent.getIntExtra(StringConstants.ACTION, -1);
	final int page = intent.getIntExtra(StringConstants.PAGE, 1);

	try
	{
	    Bundle bundle = new Bundle();

	    switch (action)
	    {
		case GET_FRONT_PAGE:
		    Log.d(TAG, "Get front page");
		    bundle.putSerializable(StringConstants.QUESTIONS, questionService.getAllQuestions(page));
		    break;
		case GET_FAQ_FOR_TAG:
		    String tag = intent.getStringExtra(QuestionIntentAction.TAGS_FAQ.getAction());
		    Log.d(TAG, "Get FAQ for " + tag);
		    bundle.putSerializable(StringConstants.QUESTIONS, questionService.getFaqForTag(tag, page));
		    break;
		case SEARCH:
		    String query = intent.getStringExtra(SearchManager.QUERY);
		    Log.d(TAG, "Received search query: " + query);
		    bundle.putSerializable(StringConstants.QUESTIONS, questionService.search(query, page));
		    break;
		case GET_RELATED:
		    Log.d(TAG, "Get related questions");
		    long questionId = intent.getLongExtra(StringConstants.QUESTION_ID, 0);
		    if (questionId > 0)
			bundle.putSerializable(StringConstants.QUESTIONS,
			                questionService.getRelatedQuestions(questionId, page));
		    break;

		default:
		    Log.e(TAG, "Unknown action: " + action);
		    break;
	    }

	    receiver.send(0, bundle);
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}
    }
}

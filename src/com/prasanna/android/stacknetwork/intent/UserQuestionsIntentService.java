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
package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.exceptions.HttpErrorException;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserQuestionsIntentService extends AbstractIntentService
{
    private static final String TAG = UserQuestionsIntentService.class.getSimpleName();

    private UserService userService = UserService.getInstance();

    public UserQuestionsIntentService()
    {
	this("UserQuestionsService");
    }

    public UserQuestionsIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	try
	{
	    handleIntent(intent);
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getCode(), e.getMessage());
	}
    }

    private void handleIntent(Intent intent)
    {
	int page = intent.getIntExtra(StringConstants.PAGE, 1);
	String action = intent.getAction();
	String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);

	if (accessToken == null)
	{
	    long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
	    if (userId == -1)
	    {
		getFrontPageQuestionsAndBroadcast(null, page);
	    }
	    else
	    {
		broadcastIntent(userService.getQuestionsByUser(userId, page));
	    }
	}
	else
	{
	    if (action.equals(UserIntentAction.QUESTIONS_BY_USER.name()))
	    {
		broadcastIntent(userService.getMyQuestions(page));
	    }
	    else
	    {
		getFrontPageQuestionsAndBroadcast(accessToken, page);
	    }
	}
    }

    private void getFrontPageQuestionsAndBroadcast(String accessToken, int page)
    {
	ArrayList<Question> questions = userService.getAllQuestions(page);

	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(StringConstants.QUESTIONS, questions);
	sendBroadcast(broadcastIntent);

	Log.d(TAG, "Questions fetched and broadcasted");
    }

    private void broadcastIntent(ArrayList<Question> questions)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(StringConstants.QUESTIONS, questions);
	sendBroadcast(broadcastIntent);
    }
}

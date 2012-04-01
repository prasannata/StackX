package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserQuestionsIntentService extends IntentService
{
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
	int page = intent.getIntExtra("page", 1);
	String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);
	if (accessToken == null)
	{
	    long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
	    if (userId == -1)
	    {
		getFrontPageQuestions(page);
	    }
	    else
	    {
		broadcastIntent(userService.getQuestionsByUser(userId, page));
	    }
	}
	else
	{
	    broadcastIntent(userService.getMyQuestions(accessToken, page));
	}
    }

    private void getFrontPageQuestions(int page)
    {
	ArrayList<Question> questions = userService.getAllQuestions(page);
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTIONS.getExtra(), questions);
	sendBroadcast(broadcastIntent);
    }

    private void broadcastIntent(ArrayList<Question> questions)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.getExtra(), questions);
	sendBroadcast(broadcastIntent);
    }
}

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
    private UserService userSerivce = UserService.getInstance();

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
	long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
	int page = intent.getIntExtra("page", 1);

	if (userId == -1)
	{
	    getQuestionsForUnauthorizedUser(page);
	}
    }

    private void getQuestionsForUnauthorizedUser(int page)
    {
	ArrayList<Question> questions = userSerivce.getAllQuestions(page);
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(
	    IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.getExtra(), questions);
	sendBroadcast(broadcastIntent);
    }

}

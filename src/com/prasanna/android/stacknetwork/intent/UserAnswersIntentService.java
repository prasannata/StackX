package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserAnswersIntentService extends IntentService
{
    private UserService userService = UserService.getInstance();

    public UserAnswersIntentService()
    {
	this("UserAnswersIntentService");
    }

    public UserAnswersIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	int page = intent.getIntExtra(StringConstants.PAGE, 1);

	String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);
	if (accessToken == null)
	{
	    long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
	    if (userId > 0)
	    {
		broadcastIntent(userService.getAnswersByUser(userId, page));
	    }
	}
	else
	{
	    broadcastIntent(userService.getMyAnswers(page));
	}
    }

    private void broadcastIntent(ArrayList<Answer> answers)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.getExtra(), answers);
	sendBroadcast(broadcastIntent);
    }
}

package com.prasanna.android.stacknetwork.intent;

import android.app.IntentService;
import android.content.Intent;

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
	long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
	int page = intent.getIntExtra(StringConstants.PAGE, 1);

	if (userId > 0)
	{
	    getAnswersByUserAndBroadcastIntent(userId, page);
	}
    }

    private void getAnswersByUserAndBroadcastIntent(long userId, int page)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.getExtra(),
	                userService.getAnswersByUser(userId, page));
	sendBroadcast(broadcastIntent);
    }
}

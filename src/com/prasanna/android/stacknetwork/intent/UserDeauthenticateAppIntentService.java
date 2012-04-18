package com.prasanna.android.stacknetwork.intent;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserDeauthenticateAppIntentService extends IntentService
{
    public UserDeauthenticateAppIntentService()
    {
	this(UserDeauthenticateAppIntentService.class.getSimpleName());
    }

    public UserDeauthenticateAppIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.LOGOUT.name());
	broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.LOGOUT.getExtra(),
	                UserService.getInstance().logout(intent.getStringExtra(StringConstants.ACCESS_TOKEN)));
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	sendBroadcast(broadcastIntent);
    }
}

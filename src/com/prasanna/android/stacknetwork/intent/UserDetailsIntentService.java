package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserDetailsIntentService extends IntentService
{
    private UserService userService = UserService.getInstance();

    public UserDetailsIntentService()
    {
	this("UserByIdService");
    }

    public UserDetailsIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	User user = null;
	long userId = -1;
	String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);
	if (accessToken == null)
	{
	    userId = intent.getLongExtra(StringConstants.USER_ID, -1);
	    user = userService.getUserById(userId);
	}
	else
	{
	    user = userService.getLoggedInUser(accessToken);
	}

	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.USER_DETAIL.getExtra(), user);
	sendBroadcast(broadcastIntent);

	if (user != null)
	{
	    fetchUserAccountsAndBroadcast(userId, accessToken);
	}
    }

    private void fetchUserAccountsAndBroadcast(long userId, String accessToken)
    {
	ArrayList<Account> accounts = null;
	if (accessToken != null)
	{
	    accounts = userService.getAccounts(accessToken, 1);
	}
	else
	{
	    accounts = userService.getAccounts(userId, 1);
	}

	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.UserIntentAction.USER_ACCOUNTS.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.USER_ACCOUNTS.getExtra(), accounts);
	sendBroadcast(broadcastIntent);
    }
}

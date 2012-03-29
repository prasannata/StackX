package com.prasanna.android.stacknetwork.intent;

import android.app.IntentService;
import android.content.Intent;

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
        long userId = (long) intent.getLongExtra(StringConstants.USER_ID, -1);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.USER_DETAIL.getExtra(),
                userService.getUserById(userId));
        sendBroadcast(broadcastIntent);
    }
}

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserInboxIntentService extends IntentService
{
    private UserService userService = UserService.getInstance();

    public UserInboxIntentService()
    {
        this(UserInboxIntentService.class.getSimpleName());
    }

    public UserInboxIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        ArrayList<InboxItem> inboxItems = null;
        String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);
        int page = intent.getIntExtra(StringConstants.PAGE, 1);

        if (accessToken != null)
        {
            inboxItems = userService.getInbox(page);
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.UserIntentAction.INBOX.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.INBOX.getExtra(), inboxItems);
        sendBroadcast(broadcastIntent);
    }
}

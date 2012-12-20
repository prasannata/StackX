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

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;

public class UserInboxIntentService extends AbstractIntentService
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
        try
        {
            int page = intent.getIntExtra(StringConstants.PAGE, 1);
            String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);
            if (accessToken != null)
            {
                if (intent.hasExtra(UserIntentAction.NEW_MSG.getExtra()))
                    broadcastIntent(userService.getInbox(page), IntentActionEnum.UserIntentAction.INBOX.name());
                else
                    broadcastIntent(userService.getUnreadItemsInInbox(page),
                            IntentActionEnum.UserIntentAction.NEW_MSG.name());
            }
        }
        catch (HttpErrorException e)
        {
            broadcastHttpErrorIntent(e.getError());
        }
    }

    private void broadcastIntent(ArrayList<InboxItem> inboxItems, String action)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.INBOX.getExtra(), inboxItems);
        sendBroadcast(broadcastIntent);
    }
}

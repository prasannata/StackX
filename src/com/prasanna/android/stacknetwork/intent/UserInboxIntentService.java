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
import java.util.HashMap;

import android.content.Intent;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

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
		{
		    getUnreadInboxItems(intent, page);
		}
		else
		{
		    broadcastIntent(UserIntentAction.INBOX.name(), UserIntentAction.INBOX.getExtra(),
			            userService.getInbox(page));
		}
	    }
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}
    }

    @SuppressWarnings("unchecked")
    private void getUnreadInboxItems(Intent intent, int page)
    {
	int totalNewMsgs = 0;
	HashMap<String, Integer> newMsgCount = new HashMap<String, Integer>();
	ArrayList<Site> sites = (ArrayList<Site>) intent.getSerializableExtra(UserIntentAction.SITES.getExtra());

	for (Site site : sites)
	{
	    ArrayList<InboxItem> unreadInboxItems = userService.getUnreadItemsInInbox(page, site);
	    if (unreadInboxItems != null && !unreadInboxItems.isEmpty())
	    {
		newMsgCount.put(site.name, unreadInboxItems.size());
		totalNewMsgs += unreadInboxItems.size();
	    }
	}

	broadcastUnreadItemsCount(totalNewMsgs, newMsgCount);
    }

    private void broadcastUnreadItemsCount(int totalNewMsgs, HashMap<String, Integer> newMsgCount)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(UserIntentAction.NEW_MSG.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(UserIntentAction.NEW_MSG.getExtra(), newMsgCount);
	broadcastIntent.putExtra(UserIntentAction.TOTAL_NEW_MSGS.getExtra(), totalNewMsgs);
	sendBroadcast(broadcastIntent);
    }
}

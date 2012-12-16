/*
    Copyright 2012 Prasanna Thirumalai
    
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

import java.util.HashMap;

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
            user = userService.getLoggedInUser();
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
        HashMap<String, Account> accounts = null;
        if (accessToken != null)
        {
            accounts = userService.getAccounts(1);
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

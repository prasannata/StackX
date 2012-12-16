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

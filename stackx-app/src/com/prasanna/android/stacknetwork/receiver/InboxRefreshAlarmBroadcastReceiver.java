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

package com.prasanna.android.stacknetwork.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class InboxRefreshAlarmBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = InboxRefreshAlarmBroadcastReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    LogWrapper.d(TAG, "Alarm receiver invoked");

    checkForNewMessages(context);
  }

  private void checkForNewMessages(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN)) {
      LogWrapper.d(TAG, "Checking inbox for updates");

      Intent fetchInboxIntent = new Intent(context, UserIntentService.class);
      fetchInboxIntent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_UNREAD_INBOX);
      fetchInboxIntent.putExtra(UserIntentAction.NEW_MSG.getAction(), Boolean.TRUE);

      context.startService(fetchInboxIntent);
    }
  }
}

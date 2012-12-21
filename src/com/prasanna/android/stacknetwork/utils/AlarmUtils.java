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

package com.prasanna.android.stacknetwork.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.InboxRefreshActivity;
import com.prasanna.android.stacknetwork.LoginActivity;
import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.receiver.NewMsgNotificationReceiver;

public class AlarmUtils
{
    private static final String TAG = AlarmUtils.class.getSimpleName();

    public static void createInboxRefreshAlarm(Context context)
    {
	int interval = SettingsFragment.getInboxRefreshInterval(context);

	if (interval > 0)
	{
	    interval = interval * 60 * 1000;
	    Log.d(TAG, "Inbox refreshing set to " + interval + " minutes");
	}
	else
	{
	    Log.d(TAG, "Inbox refreshing set to manual");
	}

	createRepeatingAlarm(LoginActivity.getAppContext(), InboxRefreshActivity.class, interval, 0);
    }

    public static void cancelInboxRefreshAlarm()
    {
	cancelAlarm(LoginActivity.getAppContext(), 0);
    }

    public static void rescheduleInboxRefreshAlarm(Context context)
    {
	cancelInboxRefreshAlarm();
	createInboxRefreshAlarm(context);
    }

    public static void createRepeatingAlarm(Context context, Class<?> clazz, int intervalInMs, int flags)
    {
	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	Intent intent = new Intent(context, clazz);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalInMs, pendingIntent);
    }

    public static void cancelAlarm(Context context, int flags)
    {
	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	Intent intent = new Intent(context, NewMsgNotificationReceiver.class);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
	pendingIntent.cancel();
	alarmManager.cancel(pendingIntent);
    }

    public static void rescheduleRepeatingAlarm(Context context, Class<?> clazz, int newIntervalInMs, int flags)
    {
	cancelAlarm(context, flags);
	createRepeatingAlarm(context, clazz, newIntervalInMs, flags);
    }
}

/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.receiver.AccountSyncAlarmBroadcastReceiver;
import com.prasanna.android.stacknetwork.receiver.InboxRefreshAlarmBroadcastReceiver;
import com.prasanna.android.utils.LogWrapper;

public class AlarmUtils {
  private static final String TAG = AlarmUtils.class.getSimpleName();

  public static boolean isAccountSyncAlarmSet(Context context) {
    Intent intent = new Intent(context, AccountSyncAlarmBroadcastReceiver.class);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null;
  }

  public static void activatePeriodicAccountSync(Context context) {
    createRepeatingAlarm(context, AccountSyncAlarmBroadcastReceiver.class, IntegerConstants.MS_IN_HALF_AN_HOUR, 0);
  }

  public static void cancelPeriodicAccountSync(Context context) {
    cancelAlarm(context, AccountSyncAlarmBroadcastReceiver.class, 0);
  }

  public static void setInboxRefreshAlarm(Context context) {
    int interval = SettingsFragment.getInboxRefreshInterval(context);

    if (interval <= 0) {
      LogWrapper.d(TAG, "Inbox refreshing set to manual");
      cancelInboxRefreshAlarm(context);
    }
    else {
      LogWrapper.d(TAG, "Inbox refreshing set to " + interval + " minutes");
      interval = interval * 60 * 1000;
      createRepeatingAlarm(context, InboxRefreshAlarmBroadcastReceiver.class, interval, 0);
    }
  }

  public static void cancelInboxRefreshAlarm(Context context) {
    LogWrapper.d(TAG, "Cancel inbox refresh alarm");
    cancelAlarm(context, InboxRefreshAlarmBroadcastReceiver.class, 0);
  }

  public static void rescheduleInboxRefreshAlarm(Context context) {
    cancelInboxRefreshAlarm(context);
    setInboxRefreshAlarm(context);
  }

  public static void createRepeatingAlarm(Context context, Class<?> clazz, int intervalInMs, int flags) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, clazz);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalInMs, pendingIntent);
  }

  public static void cancelAlarm(Context context, Class<?> clazz, int flags) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, clazz);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
    alarmManager.cancel(pendingIntent);
    pendingIntent.cancel();
  }

  public static void rescheduleRepeatingAlarm(Context context, Class<?> clazz, int newIntervalInMs, int flags) {
    cancelAlarm(context, clazz, flags);
    createRepeatingAlarm(context, clazz, newIntervalInMs, flags);
  }
}

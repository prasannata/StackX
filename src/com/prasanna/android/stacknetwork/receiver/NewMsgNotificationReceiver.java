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

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Vibrator;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.UserInboxActivity;
import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;

public class NewMsgNotificationReceiver extends BroadcastReceiver
{
    private static final String NEW_MSG_NOTIF_TITLE = "%d new messages";
    private static final int VIBRATE_DURATION = 300;

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Context context, Intent intent)
    {
	if (SettingsFragment.isNotificationEnabled(context))
	{
	    HashMap<String, Integer> unreadItemsInInbox = (HashMap<String, Integer>) intent
		            .getSerializableExtra(UserIntentAction.NEW_MSG.getExtra());
	    int totalNewMsgs = intent.getIntExtra(UserIntentAction.TOTAL_NEW_MSGS.getExtra(), 0);

	    if (unreadItemsInInbox != null && !unreadItemsInInbox.isEmpty())
	    {
		sendNotification(context, totalNewMsgs, unreadItemsInInbox);

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		vibrateIfEnabled(context, audioManager.getRingerMode());

		playNotificationTone(context, audioManager.getRingerMode());
	    }
	}
    }

    private void vibrateIfEnabled(Context context, int ringerMode)
    {
	if (SettingsFragment.isVibrateEnabled(context)
	                && (ringerMode == AudioManager.RINGER_MODE_VIBRATE || ringerMode == AudioManager.RINGER_MODE_NORMAL))
	{
	    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    vibrator.vibrate(VIBRATE_DURATION);
	}
    }

    private void playNotificationTone(Context context, int ringerMode)
    {
	if (ringerMode == AudioManager.RINGER_MODE_NORMAL)
	{
	    Ringtone ringtone = RingtoneManager.getRingtone(context, SettingsFragment.getRingtone(context));
	    ringtone.play();
	}
    }

    @SuppressLint("NewApi")
    private void sendNotification(Context context, int totalNewMsgs, HashMap<String, Integer> newMsgCount)
    {
	Intent resultIntent = new Intent(context, UserInboxActivity.class);
	TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
	stackBuilder.addParentStack(UserInboxActivity.class);
	stackBuilder.addNextIntent(resultIntent);
	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

	SoftReference<Bitmap> bitmapSoftReference = AppUtils.getBitmap(context.getResources(),
	                R.drawable.new_msg_notify);

	Notification.InboxStyle inboxStyle = new Notification.InboxStyle();

	for (Map.Entry<String, Integer> entry : newMsgCount.entrySet())
	{
	    inboxStyle.addLine(entry.getKey() + " (" + entry.getValue() + ")");
	}

	Notification notification = new Notification.Builder(context)
	                .setContentTitle(String.format(NEW_MSG_NOTIF_TITLE, totalNewMsgs)).setStyle(inboxStyle)
	                .setContentText("Question").setSmallIcon(R.drawable.new_msg_notify)
	                .setLargeIcon(bitmapSoftReference.get()).setContentIntent(resultPendingIntent).build();

	NotificationManager notificationManager = (NotificationManager) context
	                .getSystemService(Context.NOTIFICATION_SERVICE);

	notification.flags |= Notification.FLAG_AUTO_CANCEL;

	notificationManager.notify(0, notification);
    }
}

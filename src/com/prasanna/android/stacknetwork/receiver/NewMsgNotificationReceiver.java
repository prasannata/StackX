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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.UserInboxActivity;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;

public class NewMsgNotificationReceiver extends BroadcastReceiver
{
    private static final String NEW_MSG_NOTIF_TITLE = "You have got two %d new messages";

    @SuppressWarnings("unchecked")
    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        ArrayList<InboxItem> unreadItemsInInbox = (ArrayList<InboxItem>) intent
                .getSerializableExtra(UserIntentAction.INBOX.getExtra());

        if (unreadItemsInInbox != null && !unreadItemsInInbox.isEmpty())
        {
            Intent resultIntent = new Intent(context, UserInboxActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(UserInboxActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(context)
                    .setContentTitle(String.format(NEW_MSG_NOTIF_TITLE, unreadItemsInInbox.size()))
                    .setContentText("Question").setSmallIcon(R.drawable.new_msg_notify)
                    .setContentIntent(resultPendingIntent).build();

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, notification);
        }
    }
}

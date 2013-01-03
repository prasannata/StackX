package com.prasanna.android.stacknetwork.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class InboxRefreshAlarmBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = InboxRefreshAlarmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
	Log.d(TAG, "Alarm receiver invoked");

	checkForNewMessages(context);
    }

    private void checkForNewMessages(Context context)
    {
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
	{
	    Log.d(TAG, "Checking inbox for updates");

	    Intent fetchInboxIntent = new Intent(context, UserIntentService.class);
	    fetchInboxIntent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_UNREAD_INBOX);
	    fetchInboxIntent.putExtra(UserIntentAction.NEW_MSG.getAction(), Boolean.TRUE);

	    context.startService(fetchInboxIntent);
	}
    }
}

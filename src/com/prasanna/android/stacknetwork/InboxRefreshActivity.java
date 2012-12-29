package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class InboxRefreshActivity extends Activity
{
    private static final String TAG = InboxRefreshActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	Log.d(TAG, "onCreate");

	super.onCreate(savedInstanceState);

	Intent fetchInboxIntent = new Intent(this, UserIntentService.class);

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
	{
	    fetchInboxIntent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_UNREAD_INBOX);
	    fetchInboxIntent.putExtra(UserIntentAction.NEW_MSG.getExtra(), Boolean.TRUE);
	    fetchInboxIntent.putExtra(UserIntentAction.SITES.getExtra(),
		            CacheUtils.getRegisteredSitesForUser(getCacheDir()));

	    startService(fetchInboxIntent);
	}
    }
}

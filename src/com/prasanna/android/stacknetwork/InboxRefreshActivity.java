package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.prasanna.android.stacknetwork.intent.UserInboxIntentService;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class InboxRefreshActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	Intent fetchInboxIntent = new Intent(this, UserInboxIntentService.class);

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
	{
	    fetchInboxIntent.putExtra(StringConstants.ACCESS_TOKEN,
		            sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null));

	    fetchInboxIntent.putExtra(UserIntentAction.NEW_MSG.getExtra(), Boolean.TRUE);
	    fetchInboxIntent.putExtra(UserIntentAction.SITES.getExtra(), CacheUtils.getRegisteredSitesForUser(this));

	    startService(fetchInboxIntent);
	}
    }
}

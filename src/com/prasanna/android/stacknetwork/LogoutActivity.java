package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.intent.UserDeauthenticateAppIntentService;
import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class LogoutActivity extends Activity
{
    private static final String TAG = LogoutActivity.class.getSimpleName();

    private Intent accessTokenDeauthenticateIntent;

    private ProgressDialog progressDialog;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    progressDialog.dismiss();

	    StackExchangeHttpError error = (StackExchangeHttpError) intent.getSerializableExtra(UserIntentAction.LOGOUT
		            .getExtra());

	    processLogoutResponse(error);
	}

    };

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(new LinearLayout(this));
	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	String accessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);
	registerReceiver();
	startIntentService(accessToken);
    }

    @Override
    protected void onDestroy()
    {
	super.onDestroy();
	stopServiceAndUnregisterReceiver();
    }

    private void stopServiceAndUnregisterReceiver()
    {
	if (accessTokenDeauthenticateIntent != null)
	{
	    stopService(accessTokenDeauthenticateIntent);
	}

	try
	{
	    unregisterReceiver(receiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    @Override
    public void onStop()
    {
	super.onStop();

	stopServiceAndUnregisterReceiver();
    }

    private void startIntentService(String accessToken)
    {
	progressDialog = ProgressDialog.show(LogoutActivity.this, "", "Logging out");

	accessTokenDeauthenticateIntent = new Intent(this, UserDeauthenticateAppIntentService.class);
	accessTokenDeauthenticateIntent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
	startService(accessTokenDeauthenticateIntent);
    }

    private void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(UserIntentAction.LOGOUT.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(receiver, filter);
    }

    private void processLogoutResponse(StackExchangeHttpError error)
    {
	if (error != null && error.id == -1)
	{
	    Editor editor = sharedPreferences.edit();
	    editor.remove(StringConstants.ACCESS_TOKEN);
	    editor.commit();
	    
	    Intent loginIntent = new Intent(LogoutActivity.this, LoginActivity.class);
	    startActivity(loginIntent);
	}
	else if (error != null && error.id > 0)
	{
	    Log.d(TAG, "Logout failed with " + error.message);
	    finish();
	}
	else
	{
	    Log.d(TAG, "Logout failed for unknown reason");
	    finish();
	}
    }
}

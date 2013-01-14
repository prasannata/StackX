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

import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class LogoutActivity extends Activity
{
    private static final String TAG = LogoutActivity.class.getSimpleName();

    private Intent deauthAppIntent;

    private ProgressDialog progressDialog;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            progressDialog.dismiss();

            StackExchangeHttpError error = (StackExchangeHttpError) intent.getSerializableExtra(UserIntentAction.LOGOUT
                            .getAction());

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
        if (deauthAppIntent != null)
        {
            stopService(deauthAppIntent);
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

        deauthAppIntent = new Intent(this, UserIntentService.class);
        deauthAppIntent.putExtra(StringConstants.ACTION, UserIntentService.DEAUTH_APP);
        deauthAppIntent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
        startService(deauthAppIntent);
    }

    private void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(UserIntentAction.LOGOUT.getAction());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    private void startLoginActivity()
    {
        Intent intent = new Intent(this, StackNetworkListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void processLogoutResponse(StackExchangeHttpError error)
    {
        if (error != null && error.id == -1)
        {
            Editor editor = sharedPreferences.edit();
            editor.remove(StringConstants.ACCESS_TOKEN);
            editor.commit();

            SharedPreferencesUtil.clear(getApplicationContext());
            SharedPreferencesUtil.setFirstRunComplete(this);
            deleteMyTags();
            startLoginActivity();
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

    private void deleteMyTags()
    {
        TagDAO tagDAO = new TagDAO(this);
        try
        {
            tagDAO.open();
            tagDAO.deleteAll();
        }
        finally
        {
            tagDAO.close();
        }
    }
}

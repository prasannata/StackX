package com.prasanna.android.stacknetwork.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BatteryLevelReceiver extends BroadcastReceiver
{
    private static final String TAG = BatteryLevelReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive: " + intent.getAction());

        Toast.makeText(context, "Battery low", Toast.LENGTH_LONG).show();
    }
}

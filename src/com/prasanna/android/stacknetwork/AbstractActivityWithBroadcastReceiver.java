package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class AbstractActivityWithBroadcastReceiver extends Activity
{
    public abstract void processReceiverIntent(Context context, Intent intent);

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    processReceiverIntent(context, intent);
	}
    };

    public BroadcastReceiver getReceiver()
    {
	return receiver;
    }
}

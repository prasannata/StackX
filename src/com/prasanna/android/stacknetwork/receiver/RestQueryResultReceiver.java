package com.prasanna.android.stacknetwork.receiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class RestQueryResultReceiver extends ResultReceiver
{
    public interface StackXRestQueryResultReceiver
    {
	void onReceiveResult(int resultCode, Bundle resultData);
    }

    private StackXRestQueryResultReceiver receiver;

    public RestQueryResultReceiver(Handler handler)
    {
	super(handler);
	
    }

    public void setReceiver(StackXRestQueryResultReceiver receiver) {
	this.receiver = receiver;
    }
    
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData)
    {
	receiver.onReceiveResult(resultCode, resultData);
    }
}

package com.prasanna.android.stacknetwork.intent;

import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

import android.app.IntentService;
import android.content.Intent;

public abstract class AbstractIntentService extends IntentService
{

    public AbstractIntentService(String name)
    {
	super(name);
    }

    protected void broadcastHttpErrorIntent(int code, String text)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.ErrorIntentAction.HTTP_ERROR.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(StringConstants.HttpError.CODE, code);
	broadcastIntent.putExtra(StringConstants.HttpError.TEXT, text);
	sendBroadcast(broadcastIntent);
    }
}

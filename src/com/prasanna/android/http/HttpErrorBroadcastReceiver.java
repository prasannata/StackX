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

package com.prasanna.android.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.prasanna.android.listener.HttpErrorListener;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.ErrorIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class HttpErrorBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = HttpErrorBroadcastReceiver.class.getSimpleName();

    private Context activityContext;

    private HttpErrorListener httpErrorListener;

    public HttpErrorBroadcastReceiver(final Context context, final HttpErrorListener httpErrorListener)
    {
	this.activityContext = context;
	this.httpErrorListener = httpErrorListener;

	registerReceiver();
    }

    private void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(ErrorIntentAction.HTTP_ERROR.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	activityContext.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
	Log.d(TAG, "Http error receiver intent received");

	StackXError error = (StackXError) intent.getSerializableExtra(StringConstants.ERROR);
	if (httpErrorListener != null)
	{
	    httpErrorListener.onHttpError(error.statusCode, error.name + " - " + error.msg);
	}
    }
}

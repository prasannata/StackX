/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.service;

import java.io.Serializable;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.prasanna.android.http.ClientException;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.ErrorIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractIntentService extends IntentService
{
    private static final String TAG = AbstractIntentService.class.getSimpleName();

    public static final int ERROR = -1;

    public AbstractIntentService(String name)
    {
        super(name);
    }

    protected boolean helloWorld()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (!helloWorld())
            throw new ClientException(ClientException.ClientErrorCode.NO_NETWORK);
    }

    protected void broadcastHttpErrorIntent(StackXError error)
    {
        Log.d(TAG, "broadcastHttpErrorIntent: " + error.msg);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ErrorIntentAction.HTTP_ERROR.getAction());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(StringConstants.ERROR, error);
        sendBroadcast(broadcastIntent);
    }

    protected void broadcastSerializableExtra(String action, String extraName, Serializable extra)
    {
        Log.d(TAG, "broadcastSerializableExtra: " + action);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(extraName, extra);
        sendBroadcast(broadcastIntent);
    }
}

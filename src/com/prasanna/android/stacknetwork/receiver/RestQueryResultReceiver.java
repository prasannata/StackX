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

    public void setReceiver(StackXRestQueryResultReceiver receiver)
    {
        this.receiver = receiver;
    }

    public StackXRestQueryResultReceiver getReceiver()
    {
        return receiver;
    }
    
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData)
    {
        receiver.onReceiveResult(resultCode, resultData);
    }
}

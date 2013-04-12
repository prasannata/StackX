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

package com.prasanna.android.task;

import com.prasanna.android.utils.LogWrapper;

import android.os.AsyncTask;

public class AsyncTaskExecutor
{
    private static final AsyncTaskExecutor INSTANCE = new AsyncTaskExecutor();

    private AsyncTaskExecutor()
    {
    }

    public static AsyncTaskExecutor getInstance()
    {
        return INSTANCE;
    }

    public <P, S, R> AsyncTask<P, S, R> executeAsyncTask(AsyncTask<P, S, R> task, P... args)
    {
        if (task != null)
        {
            LogWrapper.d(getClass().getSimpleName(), "Executing async task");
            return task.execute(args);
        }

        return null;
    }
}

/*
    Copyright 2012 Prasanna Thirumalai
    
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

import java.io.File;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.utils.CacheUtils;

public class DeleteObjectAsyncTask extends AsyncTask<Object, Void, Boolean>
{
    private final File directory;
    private final String fileName;
    private final AsyncTaskCompletionNotifier<Boolean> notifier;

    public DeleteObjectAsyncTask(File directory, String fileName, AsyncTaskCompletionNotifier<Boolean> notifier)
    {
        this.directory = directory;
        this.fileName = fileName;
        this.notifier = notifier;
    }

    @Override
    protected Boolean doInBackground(Object... paramArrayOfParams)
    {
        return CacheUtils.deleteDir(new File(directory, fileName));
    }

    @Override
    protected void onPostExecute(Boolean deleted)
    {
        if (notifier != null)
        {
            notifier.notifyOnCompletion(deleted);
        }
    }
}

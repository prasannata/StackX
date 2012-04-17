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

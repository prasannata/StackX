package com.prasanna.android.task;

import java.io.File;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.utils.CacheUtils;

public class WriteObjectAsyncTask extends AsyncTask<Object, Void, Void>
{
    private final File directory;
    private final String fileName;

    public WriteObjectAsyncTask(File directory, String fileName)
    {
	this.directory = directory;
	this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Object... params)
    {
	if (params != null && params.length == 1)
	{
	    CacheUtils.cacheObject(params[0], directory, fileName);
	}

	return null;
    }
}

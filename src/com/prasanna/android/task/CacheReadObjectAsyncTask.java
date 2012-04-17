package com.prasanna.android.task;

import java.io.File;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.utils.CacheUtils;

public class CacheReadObjectAsyncTask extends AsyncTask<Void, Void, ArrayList<Object>>
{
    private final File directory;
    private final String fileName;
    private final AsyncTaskCompletionNotifier<ArrayList<Object>> notifier;

    public CacheReadObjectAsyncTask(File directory, String fileName,
	            AsyncTaskCompletionNotifier<ArrayList<Object>> notifier)
    {
	this.directory = directory;
	this.fileName = fileName;
	this.notifier = notifier;
    }

    @Override
    protected ArrayList<Object> doInBackground(Void... params)
    {
	if (fileName == null)
	{
	    return CacheUtils.readCachedObjectDir(directory);
	}
	else
	{
	    ArrayList<Object> objects = new ArrayList<Object>();
	    objects.add(CacheUtils.readCachedObject(directory, fileName));
	    return objects;
	}
    }

    @Override
    protected void onPostExecute(ArrayList<Object> objects)
    {
	if (notifier != null)
	{
	    notifier.notifyOnCompletion(objects);
	}
    }

}

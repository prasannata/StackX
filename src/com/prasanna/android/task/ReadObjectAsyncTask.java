package com.prasanna.android.task;

import java.io.File;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.utils.CacheUtils;

public class ReadObjectAsyncTask extends AsyncTask<Void, Void, ArrayList<Object>>
{
    private final File directory;
    private final String fileName;
    private final AsyncTaskCompletionNotifier<ArrayList<Object>> notifier;

    public ReadObjectAsyncTask(File directory,
            String fileName,
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
            return CacheUtils.readObjects(directory);
        }
        else
        {
            ArrayList<Object> objects = new ArrayList<Object>();
            objects.add(CacheUtils.readObject(new File(directory, fileName)));
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

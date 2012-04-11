package com.prasanna.android.task;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.service.UserService;

public class FetchTagsAsyncTask extends AsyncTask<Integer, Void, ArrayList<String>>
{
    private final AsyncTaskCompletionNotifier<ArrayList<String>> fetchUserTagsCompletionNotifier;

    public FetchTagsAsyncTask(AsyncTaskCompletionNotifier<ArrayList<String>> fetchUserTagsCompletionNotifier)
    {
	this.fetchUserTagsCompletionNotifier = fetchUserTagsCompletionNotifier;
    }

    @Override
    protected ArrayList<String> doInBackground(Integer... params)
    {
	return UserService.getInstance().getTags(1);
    }

    @Override
    protected void onPostExecute(ArrayList<String> result)
    {
	if (fetchUserTagsCompletionNotifier != null)
	{
	    fetchUserTagsCompletionNotifier.notifyOnCompletion(result);
	}
	
	super.onPostExecute(result);
    }
}

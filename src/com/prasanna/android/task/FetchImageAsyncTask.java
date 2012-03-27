package com.prasanna.android.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.prasanna.android.http.HttpHelper;

public class FetchImageAsyncTask extends AsyncTask<String, Void, Bitmap>
{
    private final ImageFetchAsyncTaskCompleteNotifierImpl imageFetchAsyncTaskCompleteNotiferImpl;

    public FetchImageAsyncTask(
	    ImageFetchAsyncTaskCompleteNotifierImpl imageFetchAsyncTaskCompleteNotiferImpl)
    {
	this.imageFetchAsyncTaskCompleteNotiferImpl = imageFetchAsyncTaskCompleteNotiferImpl;
    }

    @Override
    protected Bitmap doInBackground(String... urls)
    {
	Bitmap bitmap = null;
	if (urls != null && urls.length == 1)
	{
	    bitmap = HttpHelper.getInstance().fetchImage((String) urls[0]);
	}
	return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
	if (imageFetchAsyncTaskCompleteNotiferImpl != null)
	{
	    imageFetchAsyncTaskCompleteNotiferImpl.notifyOnCompletion(bitmap);
	}
    }

}

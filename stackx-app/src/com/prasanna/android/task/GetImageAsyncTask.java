/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.prasanna.android.cache.BitmapCache;
import com.prasanna.android.http.ClientException;
import com.prasanna.android.http.SecureHttpHelper;

public class GetImageAsyncTask extends AsyncTask<String, Void, Bitmap>
{
    private final AsyncTaskCompletionNotifier<Bitmap> imageFetchAsyncTaskCompleteNotiferImpl;

    /**
     * 
     * @param imageFetchAsyncTaskCompleteNotiferImpl
     *            Completion notifier to be invoked. Will be invoked from the UI
     *            thread.
     */
    public GetImageAsyncTask(AsyncTaskCompletionNotifier<Bitmap> imageFetchAsyncTaskCompleteNotiferImpl)
    {
        this.imageFetchAsyncTaskCompleteNotiferImpl = imageFetchAsyncTaskCompleteNotiferImpl;
    }

    @Override
    protected Bitmap doInBackground(String... urls)
    {
        if (urls != null && urls.length == 1)
        {
            Bitmap bitmap = BitmapCache.getInstance().get(urls[0]);

            if (bitmap == null)
            {
                try
                {
                    bitmap = SecureHttpHelper.getInstance().getImage((String) urls[0]);
                    BitmapCache.getInstance().add(urls[0], bitmap);
                }
                catch (ClientException e)
                {
                }
            }

            return bitmap;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        if (imageFetchAsyncTaskCompleteNotiferImpl != null)
            imageFetchAsyncTaskCompleteNotiferImpl.notifyOnCompletion(bitmap);
    }

}

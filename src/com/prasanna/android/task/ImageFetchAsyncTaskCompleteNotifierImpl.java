package com.prasanna.android.task;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class ImageFetchAsyncTaskCompleteNotifierImpl implements AsyncTaskCompletionNotifier<Bitmap>
{
    private final ImageView imageView;

    public ImageFetchAsyncTaskCompleteNotifierImpl(ImageView imageView)
    {
	this.imageView = imageView;
    }

    public void notifyOnCompletion(Bitmap result)
    {
	if (result != null && imageView != null)
	{
	    imageView.setImageBitmap(result);
	}
    }

}

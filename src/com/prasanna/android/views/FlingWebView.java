package com.prasanna.android.views;

import com.prasanna.android.listener.FlingActionListener;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class FlingWebView extends WebView
{
    private static final String TAG = FlingWebView.class.getSimpleName();

    private final Context context;
    private GestureDetector gestureDetector;
    public FlingActionListener flingActionListener;

    private GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener()
    {
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;

	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
	        float velocityY)
	{
	    Log.d(TAG, "onFling invoked");

	    if (Math.abs(event1.getY() - event1.getY()) > SWIPE_MAX_OFF_PATH)
	    {
		return false;
	    }
	    float distance = event1.getRawX() - event2.getRawX();
	    boolean enoughSpeed = Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY;
	    if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed)
	    {
		Log.d(TAG, "Swiped left to right.");
		if (flingActionListener != null)
		{
		    flingActionListener.flingedToRight();
		}
		return true;
	    }
	    else if (distance > SWIPE_MIN_DISTANCE && enoughSpeed)
	    {
		Log.d(TAG, "Swiped right to left.");
		if (flingActionListener != null)
		{
		    flingActionListener.flingedToLeft();
		}
		return true;
	    }
	    else
	    {
		return false;
	    }
	}
    };

    public FlingWebView(Context context)
    {
	super(context);
	this.context = context;
	gestureDetector = new GestureDetector(sogl);
    }

    public FlingWebView(Context context, AttributeSet attrs)
    {
	super(context, attrs);
	this.context = context;
	gestureDetector = new GestureDetector(sogl);
    }

    public FlingWebView(Context context, AttributeSet attrs, int defStyle)
    {
	super(context, attrs, defStyle);
	this.context = context;
	gestureDetector = new GestureDetector(sogl);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
	Log.d(TAG, "onTouchEvent invoked");
	return (gestureDetector.onTouchEvent(event) || super.onTouchEvent(event));
    }

    void show_toast(final String text)
    {
	Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}

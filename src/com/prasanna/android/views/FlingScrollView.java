package com.prasanna.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.prasanna.android.listener.FlingActionListener;

public class FlingScrollView extends ScrollView
{
    private GestureDetector gestureDetector;
    public FlingActionListener flingActionListener;

    public FlingScrollView(Context context)
    {
        super(context);
        gestureDetector = new GestureDetector(sogl);
    }

    public FlingScrollView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetector(sogl);
    }

    public FlingScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        gestureDetector = new GestureDetector(sogl);
    }

    private static final String TAG = FlingScrollView.class.getSimpleName();

    private GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener()
    {
        private static final int SWIPE_THRESHOLD_VELOCITY = 150;
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;

        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
        {
            Log.d(TAG, "onFling invoked: " + event1 + ", " + event2);

            if (event1 != null && event2 != null && Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH)
            {
                return false;
            }

            float downMotionEventX = event1 != null ? event1.getRawX() : 0;
            float triggerMotionEventX = event2 != null ? event2.getRawX() : 0;
            float distance = downMotionEventX - triggerMotionEventX;

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

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d(TAG, "onTouchEvent invoked");
        return (gestureDetector.onTouchEvent(event) || super.onTouchEvent(event));
    }

}

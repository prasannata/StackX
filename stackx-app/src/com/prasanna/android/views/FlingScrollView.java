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

package com.prasanna.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.prasanna.android.utils.LogWrapper;

public class FlingScrollView extends ScrollView {
  private GestureDetector gestureDetector;
  public FlingActionListener flingActionListener;

  private static final String TAG = FlingScrollView.class.getSimpleName();
  public static interface FlingActionListener {
    void flingedToRight();

    void flingedToLeft();
  }

  private GestureDetector.OnGestureListener sogl = new GestureDetector.OnGestureListener() {
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
      LogWrapper.d(TAG, "onFling invoked: " + event1 + ", " + event2);

      if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
        return false;
      }

      float distance = event1.getRawX() - event2.getRawX();
      boolean enoughSpeed = Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY;

      LogWrapper.d(TAG, "distance: " + distance + ", velocityX:" + velocityX + ", enoughSpeed:" + enoughSpeed);

      if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
        LogWrapper.d(TAG, "Swiped left to right.");

        if (flingActionListener != null) {
          flingActionListener.flingedToRight();
        }

        return true;
      }
      else if (distance > SWIPE_MIN_DISTANCE && enoughSpeed) {
        LogWrapper.d(TAG, "Swiped right to left.");

        if (flingActionListener != null) {
          flingActionListener.flingedToLeft();
        }

        return true;
      }
      else {
        return false;
      }
    }

    @Override
    public boolean onDown(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }
  };

  public FlingScrollView(Context context) {
    super(context);
    gestureDetector = new GestureDetector(context, sogl);
  }

  public FlingScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    gestureDetector = new GestureDetector(context, sogl);
  }

  public FlingScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
    gestureDetector = new GestureDetector(context, sogl);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    LogWrapper.d(TAG, "onTouchEvent invoked");
    return (gestureDetector.onTouchEvent(event) || super.onTouchEvent(event));
  }

}

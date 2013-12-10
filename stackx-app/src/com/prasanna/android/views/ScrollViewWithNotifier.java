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
import android.view.View;
import android.widget.ScrollView;

public class ScrollViewWithNotifier extends ScrollView {
  private OnScrollListener onScrollListener;

  public static interface OnScrollListener {
    public void onScrollToBottom(View view);
  }

  public ScrollViewWithNotifier(Context context) {
    super(context);
  }

  public ScrollViewWithNotifier(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public ScrollViewWithNotifier(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    View view = (View) getChildAt(getChildCount() - 1);

    int diff = (view.getBottom() - (getHeight() + getScrollY()));

    if (diff <= 0) {
      if (onScrollListener != null) {
        onScrollListener.onScrollToBottom(this);
      }
    }
    super.onScrollChanged(l, t, oldl, oldt);
  }

  public void setOnScrollListener(OnScrollListener onScrollListener) {
    this.onScrollListener = onScrollListener;
  }
}

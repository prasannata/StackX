package com.prasanna.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class ScrollViewWithNotifier extends ScrollView
{
    private OnScrollListener onScrollListener;

    public static interface OnScrollListener
    {
        public void onScrollToBottom(View view);
    }

    public ScrollViewWithNotifier(Context context)
    {
        super(context);
    }

    public ScrollViewWithNotifier(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public ScrollViewWithNotifier(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        View view = (View) getChildAt(getChildCount() - 1);

        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        if (diff <= 0)
        {
            if (onScrollListener != null)
            {
                onScrollListener.onScrollToBottom(this);
            }
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener)
    {
        this.onScrollListener = onScrollListener;
    }
}

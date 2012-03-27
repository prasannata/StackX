package com.prasanna.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.prasanna.android.stacknetwork.AbstractUserActionBarActivity;

public class ScrollViewWithNotifier extends ScrollView
{

    private AbstractUserActionBarActivity activity;

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

    public void setUsingActivity(AbstractUserActionBarActivity activity)
    {
        this.activity = activity;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        View view = (View) getChildAt(getChildCount() - 1);

        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        if (diff <= 0)
        {
            if (activity != null)
            {
                activity.scrollViewToBottomNotifier();
            }
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

}

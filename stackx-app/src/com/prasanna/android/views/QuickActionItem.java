package com.prasanna.android.views;

import android.view.View.OnClickListener;

public class QuickActionItem
{

    private final String title;
    private final OnClickListener onClickListener;

    public QuickActionItem(String title, OnClickListener onClickListener)
    {
        this.title = title;
        this.onClickListener = onClickListener;
    }

    public String getTitle()
    {
        return title;
    }

    public OnClickListener getOnClickListener()
    {
        return onClickListener;
    }

    
}

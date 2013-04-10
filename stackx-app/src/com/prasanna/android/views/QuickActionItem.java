/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

    public QuickActionItem(Context context, int titleResId, OnClickListener onClickListener)
    {
        this.title = context.getString(titleResId);
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuickActionItem other = (QuickActionItem) obj;
        if (title == null)
        {
            if (other.title != null)
                return false;
        }
        else if (!title.equals(other.title))
            return false;
        return true;
    }

}

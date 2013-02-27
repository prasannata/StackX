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

package com.prasanna.android.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;

public class TagsViewBuilder
{
    public static LinearLayout buildView(Context context, LinearLayout parentLayout, String[] tags)
    {
        return new TagsViewBuilder().build(context, parentLayout, tags);
    }

    private LinearLayout build(Context context, LinearLayout parentLayout, String[] tags)
    {
        if (parentLayout.getChildCount() > 0)
            parentLayout.removeAllViews();

        int maxWidth = context.getResources().getDisplayMetrics().widthPixels - 20;

        LinearLayout rowLayout = createNewRowForTags(context, 0);

        if (tags != null && tags.length > 0)
        {
            for (int i = 0; i < tags.length; i++)
            {
                LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(3, 0, 3, 0);
                TextView tagTextView = ((TextView) LayoutInflater.from(context).inflate(R.layout.tags_layout, null));
                tagTextView.setText(tags[i]);

                tagTextView.measure(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                rowLayout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                if ((tagTextView.getMeasuredWidth() + rowLayout.getMeasuredWidth()) > maxWidth)
                {
                    parentLayout.addView(rowLayout);
                    rowLayout = createNewRowForTags(context, 3);
                }

                rowLayout.addView(tagTextView, params);
            }

            parentLayout.addView(rowLayout);
        }

        return parentLayout;
    }

    private LinearLayout createNewRowForTags(Context context, int topMargin)
    {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = topMargin;
        rowLayout.setLayoutParams(layoutParams);
        return rowLayout;
    }

}

/*
    Copyright 2012 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.utils;

import java.util.ArrayList;

import android.graphics.Point;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.StackXItem;

public class PopupBuilder
{
    public static void build(LayoutInflater layoutInflater, View anchor, StackXItem item, Point size)
    {
        LinearLayout rowsParentLayout = inflateLayoutAndGetRowParent(layoutInflater, anchor, size);

        rowsParentLayout.addView(buildRow(layoutInflater, item), LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public static void build(LayoutInflater layoutInflater,
            View anchor,
            ArrayList<? extends StackXItem> items,
            Point size)
    {
        if (items != null)
        {
            LinearLayout rowsParentLayout = inflateLayoutAndGetRowParent(layoutInflater, anchor, size);

            for (StackXItem item : items)
            {
                rowsParentLayout.addView(buildRow(layoutInflater, item), LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private static LinearLayout inflateLayoutAndGetRowParent(LayoutInflater layoutInflater, View anchor, Point size)
    {
        final LinearLayout containerLayout = (LinearLayout) layoutInflater.inflate(R.layout.popup_layout, null);
        final PopupWindow pw = new PopupWindow(containerLayout, size.x - 30, 400, true);
        final ScrollView scrollView = (ScrollView) containerLayout.findViewById(R.id.popupScrollView);
        LinearLayout popupLinearLayout = (LinearLayout) scrollView.findViewById(R.id.popupItemList);
        ImageView closePopup = (ImageView) containerLayout.findViewById(R.id.closePopup);
        closePopup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (pw != null)
                {
                    pw.dismiss();
                }
            }
        });

        pw.showAsDropDown(anchor, 10, 10);
        return popupLinearLayout;
    }

    private static RelativeLayout buildRow(LayoutInflater layoutInflater, StackXItem item)
    {
        RelativeLayout itemRowLayout = (RelativeLayout) layoutInflater.inflate(R.layout.popup_item_row, null);

        TextView textView = (TextView) itemRowLayout.findViewById(R.id.popupItemContent);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(item.body));

        if (item.score != -1)
        {
            textView = (TextView) itemRowLayout.findViewById(R.id.popupItemScore);
            textView.setText(String.valueOf(item.score));
        }

        if (item.owner != null)
        {
            textView = (TextView) itemRowLayout.findViewById(R.id.popupItemAuthor);
            textView.setText(Html.fromHtml(item.owner.displayName));
        }

        return itemRowLayout;
    }
}

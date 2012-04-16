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
import com.prasanna.android.stacknetwork.model.BaseStackExchangeItem;

public class PopupBuilder
{
    public static void build(LayoutInflater layoutInflater, View anchor, BaseStackExchangeItem item, Point size)
    {
        LinearLayout rowsParentLayout = inflateLayoutAndGetRowParent(layoutInflater, anchor, size);

        rowsParentLayout.addView(buildRow(layoutInflater, item), LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public static void build(LayoutInflater layoutInflater,
            View anchor,
            ArrayList<? extends BaseStackExchangeItem> items,
            Point size)
    {
        if (items != null)
        {
            LinearLayout rowsParentLayout = inflateLayoutAndGetRowParent(layoutInflater, anchor, size);

            for (BaseStackExchangeItem item : items)
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

    private static RelativeLayout buildRow(LayoutInflater layoutInflater, BaseStackExchangeItem item)
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

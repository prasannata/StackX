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
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;

public class QuickActionMenu
{
    private final Context context;
    private ListView listView;
    private ActionItemAdapter actionItemAdapter;
    private PopupWindow popupWindow;
    private View contentView;

    private class ActionItemAdapter extends ArrayAdapter<QuickActionItem>
    {
        public ActionItemAdapter(Context context, int textViewResourceId)
        {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.quick_action_item, null);
            view.setText(getItem(position).getTitle());
            return view;
        }
    }

    public QuickActionMenu(Context context)
    {
        this.context = context;

        contentView = initPopupWindow();
        setupPopupWindowView();
    }

    private View initPopupWindow()
    {
        View v = LayoutInflater.from(context).inflate(R.layout.quick_action_popup, null);
        popupWindow = new PopupWindow(context);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources()));
        popupWindow.setContentView(v);
        contentViewHolder = new RelativeLayout(context);
        return v;
    }

    private void setupPopupWindowView()
    {
        actionItemAdapter = new ActionItemAdapter(context, R.layout.quick_action_item);
        listView = (ListView) contentView.findViewById(R.id.quickActionItemlist);
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                actionItemAdapter.getItem(position).getOnClickListener().onClick(view);
                dismiss();
            }
        });
        listView.setAdapter(actionItemAdapter);
    }

    public void addActionItem(QuickActionItem quickActionItem)
    {
        actionItemAdapter.add(quickActionItem);
        actionItemAdapter.notifyDataSetChanged();
    }

    public void removeActionItem(QuickActionItem quickActionItem)
    {
        actionItemAdapter.remove(quickActionItem);
        actionItemAdapter.notifyDataSetChanged();
    }

    public void show(View anchor)
    {
        popupWindow.showAsDropDown(anchor);
    }

    public void setOnDisimissListener(OnDismissListener onDismissListener)
    {
        popupWindow.setOnDismissListener(onDismissListener);
    }

    public void dismiss()
    {
        popupWindow.dismiss();
    }
}

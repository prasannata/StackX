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

package com.prasanna.android.stacknetwork.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.prasanna.android.stacknetwork.model.Post;

public class ItemListAdapter<T extends Post> extends ArrayAdapter<T> {
    private final ListItemView<T> listItemView;

    public interface ListItemView<T> {
        View getView(T item, int position, View convertView, ViewGroup parent);
    }

    public ItemListAdapter(Context context, int textViewResourceId, ArrayList<T> items, ListItemView<T> listItemView) {
        super(context, textViewResourceId, items);
        this.listItemView = listItemView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return listItemView.getView(getItem(position), position, convertView, parent);
    }
}

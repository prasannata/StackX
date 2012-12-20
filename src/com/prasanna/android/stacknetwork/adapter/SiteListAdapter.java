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

package com.prasanna.android.stacknetwork.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionsActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.OperatingSite;

@SuppressLint("ViewConstructor")
public class SiteListAdapter extends AbstractDraggableArrayListAdpater<Site>
{
    public static final String TAG = SiteListAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;

    public SiteListAdapter(Context context, int textViewResourceId, List<Site> sites, ListView listView)
    {
        super(context, textViewResourceId, sites, listView);

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        RelativeLayout layoutForSites = (RelativeLayout) convertView;

        if (dataSet != null && position >= 0 && position < dataSet.size())
        {
            layoutForSites = (RelativeLayout) layoutInflater.inflate(R.layout.sitelist_row, null);
            TextView textView = (TextView) layoutForSites.findViewById(R.id.siteName);
            textView.setGravity(Gravity.LEFT);
            textView.setId(dataSet.get(position).name.hashCode());
            textView.setText(dataSet.get(position).name);

            if (dataSet.get(position).userType.equals(UserType.REGISTERED))
            {
                textView = (TextView) layoutForSites.findViewById(R.id.siteUserTypeRegistered);
                textView.setVisibility(View.VISIBLE);
            }

            /*
             * Not able to make onListItemClick work when onLongClickListener is
             * set for linearLayoutForSites.
             */
            layoutForSites.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    if (reorder == false)
                    {
                        Log.d(TAG, "Clicking on list item " + position);

                        Site site = dataSet.get(position);
                        OperatingSite.setSite(site);
                        Intent startQuestionActivityIntent = new Intent(listView.getContext(), QuestionsActivity.class);
                        listView.getContext().startActivity(startQuestionActivityIntent);
                    }
                }
            });

            enableDragAndDrop(layoutForSites, position, dataSet.get(position).name);
        }

        return layoutForSites;
    }

    public List<Site> getSites()
    {
        return dataSet;
    }

    @Override
    public String getTag()
    {
        return TAG;
    }
}

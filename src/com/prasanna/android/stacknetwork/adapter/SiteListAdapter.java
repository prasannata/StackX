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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.QuestionsActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

@SuppressLint("ViewConstructor")
public class SiteListAdapter extends AbstractDraggableArrayListAdpater<Site>
{
    public static final String TAG = SiteListAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;

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

            setViewAndListenerForDefaultSiteOption(position, layoutForSites);
            setOnClickForSite(position, layoutForSites);
        }

        return layoutForSites;
    }

    private void setViewAndListenerForDefaultSiteOption(final int position, RelativeLayout layoutForSites)
    {
        String currentDefaultSite = SharedPreferencesUtil.getDefaultSiteName(getContext());
        final ImageView iv = (ImageView) layoutForSites.findViewById(R.id.isDefaultSite);
        if (isDefaultSite(currentDefaultSite, position))
            iv.setImageResource(R.drawable.circle_delft);
        iv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String currentDefaultSite = SharedPreferencesUtil.getDefaultSiteName(getContext());
                if (isDefaultSite(currentDefaultSite, position))
                {
                    iv.setImageResource(R.drawable.circle_white);
                    SharedPreferencesUtil.clearDefaultSite(getContext());
                }
                else
                {
                    iv.setImageResource(R.drawable.circle_delft);
                    SharedPreferencesUtil.setDefaultSiteName(getContext(), dataSet.get(position).name);
                    SharedPreferencesUtil.setDefaultSite(getContext(), dataSet.get(position));
                    notifyDataSetChanged();
                    Toast.makeText(getContext(), dataSet.get(position).name + " set as default site.",
                                    Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isDefaultSite(String currentDefaultSite, final int position)
    {
        return currentDefaultSite != null && currentDefaultSite.equals(dataSet.get(position).name);
    }

    private void setOnClickForSite(final int position, RelativeLayout layoutForSites)
    {
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
                    startQuestionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startQuestionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    listView.getContext().startActivity(startQuestionActivityIntent);
                    Toast.makeText(getContext(), "Use options menu to change site any time.", Toast.LENGTH_LONG).show();
                }
            }
        });
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

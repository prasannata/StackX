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

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

public class SiteListAdapter extends ArrayAdapter<Site>
{
    public static final String TAG = SiteListAdapter.class.getSimpleName();

    private OnSiteSelectedListener onSiteSelectedListener;

    public interface OnSiteSelectedListener
    {
        void onSiteSelected(Site site);
    }

    public SiteListAdapter(Context context, int textViewResourceId, List<Site> sites)
    {
        super(context, textViewResourceId, sites);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.sitelist_row, null);

        TextView textView = (TextView) convertView.findViewById(R.id.siteName);
        textView.setText(getItem(position).name);

        if (getItem(position).userType.equals(UserType.REGISTERED))
        {
            textView = (TextView) convertView.findViewById(R.id.siteUserTypeRegistered);
            textView.setVisibility(View.VISIBLE);
        }

        if (getItem(position).writePermissions != null)
        {
            for (WritePermission permission : getItem(position).writePermissions)
            {
                if (permission.canAdd & permission.canDelete & permission.canEdit)
                {
                    View writePermissionView = convertView.findViewById(R.id.writePermissionEnabled);
                    writePermissionView.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }

        setViewAndListenerForDefaultSiteOption(position, convertView);
        setOnClickForSite(position, convertView);
        return convertView;
    }

    private void setViewAndListenerForDefaultSiteOption(final int position, View layoutForSites)
    {
        String currentDefaultSite = SharedPreferencesUtil.getDefaultSiteName(getContext());
        final ImageView iv = (ImageView) layoutForSites.findViewById(R.id.isDefaultSite);
        if (isDefaultSite(currentDefaultSite, position))
            iv.setImageResource(R.drawable.circle_delft);
        else
            iv.setImageResource(R.drawable.circle_white);

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
                    SharedPreferencesUtil.setDefaultSite(getContext(), getItem(position));
                    notifyDataSetChanged();
                    Toast.makeText(getContext(), getItem(position).name + " set as default site.", Toast.LENGTH_LONG)
                                    .show();
                }
            }
        });
    }

    private boolean isDefaultSite(String currentDefaultSite, final int position)
    {
        return currentDefaultSite != null && currentDefaultSite.equals(getItem(position).name);
    }

    private void setOnClickForSite(final int position, View layoutForSites)
    {
        layoutForSites.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "Clicking on list item " + position);

                if (onSiteSelectedListener != null)
                    onSiteSelectedListener.onSiteSelected(getItem(position));
            }
        });
    }

    public void setOnSiteSelectedListener(OnSiteSelectedListener onSiteSelectedListener)
    {
        this.onSiteSelectedListener = onSiteSelectedListener;
    }
}

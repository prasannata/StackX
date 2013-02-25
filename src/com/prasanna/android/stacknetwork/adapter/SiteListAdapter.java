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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.utils.AppUtils;

public class SiteListAdapter extends ArrayAdapter<Site>
{
    private OnSiteSelectedListener onSiteSelectedListener;
    private Filter filter;

    public interface OnSiteSelectedListener
    {
        void onSiteSelected(Site site);
    }

    public SiteListAdapter(Context context, int layoutResourceId, int textViewResourceId, ArrayList<Site> sites, Filter filter)
    {
        super(context, layoutResourceId, textViewResourceId, sites);
        this.filter = filter;
    }

    static class ViewHolder
    {
        TextView siteNameView;
        TextView registeredView;
        ImageView writePermissionView;
        ImageView defaultSiteOpt;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.sitelist_row, null);
            holder = new ViewHolder();
            holder.siteNameView = (TextView) convertView.findViewById(R.id.siteName);
            holder.registeredView = (TextView) convertView.findViewById(R.id.siteUserTypeRegistered);
            holder.writePermissionView = (ImageView) convertView.findViewById(R.id.writePermissionEnabled);
            holder.defaultSiteOpt = (ImageView) convertView.findViewById(R.id.isDefaultSite);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.siteNameView.setText(Html.fromHtml(getItem(position).name));

        if (getItem(position).userType != null && getItem(position).userType.equals(UserType.REGISTERED))
            holder.registeredView.setVisibility(View.VISIBLE);
        else
            holder.registeredView.setVisibility(View.GONE);

        holder.writePermissionView.setVisibility(View.GONE);

        if (getItem(position).writePermissions != null)
        {
            for (WritePermission permission : getItem(position).writePermissions)
            {
                if (permission.canAdd & permission.canDelete & permission.canEdit)
                {
                    holder.writePermissionView.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }

        setViewAndListenerForDefaultSiteOption(position, holder);
        setOnClickForSite(position, convertView);
        return convertView;
    }

    private void setViewAndListenerForDefaultSiteOption(final int position, final ViewHolder holder)
    {
        String currentDefaultSite = AppUtils.getDefaultSiteName(getContext());
        if (isDefaultSite(currentDefaultSite, position))
            holder.defaultSiteOpt.setImageResource(R.drawable.circle_delft);
        else
            holder.defaultSiteOpt.setImageResource(R.drawable.circle_white);

        holder.defaultSiteOpt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String currentDefaultSite = AppUtils.getDefaultSiteName(getContext());
                if (isDefaultSite(currentDefaultSite, position))
                {
                    holder.defaultSiteOpt.setImageResource(R.drawable.circle_white);
                    AppUtils.clearDefaultSite(getContext());
                }
                else
                {
                    holder.defaultSiteOpt.setImageResource(R.drawable.circle_delft);
                    AppUtils.setDefaultSite(getContext(), getItem(position));
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
                if (onSiteSelectedListener != null)
                    onSiteSelectedListener.onSiteSelected(getItem(position));
            }
        });
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    public void setOnSiteSelectedListener(OnSiteSelectedListener onSiteSelectedListener)
    {
        this.onSiteSelectedListener = onSiteSelectedListener;
    }
}

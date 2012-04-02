package com.prasanna.android.stacknetwork.adapter;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;

public class SiteListAdapter extends ArrayAdapter<Site>
{
    private final List<Site> sites;
    private final Context context;

    public SiteListAdapter(Context context, int textViewResourceId, List<Site> sites)
    {
        super(context, textViewResourceId, sites);
        this.context = context;
        this.sites = sites;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout linearLayoutForSites = (LinearLayout) convertView;

        if (sites != null && position >= 0 && position < sites.size())
        {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            linearLayoutForSites = (LinearLayout) vi.inflate(R.layout.sitelist_row, null);

            TextView textView = (TextView) linearLayoutForSites.findViewById(R.id.siteName);
            textView.setGravity(Gravity.LEFT);
            textView.setId(sites.get(position).name.hashCode());
            textView.setText(sites.get(position).name);
        }
        return linearLayoutForSites;
    }
}

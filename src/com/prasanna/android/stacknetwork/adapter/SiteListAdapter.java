package com.prasanna.android.stacknetwork.adapter;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
	View view = convertView;
	if (view == null)
	{
	    LayoutInflater vi = (LayoutInflater) context
		    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    view = vi.inflate(R.layout.sitelist_row, null);
	}
	final Site site = sites.get(position);
	TextView textView = (TextView) view.findViewById(R.id.siteName);
	textView.setGravity(Gravity.LEFT);
	textView.setId(site.getName().hashCode());
	textView.setText(site.getName());
	return view;
    }
}

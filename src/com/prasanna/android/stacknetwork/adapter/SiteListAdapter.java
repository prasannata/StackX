package com.prasanna.android.stacknetwork.adapter;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;

public class SiteListAdapter extends AbstractDraggableArrayListAdpater<Site>
{
    public static final String TAG = SiteListAdapter.class.getSimpleName();

    private final Context context;

    public SiteListAdapter(Context context, int textViewResourceId, List<Site> sites, ListView listView)
    {
	super(context, textViewResourceId, sites, listView);
	this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
	LinearLayout linearLayoutForSites = (LinearLayout) convertView;

	if (dataSet != null && position >= 0 && position < dataSet.size())
	{
	    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    linearLayoutForSites = (LinearLayout) vi.inflate(R.layout.sitelist_row, null);

	    TextView textView = (TextView) linearLayoutForSites.findViewById(R.id.siteName);
	    textView.setGravity(Gravity.LEFT);
	    textView.setId(dataSet.get(position).name.hashCode());
	    textView.setText(dataSet.get(position).name);

	    if (dataSet.get(position).userType.equals(UserType.REGISTERED))
	    {
		textView = (TextView) linearLayoutForSites.findViewById(R.id.siteUserTypeRegistered);
		textView.setVisibility(View.VISIBLE);
	    }

	    enableDragAndDrop(linearLayoutForSites, position, dataSet.get(position).name);
	}

	return linearLayoutForSites;
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

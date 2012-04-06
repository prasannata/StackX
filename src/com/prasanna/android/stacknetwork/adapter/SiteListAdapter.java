package com.prasanna.android.stacknetwork.adapter;

import java.util.List;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;

public class SiteListAdapter extends ArrayAdapter<Site>
{
    public static final String TAG = SiteListAdapter.class.getSimpleName();
    public static final String POSITION = "POSITION";
    public static final String DRAG = "DRAG";

    private final List<Site> sites;
    private final Context context;
    private final ListView listView;
    private int numRowsPassed = 0;
    private int lastRecordedY = -1;
    private int startOffset = 0;
    private boolean reorder = false;

    public class SiteDragAndDropListener implements View.OnDragListener
    {
        @Override
        public boolean onDrag(View paramView, DragEvent paramDragEvent)
        {
            Log.d(TAG, "On drag enter");

            if (reorder == true)
            {
                switch (paramDragEvent.getAction())
                {
                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.d(TAG, "ACTION_DRAG_STARTED");
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d(TAG, "ACTION_DRAG_ENDED");
                        numRowsPassed = 0;
                        lastRecordedY = -1;
                        notifyDataSetChanged();
                        return true;
                    case DragEvent.ACTION_DROP:
                        Log.d(TAG, "ACTION_DROP");
                        Item clipDataItem = paramDragEvent.getClipData().getItemAt(0);
                        Intent intent = clipDataItem.getIntent();
                        int draggedListItemPosition = intent.getIntExtra(POSITION, -1);
                        if (draggedListItemPosition != -1)
                        {
                            int currentRowX = (int) paramDragEvent.getX();
                            int currentRowY = startOffset + (numRowsPassed * paramView.getHeight());
                            int currentRowPosition = listView.pointToPosition(currentRowX, currentRowY);
                            sites.add(currentRowPosition + 1, sites.get(draggedListItemPosition));
                            sites.remove(draggedListItemPosition);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        Log.d(TAG, "ACTION_DRAG_LOCATION");

                        int y = (int) paramDragEvent.getY();

                        paramView.setBackgroundColor(Color.GRAY);

                        if (y < lastRecordedY)
                        {
                            numRowsPassed++;
                            lastRecordedY = -1;
                        }

                        lastRecordedY = y;
                        return true;
                }
            }

            return false;
        }
    }

    public SiteListAdapter(Context context, int textViewResourceId, List<Site> sites, ListView listView)
    {
        super(context, textViewResourceId, sites);
        this.context = context;
        this.sites = sites;
        this.listView = listView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
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

            if (sites.get(position).userType.equals(UserType.REGISTERED))
            {
                textView = (TextView) linearLayoutForSites.findViewById(R.id.siteUserTypeRegistered);
                textView.setVisibility(View.VISIBLE);
            }

            linearLayoutForSites.setOnDragListener(new SiteDragAndDropListener());
            linearLayoutForSites.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View paramView)
                {
                    if (reorder == true)
                    {
                        Intent intent = new Intent(DRAG);
                        intent.putExtra(POSITION, position);
                        ClipData.Item clipDataItem = new ClipData.Item(intent);

                        ClipData dragData = new ClipData(sites.get(position).name, new String[]
                        { "text/plain" }, clipDataItem);

                        startOffset = paramView.getBottom();

                        paramView.startDrag(dragData, new DragShadowBuilder(paramView), null, 0);
                    }
                    return false;
                }
            });
        }
        return linearLayoutForSites;
    }

    public void toggleReorder()
    {
        reorder = !reorder;

        Log.d(TAG, "Reorder = " + reorder);
    }
}

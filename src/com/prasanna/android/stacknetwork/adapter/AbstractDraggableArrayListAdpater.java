package com.prasanna.android.stacknetwork.adapter;

import java.util.List;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class AbstractDraggableArrayListAdpater<T> extends ArrayAdapter<T>
{
    protected static final String POSITION = "POSITION";
    protected static final String DRAG = "DRAG";

    protected final ListView listView;
    protected List<T> dataSet;

    protected boolean reorder = false;
    protected boolean changed;

    protected abstract String getTag();

    private int startOffset = 0;
    private int lastVisitedViewBottom = 0;

    protected class ListViewDragListener implements View.OnDragListener
    {
	@Override
	public boolean onDrag(View paramView, DragEvent paramDragEvent)
	{
	    if (reorder == true)
	    {
		switch (paramDragEvent.getAction())
		{
		    case DragEvent.ACTION_DRAG_STARTED:
			Log.d(getTag(), "ACTION_DRAG_STARTED");
			return true;
		    case DragEvent.ACTION_DRAG_ENDED:
			Log.d(getTag(), "ACTION_DRAG_ENDED");
			notifyDataSetChanged();
			return true;
		    case DragEvent.ACTION_DROP:
			Log.d(getTag(), "ACTION_DROP");
			dropItem(paramView, paramDragEvent);
			return true;
		    case DragEvent.ACTION_DRAG_ENTERED:
			Log.d(getTag(), "ACTION_DRAG_ENTERED");

			lastVisitedViewBottom = paramView.getBottom();
			paramView.setBackgroundColor(Color.GRAY);

			Log.d(getTag(), "droppedViewBottom: " + lastVisitedViewBottom);
			Log.d(getTag(), "listView.getHeight(): " + listView.getHeight());
			if (Math.abs(listView.getHeight() - lastVisitedViewBottom) < 2.9 * paramView.getHeight())
			{
			    listView.smoothScrollBy(paramView.getHeight(), 5000);
			}
			return true;
		}
	    }

	    return false;
	}

	private void dropItem(View paramView, DragEvent paramDragEvent)
	{
	    Item clipDataItem = paramDragEvent.getClipData().getItemAt(0);
	    Intent intent = clipDataItem.getIntent();
	    int draggedItemPosition = intent.getIntExtra(POSITION, -1);
	    int distance = lastVisitedViewBottom - startOffset;
	    if (draggedItemPosition != -1 && Math.abs(distance) > paramView.getHeight())
	    {
		int currentRowPosition = listView.pointToPosition((int) paramDragEvent.getX(),
		                lastVisitedViewBottom - 1);

		if (currentRowPosition != ListView.INVALID_POSITION)
		{
		    Log.d(getTag(), "distance: " + distance);
		    Log.d(getTag(), "draggedItemPosition: " + draggedItemPosition);
		    Log.d(getTag(), "currentRowPosition: " + currentRowPosition);

		    dataSet.add(currentRowPosition, dataSet.remove(draggedItemPosition));

		    changed = true;
		}
	    }
	}
    }

    public AbstractDraggableArrayListAdpater(Context context, int textViewResourceId, List<T> dataSet, ListView listView)
    {
	super(context, textViewResourceId, dataSet);
	this.dataSet = dataSet;
	this.listView = listView;
    }

    public boolean wasReordered()
    {
	return changed;
    }

    protected void enableDragAndDrop(View view, final int itemPosition, final String clipDataLabel)
    {
	view.setOnDragListener(new ListViewDragListener());

	view.setOnLongClickListener(new View.OnLongClickListener()
	{
	    @Override
	    public boolean onLongClick(View paramView)
	    {
		if (reorder == true)
		{
		    Intent intent = new Intent(DRAG);
		    intent.putExtra(POSITION, itemPosition);
		    ClipData.Item clipDataItem = new ClipData.Item(intent);

		    ClipData dragData = new ClipData(clipDataLabel, new String[] { "text/plain" }, clipDataItem);

		    startOffset = paramView.getBottom();

		    paramView.startDrag(dragData, new DragShadowBuilder(paramView), null, 0);

		    return true;
		}

		return false;
	    }
	});
    }

    public void overwriteDataset(List<T> dataSet)
    {
	Log.d(getTag(), "Overwriting");

	this.dataSet = dataSet;

	notifyDataSetChanged();
    }

    public boolean toggleReorderFlag()
    {
	reorder = !reorder;

	if (reorder == true)
	{
	    changed = false;
	}

	Log.d(getTag(), "Reorder = " + reorder);

	return reorder;
    }
}

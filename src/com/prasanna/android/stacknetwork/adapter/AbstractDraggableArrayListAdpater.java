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

                        paramView.setBackgroundColor(Color.GRAY);
                        lastVisitedViewBottom = paramView.getBottom();
                        Log.d(getTag(), "lastVisitedViewBottom: " + lastVisitedViewBottom);

                        if (Math.abs(listView.getHeight() - lastVisitedViewBottom) < 2.9 * paramView.getHeight())
                        {
                            listView.smoothScrollBy(paramView.getHeight(), 300);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d(getTag(), "ACTION_DRAG_EXITED");
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
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

            Log.d(getTag(), "distance: " + distance);
            Log.d(getTag(), "draggedItemPosition: " + draggedItemPosition);

            if (draggedItemPosition != ListView.INVALID_POSITION && Math.abs(distance) > paramView.getHeight())
            {
                int currentItemPosition = listView.pointToPosition((int) paramDragEvent.getX(),
                        lastVisitedViewBottom - 1);

                if (currentItemPosition != ListView.INVALID_POSITION)
                {
                    Log.d(getTag(), "currentItemPosition: " + currentItemPosition);

                    dataSet.add(currentItemPosition, dataSet.remove(draggedItemPosition));

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

                    ClipData dragData = new ClipData(clipDataLabel, new String[]
                    { "text/plain" }, clipDataItem);

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

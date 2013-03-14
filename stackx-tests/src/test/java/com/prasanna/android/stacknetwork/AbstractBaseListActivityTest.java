package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowArrayAdapter;
import org.robolectric.shadows.ShadowListView;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class AbstractBaseListActivityTest<T> extends AbstractBaseActivityTest
{
    protected abstract View assertListItem(ListAdapter listAdpater, int position, T expectedItem);

    @SuppressWarnings("unchecked")
    protected ShadowListView shawdowListView(ListView listView, ArrayList<T> list)
    {
        ShadowListView shadowOfListView = Robolectric.shadowOf(listView);
        ShadowArrayAdapter<T> shadowArrayAdapter = shadowArrayAdapter(list, (ArrayAdapter<T>) listView.getAdapter());
        assertTrue(shadowOfListView.getAdapter().getCount() == list.size());
        assertTrue(shadowArrayAdapter.areAllItemsEnabled());
        return shadowOfListView;
    }

    @SuppressWarnings("unchecked")
    protected ShadowArrayAdapter<T> shadowArrayAdapter(ArrayList<T> list, ArrayAdapter<T> arrayAdapter)
    {
        ShadowArrayAdapter<T> siteListShadowAdapter = Robolectric.shadowOf(arrayAdapter);
        for (T item : list)
            siteListShadowAdapter.add(item);
        return siteListShadowAdapter;
    }

    protected ArrayList<View> assertListView(ListView listView, ArrayList<T> list)
    {
        assertTrue(listView.getCount() == 0);
        shawdowListView(listView, list);
        ArrayList<View> viewList = new ArrayList<View>();
        for (T item : list)
            viewList.add(assertListItem(listView.getAdapter(), list.indexOf(item), item));
        assertFalse(viewList.isEmpty());
        assertEquals(list.size(), viewList.size());
        return viewList;
    }
}

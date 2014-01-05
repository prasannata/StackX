package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class AbstractBaseListActivityTest<T> extends AbstractBaseActivityTest {
  protected abstract View assertListItem(ListAdapter listAdpater, int position, T expectedItem);

  protected ArrayList<View> assertListViewAndGetListItemViews(ListView listView, ArrayList<T> list) {
    ArrayList<View> viewList = new ArrayList<View>();    
    for (T item : list)
      viewList.add(assertListItem(listView.getAdapter(), list.indexOf(item), item));

    assertEquals(list.size(), viewList.size());
    return viewList;
  }
}

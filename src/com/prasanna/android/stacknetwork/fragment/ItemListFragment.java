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

package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.StackXItem;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.AbstractIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class ItemListFragment<T extends StackXItem> extends ListFragment implements OnScrollListener,
                StackXRestQueryResultReceiver
{
    private static final String TAG = ItemListFragment.class.getSimpleName();

    private boolean activityCreated = false;
    private ProgressBar progressBar;
    private StackXPage<T> currentPageObject;

    protected boolean serviceRunning = false;
    protected RestQueryResultReceiver resultReceiver;
    protected List<StackXPage<T>> pages;
    protected LinearLayout itemsContainer;
    protected ArrayList<T> items;
    protected ItemListAdapter<T> itemListAdapter;

    protected abstract String getReceiverExtraName();

    protected abstract void startIntentService();

    protected abstract String getLogTag();

    protected boolean isServiceRunning()
    {
        return serviceRunning;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        if (resultReceiver == null)
            resultReceiver = new RestQueryResultReceiver(new Handler());

        resultReceiver.setReceiver(this);

        if (pages == null)
            pages = new ArrayList<StackXPage<T>>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        if (!activityCreated)
        {
            getListView().addFooterView(getProgressBar());
            setListAdapter(itemListAdapter);
            getListView().setOnScrollListener(this);
            activityCreated = true;
        }

        if (savedInstanceState != null)
        {
            items = (ArrayList<T>) savedInstanceState.getSerializable(StringConstants.ITEMS);
            if (items != null)
                itemListAdapter.addAll(items);
        }
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");
        
        super.onResume();

        if (itemListAdapter != null && itemListAdapter.getCount() > 0)
            itemListAdapter.notifyDataSetChanged();
        else
        {
            if (itemListAdapter != null &&items != null && !items.isEmpty())
                itemListAdapter.addAll(items);
        }
    }

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");

        super.onSaveInstanceState(outState);
    }

    protected void showProgressBar()
    {
        getProgressBar().setVisibility(View.VISIBLE);
    }

    protected void dismissProgressBar()
    {
        getProgressBar().setVisibility(View.GONE);
    }

    protected Intent getIntentForService(Class<?> clazz, String action)
    {
        if (!serviceRunning)
        {
            Intent intentForService = new Intent(getActivity().getApplicationContext(), clazz);
            intentForService.setAction(action);
            return intentForService;
        }

        return null;
    }

    protected void startService(Intent intent)
    {
        if (!isServiceRunning() && intent != null)
        {
            getActivity().startService(intent);
            serviceRunning = true;
        }
    }

    protected void stopService(Intent intent)
    {
        if (intent != null)
        {
            getActivity().stopService(intent);
            serviceRunning = false;
        }
    }

    protected ViewGroup getParentLayout()
    {
        return itemsContainer;
    }

    public void refresh()
    {
        removeErrorViewIfShown();

        startIntentService();
    }

    protected void removeErrorViewIfShown()
    {
        if (getListView().getVisibility() == View.GONE)
            getListView().setVisibility(View.VISIBLE);

        View errorView = getParentLayout().findViewById(R.id.errorLayout);
        if (errorView != null)
            getParentLayout().removeView(errorView);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        serviceRunning = false;

        if (resultCode == AbstractIntentService.ERROR)
        {
            onHttpError((HttpException) resultData.getSerializable(StringConstants.EXCEPTION));
        }
        else
        {
            currentPageObject = (StackXPage<T>) resultData.getSerializable(getReceiverExtraName());
            if (currentPageObject != null)
            {
                pages.add(currentPageObject);
                displayItems(currentPageObject.items);
            }
        }
    }

    protected void displayItems(ArrayList<T> newItems)
    {
        dismissProgressBar();

        if (itemListAdapter != null && newItems != null)
        {
            Log.d(TAG, "Updating list adpater with items");

            if (items == null)
                items = new ArrayList<T>();

            items.addAll(newItems);

            if (items.isEmpty())
            {
                if (itemsContainer.findViewById(R.id.emptyStatus) == null)
                {
                    View noItemsFoundView = getActivity().getLayoutInflater().inflate(R.layout.empty_items, null);
                    TextView tv = (TextView) noItemsFoundView.findViewById(R.id.emptyStatus);
                    tv.setText("Nada");
                    tv.setTextSize(15f);
                    itemsContainer.addView(noItemsFoundView);
                }
            }
            else
            {
                itemListAdapter.addAll(newItems);
            }
        }
    }

    private void onHttpError(HttpException e)
    {
        Log.d(TAG, "Http error " + e.getStatusCode() + " " + e.getErrorResponse());

        dismissProgressBar();

        getListView().setVisibility(View.GONE);
        getParentLayout().addView(AppUtils.getErrorView(getActivity(), e));
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (!isServiceRunning() && totalItemCount >= StackUri.QueryParamDefaultValues.PAGE_SIZE
                        && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1))
        {
            Log.d(TAG, "onScroll reached bottom threshold. Fetching more questions");

            if (currentPageObject != null && currentPageObject.hasMore)
                startIntentService();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        Log.v(TAG, "onScrollStateChanged");
    }

    protected ProgressBar getProgressBar()
    {
        if (progressBar == null)
            progressBar = (ProgressBar) getActivity().getLayoutInflater().inflate(R.layout.progress_bar, null);
        return progressBar;
    }
}

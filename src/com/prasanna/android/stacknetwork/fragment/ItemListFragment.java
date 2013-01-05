/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpErrorBroadcastReceiver;
import com.prasanna.android.listener.HttpErrorListener;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.StackXItem;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.ErrorIntentAction;

public abstract class ItemListFragment<T extends StackXItem> extends ListFragment implements OnScrollListener,
                HttpErrorListener, StackXRestQueryResultReceiver
{
    private static final String TAG = ItemListFragment.class.getSimpleName();

    private boolean serviceRunning = false;
    private HttpErrorBroadcastReceiver httpErrorBroadcastReceiver;
    private ProgressBar progressBar;

    protected RestQueryResultReceiver resultReceiver;
    protected List<StackXPage<T>> pages;
    protected LinearLayout itemsContainer;
    protected ItemListAdapter<T> itemListAdapter;
    private StackXPage<T> currentPageObject;

    protected abstract String getReceiverExtraName();

    protected abstract void startIntentService();

    protected abstract String getLogTag();

    public interface OnContextItemSelectedListener<T>
    {
	boolean onContextItemSelected(MenuItem item, T stackXItem);
    }

    protected void registerHttpErrorReceiver()
    {
	httpErrorBroadcastReceiver = new HttpErrorBroadcastReceiver(this);

	IntentFilter filter = new IntentFilter(ErrorIntentAction.HTTP_ERROR.getAction());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().registerReceiver(httpErrorBroadcastReceiver, filter);
    }

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

	registerHttpErrorReceiver();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	Log.d(TAG, "onActivityCreated");

	super.onActivityCreated(savedInstanceState);

	getListView().addFooterView(getProgressBar());
	setListAdapter(itemListAdapter);
	getListView().setOnScrollListener(this);
    }

    @Override
    public void onResume()
    {
	super.onResume();

	if (itemListAdapter != null)
	    itemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop()
    {
	Log.d(TAG, "onStop");

	super.onStop();

	unregisterReceivers();
    }

    protected void unregisterReceivers()
    {
	try
	{
	    Log.d(TAG, "unregistering receivers");
	    getActivity().unregisterReceiver(httpErrorBroadcastReceiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(getLogTag(), e.getMessage());
	}
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
	showProgressBar();
	startIntentService();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
	serviceRunning = false;

	currentPageObject = (StackXPage<T>) resultData.getSerializable(getReceiverExtraName());
	if (currentPageObject != null)
	{
	    pages.add(currentPageObject);
	    displayItems(currentPageObject.items);
	}
    }

    protected void displayItems(ArrayList<T> newItems)
    {
	dismissProgressBar();

	if (itemListAdapter != null && newItems != null)
	{
	    Log.d(TAG, "Updating list adpater with questions");
	    itemListAdapter.addAll(newItems);
	}
    }

    @Override
    public void onHttpError(int code, String text)
    {
	Log.d(TAG, "Http error " + code + " " + text);

	dismissProgressBar();
	RelativeLayout errorDisplayLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.error,
	                null);
	TextView textView = (TextView) errorDisplayLayout.findViewById(R.id.errorMsg);
	textView.setText(code + " " + text);

	getParentLayout().removeAllViews();
	getParentLayout().addView(errorDisplayLayout);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
	if (!isServiceRunning() && totalItemCount >= StackUri.QueryParamDefaultValues.PAGE_SIZE
	                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1))
	{
	    Log.d(TAG, "onScroll reached bottom threshold. Fetching more questions");

	    if (currentPageObject != null && currentPageObject.hasMore)
	    {
		showProgressBar();
		startIntentService();
	    }
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

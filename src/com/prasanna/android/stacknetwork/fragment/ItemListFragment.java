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

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpErrorBroadcastReceiver;
import com.prasanna.android.listener.HttpErrorListener;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.BaseStackExchangeItem;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.ErrorIntentAction;
import com.prasanna.android.stacknetwork.utils.StackUri;

public abstract class ItemListFragment<T extends BaseStackExchangeItem> extends ListFragment implements
                OnScrollListener, HttpErrorListener
{
    private Intent intentForService;
    private boolean serviceRunning = false;
    private HttpErrorBroadcastReceiver httpErrorBroadcastReceiver;
    private LinearLayout loadingProgressView;

    protected LinearLayout itemsContainer;
    protected ItemListAdapter<T> itemListAdapter;
    
    protected abstract String getReceiverExtraName();
    protected abstract void startIntentService();
    protected abstract void registerReceiver();
    protected abstract String getLogTag();

    protected BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    Log.d(getLogTag(), "Receiver invoked: " + intent.getExtras());

	    serviceRunning = false;

	    displayItems((ArrayList<T>) intent.getSerializableExtra(getReceiverExtraName()));
	}
    };

    protected void registerHttpErrorReceiver()
    {
	httpErrorBroadcastReceiver = new HttpErrorBroadcastReceiver(this);

	IntentFilter filter = new IntentFilter(ErrorIntentAction.HTTP_ERROR.name());
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
	super.onCreate(savedInstanceState);

	registerHttpErrorReceiver();
    }

    @Override
    public void onDestroy()
    {
	super.onDestroy();

	stopServiceAndUnregisterReceivers();
    }

    @Override
    public void onStop()
    {
	super.onStop();

	stopServiceAndUnregisterReceivers();
    }

    protected void stopServiceAndUnregisterReceivers()
    {
	stopService();

	try
	{
	    getActivity().unregisterReceiver(receiver);
	    getActivity().unregisterReceiver(httpErrorBroadcastReceiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(getLogTag(), e.getMessage());
	}
    }

    protected void showLoadingSpinningWheel()
    {
	if (loadingProgressView == null)
	{
	    loadingProgressView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.loading_progress,
		            null);
	    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT);
	    layoutParams.setMargins(0, 15, 0, 15);
	    layoutParams.gravity = Gravity.CENTER | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
	    getParentLayout().addView(loadingProgressView, layoutParams);
	}
    }

    protected void dismissLoadingSpinningWheel()
    {
	if (loadingProgressView != null)
	{
	    loadingProgressView.setVisibility(View.GONE);
	    getParentLayout().removeView(loadingProgressView);
	    loadingProgressView = null;
	}
    }

    protected Intent getIntentForService(Class<?> clazz, String action)
    {
	intentForService = new Intent(getActivity().getApplicationContext(), clazz);
	intentForService.setAction(action);
	return intentForService;
    }

    protected void startService()
    {
	if (serviceRunning)
	{
	    Log.d(getLogTag(), "Service is already running");
	}
	else if (intentForService != null)
	{
	    getActivity().startService(intentForService);
	    serviceRunning = true;
	}
    }

    protected void stopService()
    {
	if (intentForService != null)
	{
	    getActivity().stopService(intentForService);
	    serviceRunning = false;
	}
    }

    protected ViewGroup getParentLayout()
    {
	return itemsContainer;
    }

    public void refresh()
    {
	stopServiceAndUnregisterReceivers();

	registerReceiver();

	showLoadingSpinningWheel();

	startIntentService();
    }

    protected void displayItems(ArrayList<T> newItems)
    {
	dismissLoadingSpinningWheel();

	if (itemListAdapter != null)
	{
	    Log.d(getLogTag(), "Updating list adpater with questions");

	    itemListAdapter.addAll(newItems);
	}
    }

    @Override
    public void onHttpError(int code, String text)
    {
	Log.d(getLogTag(), "Http error " + code + " " + text);

	dismissLoadingSpinningWheel();
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
	                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 3))
	{
	    Log.d(getLogTag(), "onScroll reached bottom threshold. Fetching more questions");

	    if (serviceRunning == false)
	    {
		showLoadingSpinningWheel();
		startIntentService();
	    }
	}
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
	Log.v(getLogTag(), "onScrollStateChanged");
    }
}
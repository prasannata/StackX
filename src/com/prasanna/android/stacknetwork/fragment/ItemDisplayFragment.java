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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpErrorBroadcastReceiver;
import com.prasanna.android.listener.HttpErrorListener;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.BaseStackExchangeItem;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.ErrorIntentAction;

public abstract class ItemDisplayFragment<T extends BaseStackExchangeItem> extends Fragment implements
                ScrollableFragment, HttpErrorListener
{
    private Intent intentForService;

    protected boolean serviceRunning = false;

    protected LinearLayout itemsContainer;

    protected ArrayList<T> items = new ArrayList<T>();

    private ProgressDialog loadingDialog;

    protected LinearLayout loadingProgressView;

    public abstract String getReceiverExtraName();

    public abstract void startIntentService();

    protected abstract void registerReceiver();

    protected abstract void displayItems();

    protected abstract String getLogTag();

    private HttpErrorBroadcastReceiver httpErrorBroadcastReceiver;

    protected BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    Log.d(getLogTag(), "Receiver invoked: " + intent.getExtras());

	    items.addAll((ArrayList<T>) intent.getSerializableExtra(getReceiverExtraName()));

	    displayItems();
	}
    };

    protected void registerHttpErrorReceiver()
    {
	httpErrorBroadcastReceiver = new HttpErrorBroadcastReceiver(this);

	IntentFilter filter = new IntentFilter(ErrorIntentAction.HTTP_ERROR.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().registerReceiver(httpErrorBroadcastReceiver, filter);
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

	stopServiceAndUnregisterReceiver();
    }

    @Override
    public void onStop()
    {
	super.onStop();

	stopServiceAndUnregisterReceiver();
    }

    protected void stopServiceAndUnregisterReceiver()
    {
	if (intentForService != null)
	{
	    getActivity().stopService(intentForService);
	}

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

    @Override
    public void onScrollToBottom()
    {
	if (serviceRunning == false)
	{
	    loadingProgressView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.loading_progress,
		            null);
	    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT);
	    layoutParams.setMargins(0, 15, 0, 15);
	    itemsContainer.addView(loadingProgressView, layoutParams);
	    startIntentService();
	}
    }

    protected void showLoadingDialog()
    {
	if (loadingDialog == null || loadingDialog.isShowing() == false)
	{
	    loadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
	}
    }

    protected void dismissLoadingDialog()
    {
	if (loadingDialog != null && loadingDialog.isShowing() == true)
	{
	    loadingDialog.dismiss();
	}
    }

    protected Intent getIntentForService(Class<?> clazz, String action)
    {
	intentForService = new Intent(getActivity().getApplicationContext(), clazz);
	intentForService.setAction(action);
	return intentForService;
    }

    public void refresh()
    {
	stopServiceAndUnregisterReceiver();

	itemsContainer.removeAllViews();

	registerReceiver();

	showLoadingDialog();

	startIntentService();
    }

    @Override
    public void onHttpError(int code, String text)
    {
	Log.d(getLogTag(), "Http error " + code + " " + text);

	dismissLoadingDialog();

	RelativeLayout errorDisplayLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.error,
	                null);
	TextView textView = (TextView) errorDisplayLayout.findViewById(R.id.errorMsg);

	textView.setText(code + " " + text);

	itemsContainer.removeAllViews();
	itemsContainer.addView(errorDisplayLayout);
    }
}

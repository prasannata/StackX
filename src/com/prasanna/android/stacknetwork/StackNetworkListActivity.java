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

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpErrorBroadcastReceiver;
import com.prasanna.android.listener.HttpErrorListener;
import com.prasanna.android.stacknetwork.adapter.SiteListAdapter;
import com.prasanna.android.stacknetwork.intent.UserSitesIntentService;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class StackNetworkListActivity extends ListActivity implements HttpErrorListener
{
    private static final String TAG = StackNetworkListActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    private Intent sitesIntent = null;

    private ArrayList<Site> sites;

    private SiteListAdapter siteListAdapter;

    // private Button reorderDoneToggleButton;
    //
    // private Button cancelReorderButton;
    //
    // private boolean reorder = false;
    //
    // private TextView dragDropHint;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    processReceiverIntent(context, intent);
	}
    };

    private HttpErrorBroadcastReceiver httpErrorBroadcastReceiver;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.sitelist);

	// setupSiteSorting();
	httpErrorBroadcastReceiver = new HttpErrorBroadcastReceiver(this);

	Object lastSavedInstance = null;
	if (savedInstanceState != null)
	{
	    lastSavedInstance = savedInstanceState.getSerializable(StringConstants.SITES);
	}

	if (lastSavedInstance == null && CacheUtils.hasSiteListCache(getCacheDir()) == false)
	{
	    registerReceiverAndStartService();
	}
	else
	{
	    if (lastSavedInstance != null)
	    {
		sites = (ArrayList<Site>) lastSavedInstance;
		updateView(sites);
	    }
	    else
	    {
		sites = CacheUtils.fetchSiteListFromCache(getCacheDir());
		updateView(sites);
	    }
	}
    }

    //
    // private void setupSiteSorting()
    // {
    // reorderDoneToggleButton = (Button) findViewById(R.id.reorderDoneToggle);
    // dragDropHint = (TextView) findViewById(R.id.dragAndDropHint);
    // cancelReorderButton = (Button) findViewById(R.id.cancelSiteListReorder);
    //
    // reorderDoneToggleButton.setOnClickListener(new View.OnClickListener()
    // {
    // @Override
    // public void onClick(View v)
    // {
    // if (siteListAdapter != null)
    // {
    // reorder = siteListAdapter.toggleReorderFlag();
    // toggleReorderDoneButtonText();
    // }
    // }
    // });
    //
    // cancelReorderButton.setOnClickListener(new View.OnClickListener()
    // {
    //
    // @Override
    // public void onClick(View v)
    // {
    // Log.d(TAG, "Cancelling ");
    //
    // if (siteListAdapter != null)
    // {
    // // Poor to just overwrite from cache.
    // siteListAdapter.overwriteDataset(CacheUtils.fetchSiteListFromCache(getApplicationContext()));
    //
    // reorder = siteListAdapter.toggleReorderFlag();
    // v.setVisibility(View.INVISIBLE);
    // toggleReorderDoneButtonText();
    // }
    // }
    // });
    // }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
     * android.view.View, int, long)
     * 
     * This nor listView.setOnItemClickListener is not getting called when each
     * row of listView is made up of linear layout and that linear layout has
     * onLongClickListener. Instead I had to set an onClickListener on the
     * linear layout.
     */
    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id)
    {
	Log.d(TAG, "Clicking on list item " + position);

	Site site = sites.get(position);
	OperatingSite.setSite(site);
	unregisterReceiver(httpErrorBroadcastReceiver);
	Intent startQuestionActivityIntent = new Intent(this, QuestionsActivity.class);
	startActivity(startQuestionActivityIntent);
    }

    private void stopServiceAndUnregisterReceiver()
    {
	if (sitesIntent != null)
	{
	    stopService(sitesIntent);
	}

	try
	{
	    unregisterReceiver(receiver);
	    unregisterReceiver(httpErrorBroadcastReceiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    @Override
    protected void onDestroy()
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

    private void registerReceiverAndStartService()
    {
	progressDialog = ProgressDialog.show(StackNetworkListActivity.this, "", getString(R.string.loadingSites));

	registerReceiver();

	startIntentService();
    }

    private void startIntentService()
    {
	sitesIntent = new Intent(this, UserSitesIntentService.class);
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
	{
	    sitesIntent.putExtra(StringConstants.ACCESS_TOKEN,
		            sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null));
	}
	startService(sitesIntent);
    }

    private void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(StringConstants.SITES);
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(receiver, filter);
    }

    @SuppressWarnings("unchecked")
    private void processReceiverIntent(Context context, Intent intent)
    {
	if (progressDialog != null)
	{
	    progressDialog.dismiss();
	}

	sites = (ArrayList<Site>) intent.getSerializableExtra(StringConstants.SITES);

	if (sites != null)
	{
	    CacheUtils.cacheSiteList(getCacheDir(), sites);
	    updateView(sites);
	}
    }

    private void updateView(ArrayList<Site> sites)
    {
	if (sites != null && sites.isEmpty() == false)
	{
	    siteListAdapter = new SiteListAdapter(this, R.layout.sitelist_row, sites, getListView());
	    setListAdapter(siteListAdapter);
	}
    }

    // private void toggleReorderDoneButtonText()
    // {
    // if (siteListAdapter.wasReordered() == true)
    // {
    // Log.d(TAG, "Persisting change to cache");
    // CacheUtils.cacheSiteList(getApplicationContext(), (ArrayList<Site>)
    // siteListAdapter.getSites());
    // }
    //
    // dragDropHint.setVisibility(reorder ? View.VISIBLE : View.INVISIBLE);
    // cancelReorderButton.setVisibility(reorder ? View.VISIBLE :
    // View.INVISIBLE);
    // reorderDoneToggleButton.setBackgroundResource(reorder ?
    // R.drawable.accept_white : R.drawable.sort_white);
    // }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
	outState.putSerializable(StringConstants.SITES, sites);
	super.onSaveInstanceState(outState);
    }

    @Override
    public void onHttpError(int code, String text)
    {
	RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.error, null);
	TextView textView = (TextView) relativeLayout.findViewById(R.id.errorMsg);

	textView.setText(code + " " + text);
    }
}

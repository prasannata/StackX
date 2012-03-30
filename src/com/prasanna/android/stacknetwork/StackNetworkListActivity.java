package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.prasanna.android.http.HttpHelper;
import com.prasanna.android.stacknetwork.adapter.SiteListAdapter;
import com.prasanna.android.stacknetwork.intent.UserSitesIntentService;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class StackNetworkListActivity extends ListActivity
{
    private static final String TAG = StackNetworkListActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    private Intent sitesIntent = null;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    processReceiverIntent(context, intent);
	}
    };

    private ArrayList<Site> sites;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	HttpHelper.getInstance().setHost(getString(R.string.stackExchangeDomain));

	registerReceiverAndStartService();
    }

    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id)
    {
	Site site = sites.get(position);
	OperatingSite.setSite(site);
	Intent myIntent = new Intent(this, QuestionsActivity.class);
	startActivity(myIntent);
    }

    @Override
    protected void onDestroy()
    {
	super.onDestroy();
	stopServiceAndUnregisterReceiver();
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
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    @Override
    public void onStop()
    {
	super.onStop();

	stopServiceAndUnregisterReceiver();
    }

    private void registerReceiverAndStartService()
    {
	progressDialog = ProgressDialog.show(StackNetworkListActivity.this, "",
	        getString(R.string.loadingSites));

	registerQuestionsReceiver();

	startIntentService();
    }

    private void startIntentService()
    {
	sitesIntent = new Intent(this, UserSitesIntentService.class);
	startService(sitesIntent);
    }

    private void registerQuestionsReceiver()
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
	updateView(sites);
    }

    private void updateView(ArrayList<Site> sites)
    {
	if (sites != null)
	{
	    setListAdapter(new SiteListAdapter(this, R.layout.sitelist_row, sites));
	}
    }
}

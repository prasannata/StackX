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

import com.prasanna.android.http.HttpHelper;
import com.prasanna.android.stacknetwork.adapter.SiteListAdapter;
import com.prasanna.android.stacknetwork.intent.UserSitesIntentService;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class StackNetworkListActivity extends ListActivity
{
    private static final String TAG = StackNetworkListActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    private Intent sitesIntent = null;

    private ArrayList<Site> sites;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            processReceiverIntent(context, intent);
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sitelist);

        Object lastSavedInstance = null;
        if (savedInstanceState != null)
        {
            lastSavedInstance = savedInstanceState.getSerializable(StringConstants.SITES);
        }

        HttpHelper.getInstance().setHost(StackUri.STACKX_API_HOST);
        if (lastSavedInstance == null && CacheUtils.hasSiteListCache(getApplicationContext()) == false)
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
                sites = CacheUtils.fetchFromSiteListCache(getApplicationContext());
                updateView(sites);
            }
        }
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
            updateView(sites);
            CacheUtils.cacheSiteList(getApplicationContext(), sites);
        }
    }

    private void updateView(ArrayList<Site> sites)
    {
        if (sites != null && sites.isEmpty() == false)
        {
            setListAdapter(new SiteListAdapter(this, R.layout.sitelist_row, sites, getListView()));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(StringConstants.SITES, sites);
        super.onSaveInstanceState(outState);
    }
}

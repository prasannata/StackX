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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.adapter.SiteListAdapter;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class StackNetworkListActivity extends ListActivity implements StackXRestQueryResultReceiver
{
    private static final String TAG = StackNetworkListActivity.class.getSimpleName();

    private ProgressDialog progressDialog;
    private Intent intent = null;
    private ArrayList<Site> sites;
    private SiteListAdapter siteListAdapter;
    private RestQueryResultReceiver receiver;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sitelist);

        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);

        Object lastSavedInstance = null;
        if (savedInstanceState != null)
            lastSavedInstance = (ArrayList<Site>) savedInstanceState.getSerializable(StringConstants.SITES);

        if (lastSavedInstance == null && SharedPreferencesUtil.hasSiteListCache(getCacheDir()) == false)
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
                sites = SharedPreferencesUtil.getSiteListFromCache(getCacheDir());
                updateView(sites);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id)
    {
        Log.d(TAG, "Clicking on list item " + position);

        Site site = sites.get(position);
        OperatingSite.setSite(site);
        startActivity(new Intent(this, QuestionsActivity.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (intent != null)
            stopService(intent);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (intent != null)
            stopService(intent);
    }

    private void registerReceiverAndStartService()
    {
        progressDialog = ProgressDialog.show(StackNetworkListActivity.this, "", getString(R.string.loadingSites));

        startIntentService();
    }

    private void startIntentService()
    {
        intent = new Intent(this, UserIntentService.class);
        intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_SITES);
        intent.putExtra(StringConstants.AUTHENTICATED, AppUtils.inAuthenticatedRealm(getApplicationContext()));
        intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);
        startService(intent);
    }

    private void updateView(ArrayList<Site> sites)
    {
        if (sites != null && sites.isEmpty() == false)
        {
            siteListAdapter = new SiteListAdapter(this, R.layout.sitelist_row, sites, getListView());
            setListAdapter(siteListAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(StringConstants.SITES, sites);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        if (progressDialog != null)
            progressDialog.dismiss();

        if (resultCode == UserIntentService.ERROR)
        {
            showError();
        }
        else
        {
            sites = (ArrayList<Site>) resultData.getSerializable(StringConstants.SITES);

            if (sites != null)
            {
                SharedPreferencesUtil.cacheSiteList(getCacheDir(), sites);
                updateView(sites);
            }
        }
    }

    private void showError()
    {
        View errorView = getLayoutInflater().inflate(R.layout.error, null);
        TextView errorTextView = (TextView) errorView.findViewById(R.id.errorMsg);
        errorTextView.setText("Failed to fetch sites");
        getListView().addFooterView(errorView);
    }
}

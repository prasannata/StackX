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

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.adapter.SiteListAdapter;
import com.prasanna.android.stacknetwork.adapter.SiteListAdapter.OnSiteSelectedListener;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.AccountSyncService;
import com.prasanna.android.stacknetwork.service.MyProfileService;
import com.prasanna.android.stacknetwork.service.TagsService;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class StackNetworkListActivity extends ListActivity implements StackXRestQueryResultReceiver,
                OnSiteSelectedListener
{
    private static final String TAG = StackNetworkListActivity.class.getSimpleName();
    private final String CHANGE_SITE_HINT = "change_site_hint";

    private ProgressDialog progressDialog;
    private Intent intent = null;
    private ArrayList<Site> sites = new ArrayList<Site>();
    private SiteListAdapter siteListAdapter;
    private RestQueryResultReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.sitelist);

        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);
        siteListAdapter = new SiteListAdapter(this, R.layout.sitelist_row, new ArrayList<Site>());
        siteListAdapter.setOnSiteSelectedListener(this);
        setListAdapter(siteListAdapter);

        if (SharedPreferencesUtil.isSet(getApplicationContext(), StringConstants.SITES_INIT, false))
        {
            progressDialog = ProgressDialog.show(StackNetworkListActivity.this, "", "Loading sites");
            startService(new Intent(getApplicationContext(), AccountSyncService.class));
        }
        else
        {
            progressDialog = ProgressDialog.show(StackNetworkListActivity.this, "",
                            "Loading sites, your accounts and permissions, this might take few seconds");
        }
        
        startIntentService();
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

    private void startIntentService()
    {
        intent = new Intent(this, UserIntentService.class);
        intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_SITES);
        intent.putExtra(StringConstants.ME, AppUtils.inAuthenticatedRealm(getApplicationContext()));
        intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);
        startService(intent);
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
            switch (resultCode)
            {
                case UserIntentService.GET_USER_SITES:
                    ArrayList<Site> result = (ArrayList<Site>) resultData.getSerializable(StringConstants.SITES);

                    if (sites != null)
                    {
                        sites.addAll(result);
                        siteListAdapter.addAll(sites);
                    }

                    break;
                default:
                    Log.d(TAG, "Unknown result code in result receiver");
                    break;
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

    @Override
    public void onSiteSelected(Site site)
    {
        OperatingSite.setSite(site);

        if (SharedPreferencesUtil.isSet(this, CHANGE_SITE_HINT, true))
        {
            Toast.makeText(this, "Use options menu to change site any time.", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.setBoolean(this, CHANGE_SITE_HINT, false);
        }

        startMyProfileService();
        startGetTagsService();
        startQuestionsActivity();
    }

    private void startMyProfileService()
    {
        if (AppUtils.inAuthenticatedRealm(this))
            startService(new Intent(this, MyProfileService.class));
    }

    private void startGetTagsService()
    {
        startService(new Intent(this, TagsService.class));
    }

    private void startQuestionsActivity()
    {
        Intent startQuestionActivityIntent = new Intent(this, QuestionsActivity.class);
        startQuestionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startQuestionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startQuestionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startQuestionActivityIntent);
    }
}

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
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.adapter.SiteListAdapter;
import com.prasanna.android.stacknetwork.adapter.SiteListAdapter.OnSiteSelectedListener;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.MyProfileService;
import com.prasanna.android.stacknetwork.service.TagsService;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
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
    private ArrayList<Site> sites;
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

        if (SharedPreferencesUtil.hasSiteListCache(getCacheDir()))
        {
            sites = SharedPreferencesUtil.getSiteListFromCache(getCacheDir());
            updateView();
        }
        else
        {
            registerReceiverAndStartService();            
        }
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
        intent.putExtra(StringConstants.ME, AppUtils.inAuthenticatedRealm(getApplicationContext()));
        intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);
        startService(intent);
    }

    private void updateView()
    {
        if (sites != null && !sites.isEmpty())
        {
            siteListAdapter = new SiteListAdapter(this, R.layout.sitelist_row, sites);
            siteListAdapter.setOnSiteSelectedListener(this);
            setListAdapter(siteListAdapter);
        }
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
                case UserIntentService.CHECK_WRITE_PERMISSION:
                    ArrayList<WritePermission> permissions = (ArrayList<WritePermission>) resultData
                                    .getSerializable(StringConstants.PERMISSION);
                    persistPermissions((Site) resultData.getSerializable(StringConstants.SITE), permissions);
                    break;
                case UserIntentService.GET_USER_SITES:
                    sites = (ArrayList<Site>) resultData.getSerializable(StringConstants.SITES);

                    if (sites != null)
                    {
                        SharedPreferencesUtil.cacheSiteList(getCacheDir(), sites);
                        updateView();
                    }
                    break;
                default:
                    Log.d(TAG, "Unknown result code in result receiver");
                    break;
            }
        }
    }

    private void startGetTagsService()
    {
        startService(new Intent(this, TagsService.class));
    }

    private void persistPermissions(Site site, ArrayList<WritePermission> permissions)
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(this);

        try
        {
            writePermissionDAO.open();
            writePermissionDAO.insertAll(site, permissions);
        }
        catch (SQLException e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            writePermissionDAO.close();
        }
    }

    private void startMyProfileService()
    {
        if (AppUtils.inAuthenticatedRealm(this))
            startService(new Intent(this, MyProfileService.class));
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

        if (SharedPreferencesUtil.isOn(this, CHANGE_SITE_HINT, true))
        {
            Toast.makeText(this, "Use options menu to change site any time.", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.setOnOff(this, CHANGE_SITE_HINT, false);
        }

        startMyProfileService();
        startGetTagsService();
        startQuestionsActivity();
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

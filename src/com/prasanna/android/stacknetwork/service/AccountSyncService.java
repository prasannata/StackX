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

package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prasanna.android.stacknetwork.StackNetworkListActivity;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DbRequestThreadExecutor;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class AccountSyncService extends AbstractStackxService
{
    private static final String TAG = AccountSyncService.class.getSimpleName();

    private final static class ServiceHandler extends Handler
    {
        private OnHandlerComplete onHandlerComplete;
        private Context context;

        public ServiceHandler(Looper looper, Context context, OnHandlerComplete onHandlerComplete)
        {
            super(looper);
            this.context = context;
            this.onHandlerComplete = onHandlerComplete;
        }

        @Override
        public void handleMessage(Message msg)
        {
            boolean newThingsFound = false;
            HashMap<String, Site> sites = SiteDAO.getAll(context);
            long sitesLastUpdated = SiteDAO.getLastUpdateTime(context);
            if (AppUtils.aDaySince(sitesLastUpdated))
                refreshSiteList(sites);

            long accountsLastUpdated = SharedPreferencesUtil
                            .getLong(context, StringConstants.ACCOUNTS_LAST_UPDATED, 0L);

            if (AppUtils.aHalfAnHourSince(accountsLastUpdated))
            {
                Log.d(TAG, "Syncing user accounts");

                long accountId = SharedPreferencesUtil.getLong(context, StringConstants.ACCOUNT_ID, 0L);
                ArrayList<Account> existingAccounts = UserAccountsDAO.get(context, accountId);
                HashMap<String, Account> retrievedAccounts = UserServiceHelper.getInstance().getAccounts(1);
                if (retrievedAccounts != null)
                {
                    if (existingAccounts == null)
                    {
                        Log.d(TAG, "User with no accounts has accounts");
                        DbRequestThreadExecutor.persistAccounts(context,
                                        new ArrayList<Account>(retrievedAccounts.values()));
                    }
                    else
                    {
                        int existingAccountsSize = existingAccounts.size();

                        ArrayList<Account> newAccounts = null;
                        Iterator<Account> existingAccountIter = existingAccounts.iterator();
                        while (existingAccountIter.hasNext())
                        {
                            Account existingAccount = existingAccountIter.next();

                            if (!retrievedAccounts.containsKey(existingAccount.siteUrl))
                            {
                                Log.d(TAG, "deleted account: " + existingAccount.siteName);
                                existingAccountIter.remove();
                            }
                            else
                            {
                                Log.d(TAG, "Existing account: " + existingAccount.siteName);
                                retrievedAccounts.remove(existingAccount.siteUrl);
                            }
                        }

                        if (!retrievedAccounts.isEmpty())
                        {
                            Log.d(TAG, "Adding new accounts to DB");

                            for (Map.Entry<String, Account> entry : retrievedAccounts.entrySet())
                            {
                                Log.d(TAG, "New account: " + entry.getValue().siteName);
                                if (newAccounts == null)
                                    newAccounts = new ArrayList<Account>();

                                Site site = sites.get(entry.getValue().siteUrl);
                                if (site != null)
                                {
                                    site.writePermissions = UserServiceHelper.getInstance().getWritePermissions(
                                                    site.apiSiteParameter);
                                    DbRequestThreadExecutor.persistPermissions(context, site, site.writePermissions);
                                }
                                newAccounts.add(entry.getValue());
                            }

                            DbRequestThreadExecutor.persistAccounts(context, newAccounts);
                            SiteDAO.updateSites(context, newAccounts);
                            newThingsFound = true;
                        }

                        if (existingAccountsSize != existingAccounts.size())
                        {
                            Log.d(TAG, "Removing accounts from DB");

                            UserAccountsDAO.delete(context, existingAccounts);
                            WritePermissionDAO.delete(context, existingAccounts);
                            SiteDAO.updateSites(context, newAccounts);
                            newThingsFound = true;
                        }
                    }

                    SharedPreferencesUtil.setLong(context, StringConstants.ACCOUNTS_LAST_UPDATED,
                                    System.currentTimeMillis());
                }
            }

            onHandlerComplete.onHandleMessageFinish(msg, newThingsFound);
        }

        private void refreshSiteList(HashMap<String, Site> sites)
        {
            LinkedHashMap<String, Site> retrievedSites = UserServiceHelper.getInstance().getAllSitesInNetwork();
            for (String key : retrievedSites.keySet())
            {
                if (!sites.containsKey(key))
                    SiteDAO.insert(context, retrievedSites.get(key));
            }
        }
    }

    @Override
    protected Handler getServiceHandler(Looper looper)
    {
        return new ServiceHandler(looper, getApplicationContext(), new OnHandlerComplete()
        {
            @Override
            public void onHandleMessageFinish(Message message, Object... args)
            {
                setRunning(false);
                Boolean newThingsFound = (Boolean) args[0];
                if (newThingsFound)
                    AccountSyncService.this.sendBroadcast(new Intent(
                                    StackNetworkListActivity.ACCOUNT_UPDATE_INTENT_FILTER));
                AccountSyncService.this.stopSelf(message.arg1);
            }
        });
    }

}

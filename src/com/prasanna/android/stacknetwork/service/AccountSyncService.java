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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prasanna.android.stacknetwork.StackNetworkListActivity;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.WritePermission;
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
            if (sites != null)
            {
                long sitesLastUpdated = SiteDAO.getLastUpdateTime(context);
                if (AppUtils.aDaySince(sitesLastUpdated))
                    refreshSiteList(sites);

                long accountsLastUpdated = SharedPreferencesUtil.getLong(context,
                                StringConstants.ACCOUNTS_LAST_UPDATED, 0L);

                if (AppUtils.aHalfAnHourSince(accountsLastUpdated))
                    newThingsFound = syncAccounts(sites);
            }
            onHandlerComplete.onHandleMessageFinish(msg, newThingsFound);
        }

        private boolean syncAccounts(HashMap<String, Site> sites)
        {
            boolean accountsAdded = false;
            boolean accountsModified = false;
            boolean accountsDeleted = false;

            Log.d(TAG, "Syncing user accounts");

            long accountId = SharedPreferencesUtil.getLong(context, StringConstants.ACCOUNT_ID, 0L);
            HashMap<String, Account> retrievedAccounts = UserServiceHelper.getInstance().getAccounts(1);
            ArrayList<Account> existingAccounts = UserAccountsDAO.get(context, accountId);
            if (existingAccounts == null)
            {
                Log.d(TAG, "User with no accounts has accounts");
                addNewAccounts(sites, retrievedAccounts);
                accountsAdded = true;
            }
            else
            {
                if (retrievedAccounts != null)
                {
                    accountsDeleted = checkAndUpdateForDeletedAccounts(new HashMap<String, Account>(retrievedAccounts),
                                    new ArrayList<Account>(existingAccounts));
                    accountsAdded = checkAndUpdateNewAccounts(sites, new HashMap<String, Account>(retrievedAccounts),
                                    new ArrayList<Account>(existingAccounts));
                    accountsModified = checkForWriterPermissionsForExistingAccounts(sites,
                                    new HashMap<String, Account>(retrievedAccounts), new ArrayList<Account>(
                                                    existingAccounts));
                }
            }

            SharedPreferencesUtil.setLong(context, StringConstants.ACCOUNTS_LAST_UPDATED, System.currentTimeMillis());
            return accountsAdded || accountsModified || accountsDeleted;
        }

        private boolean checkForWriterPermissionsForExistingAccounts(HashMap<String, Site> sites,
                        HashMap<String, Account> retrievedAccounts, ArrayList<Account> existingAccounts)
        {
            Iterator<Account> iterator = existingAccounts.iterator();

            while (iterator.hasNext())
            {
                if (!retrievedAccounts.containsKey(iterator.next().siteUrl))
                    iterator.remove();
            }

            if (!existingAccounts.isEmpty())
            {
                for (Account account : existingAccounts)
                {
                    Log.d(TAG, "Checking for change in write permission for " + account.siteUrl);
                    Site site = sites.get(account.siteUrl);
                    if (site != null && site.apiSiteParameter != null)
                        getAndPersistWritePermissions(site);
                }
                return true;
            }
            return false;
        }

        private boolean checkAndUpdateForDeletedAccounts(HashMap<String, Account> retrievedAccounts,
                        ArrayList<Account> existingAccounts)
        {
            int existingAccountsSize = existingAccounts.size();
            Iterator<Account> existingAccountIter = existingAccounts.iterator();

            while (existingAccountIter.hasNext())
            {
                Account existingAccount = existingAccountIter.next();

                if (retrievedAccounts.containsKey(existingAccount.siteUrl))
                {
                    Log.d(TAG, "deleted account: " + existingAccount.siteName);
                    existingAccountIter.remove();
                }
            }

            if (existingAccounts.size() > 0 && existingAccountsSize != existingAccounts.size())
            {
                Log.d(TAG, "Removing accounts from DB");
                removeAccounts(existingAccounts);
                return true;
            }

            return false;
        }

        private void removeAccounts(ArrayList<Account> existingAccounts)
        {
            UserAccountsDAO.delete(context, existingAccounts);
            WritePermissionDAO.delete(context, existingAccounts);
            SiteDAO.updateSites(context, existingAccounts, false);
        }

        private boolean checkAndUpdateNewAccounts(HashMap<String, Site> sites,
                        HashMap<String, Account> retrievedAccounts, ArrayList<Account> existingAccounts)
        {
            ArrayList<Account> newAccounts = null;
            for (Account existingAccount : existingAccounts)
            {
                if (retrievedAccounts.containsKey(existingAccount.siteUrl))
                    retrievedAccounts.remove(existingAccount.siteUrl);
            }

            if (!retrievedAccounts.isEmpty())
            {
                for (String key : retrievedAccounts.keySet())
                {
                    Log.d(TAG, "New account : " + key);
                    Site site = sites.get(key);
                    getAndPersistWritePermissions(site);
                    if (newAccounts == null)
                        newAccounts = new ArrayList<Account>();

                    newAccounts.add(retrievedAccounts.get(key));
                }

                if (newAccounts != null)
                {
                    UserAccountsDAO.insertAll(context, newAccounts);
                    DbRequestThreadExecutor.persistAccounts(context, newAccounts);
                    SiteDAO.updateSites(context, newAccounts, true);
                    return true;
                }
            }

            return false;
        }

        private void addNewAccounts(HashMap<String, Site> sites, HashMap<String, Account> retrievedAccounts)
        {
            UserAccountsDAO.insertAll(context, new ArrayList<Account>(retrievedAccounts.values()));

            for (String siteUrl : retrievedAccounts.keySet())
            {
                Site site = sites.get(siteUrl);
                getAndPersistWritePermissions(site);
            }
        }

        private void getAndPersistWritePermissions(Site site)
        {
            ArrayList<WritePermission> writePermissions = UserServiceHelper.getInstance().getWritePermissions(
                            site.apiSiteParameter);
            if (writePermissions != null)
                DbRequestThreadExecutor.persistPermissionsInCurrentThread(context, site, writePermissions);
        }

        private void refreshSiteList(HashMap<String, Site> sites)
        {
            LinkedHashMap<String, Site> retrievedSites = UserServiceHelper.getInstance().getAllSitesInNetwork();
            if (retrievedSites != null)
            {
                for (String key : retrievedSites.keySet())
                {
                    if (!sites.containsKey(key))
                        SiteDAO.insert(context, retrievedSites.get(key));
                }
                
                SiteDAO.updateLastUpdateTime(context);
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

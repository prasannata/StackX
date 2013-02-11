package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.database.SQLException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
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
            HashMap<String, Site> sites = getSites();
            long accountsLastUpdated = SharedPreferencesUtil
                            .getLong(context, StringConstants.ACCOUNTS_LAST_UPDATED, 0L);

            if (AppUtils.aHalfAnHourSince(accountsLastUpdated))
            {
                Log.d(TAG, "Syncing user accounts");

                ArrayList<Account> existingAccounts = getExistingAccounts();
                HashMap<String, Account> retrievedAccounts = UserServiceHelper.getInstance().getAccounts(1);
                if (retrievedAccounts != null)
                {
                    if (existingAccounts == null)
                    {
                        Log.d(TAG, "User with no accounts has accounts");
                        AppUtils.persistAccounts(context, new ArrayList<Account>(retrievedAccounts.values()));
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
                                    AppUtils.persistPermissions(context, site, site.writePermissions);
                                }
                                newAccounts.add(entry.getValue());
                            }

                            AppUtils.persistAccounts(context, newAccounts);
                            updateSites(newAccounts);
                        }

                        if (existingAccountsSize != existingAccounts.size())
                        {
                            Log.d(TAG, "Removing accounts from DB");

                            removeAccounts(existingAccounts);
                            removeWritePermissions(existingAccounts);
                            updateSites(newAccounts);
                        }
                    }

                    SharedPreferencesUtil.setLong(context, StringConstants.ACCOUNTS_LAST_UPDATED,
                                    System.currentTimeMillis());
                }
            }

            onHandlerComplete.onHandleMessageFinish(msg);
        }

        private void updateSites(ArrayList<Account> newAccounts)
        {
            SiteDAO siteDAO = new SiteDAO(context);
            try
            {
                siteDAO.open();
                siteDAO.updateRegistrationInfo(newAccounts);
            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                siteDAO.close();
            }
        }

        private void removeWritePermissions(ArrayList<Account> deletedAccounts)
        {
            WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);
            try
            {
                writePermissionDAO.open();
                writePermissionDAO.deleteList(deletedAccounts);
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

        private void removeAccounts(final ArrayList<Account> deletedAccounts)
        {
            UserAccountsDAO userAccountsDao = new UserAccountsDAO(context);

            try
            {
                userAccountsDao.open();
                userAccountsDao.deleteList(deletedAccounts);
            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                userAccountsDao.close();
            }
        }

        private ArrayList<Account> getExistingAccounts()
        {
            UserAccountsDAO accountsDAO = new UserAccountsDAO(context);

            try
            {
                accountsDAO.open();
                return accountsDAO.getAccounts(SharedPreferencesUtil.getLong(context, StringConstants.ACCOUNT_ID, 0L));
            }
            catch (SQLException e)
            {
                Log.e(TAG, e.getMessage());
            }
            finally
            {
                accountsDAO.close();
            }

            return null;
        }

        private HashMap<String, Site> getSites()
        {
            SiteDAO siteDAO = new SiteDAO(context);

            try
            {
                siteDAO.open();
                return siteDAO.getLinkSitesMap();
            }
            catch (SQLException e)
            {
                Log.e(TAG, e.getMessage());
            }
            finally
            {
                siteDAO.close();
            }

            return null;
        }
    }

    @Override
    protected Handler getServiceHandler(Looper looper)
    {
        return new ServiceHandler(looper, getApplicationContext(), new OnHandlerComplete()
        {
            @Override
            public void onHandleMessageFinish(Message message)
            {
                setRunning(false);
                AccountSyncService.this.stopSelf(message.arg1);
            }
        });
    }

}

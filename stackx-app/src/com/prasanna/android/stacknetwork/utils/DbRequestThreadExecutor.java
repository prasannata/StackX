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

package com.prasanna.android.stacknetwork.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;
import android.database.SQLException;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
import com.prasanna.android.utils.LogWrapper;

public class DbRequestThreadExecutor {
    private static final String TAG = DbRequestThreadExecutor.class.getSimpleName();

    public static void persistSites(final Context context, final ArrayList<Site> sites) {
        AppUtils.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                SiteDAO.insertAll(context, sites);
            }
        });
    }

    public static void persistAccounts(final Context context, final ArrayList<Account> accounts) {
        AppUtils.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                UserAccountsDAO.insertAll(context, accounts);
            }
        });
    }

    public static void persistPermissions(final Context context, final Site site,
            final ArrayList<WritePermission> permissions) {
        AppUtils.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                persistPermissionsInCurrentThread(context, site, permissions);
            }
        });
    }

    public static void persistPermissionsInCurrentThread(final Context context, final Site site,
            final ArrayList<WritePermission> permissions) {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);

        try {
            writePermissionDAO.open();
            writePermissionDAO.insert(site, permissions);

            for (WritePermission permission : permissions) {
                if (permission.objectType != null)
                    switchOnObjectType(context, permission);
            }
        }
        catch (SQLException e) {
            LogWrapper.e(TAG, e.getMessage());
        }
        finally {
            writePermissionDAO.close();
        }
    }

    private static void switchOnObjectType(final Context context, final WritePermission permission) {
        switch (permission.objectType) {
            case ANSWER:
                SharedPreferencesUtil.setLong(context, WritePermission.PREF_SECS_BETWEEN_ANSWER_WRITE,
                        permission.minSecondsBetweenActions);
                break;
            case COMMENT:
                SharedPreferencesUtil.setLong(context, WritePermission.PREF_SECS_BETWEEN_COMMENT_WRITE,
                        permission.minSecondsBetweenActions);
                break;
            case QUESTION:
                SharedPreferencesUtil.setLong(context, WritePermission.PREF_SECS_BETWEEN_QUESTION_WRITE,
                        permission.minSecondsBetweenActions);
                break;
            default:
                break;
        }
    }

    public static HashMap<String, SearchCriteria> getSearchesMarkedForTab(final Context context, final String site) {
        HashMap<String, SearchCriteria> searchCriterias = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<String, SearchCriteria>> callable = new Callable<HashMap<String, SearchCriteria>>() {
            @Override
            public HashMap<String, SearchCriteria> call() throws Exception {
                ArrayList<SearchCriteriaDomain> criteriaForCustomTabs =
                        SearchCriteriaDAO.getCriteriaForCustomTabs(context, site);
                if (criteriaForCustomTabs != null) {
                    HashMap<String, SearchCriteria> searchCriterias = new HashMap<String, SearchCriteria>();
                    for (SearchCriteriaDomain searchCriteriaDomain : criteriaForCustomTabs)
                        searchCriterias.put(searchCriteriaDomain.name, searchCriteriaDomain.searchCriteria);
                    return searchCriterias;
                }

                return null;
            }
        };

        try {
            Future<HashMap<String, SearchCriteria>> future = executor.submit(callable);
            searchCriterias = future.get();
            executor.shutdown();
        }
        catch (InterruptedException e) {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (ExecutionException e) {
            LogWrapper.e(TAG, e.getMessage());
        }

        return searchCriterias;
    }
}

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

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;

public class DbRequestThreadExecutor
{
    private static final String TAG = DbRequestThreadExecutor.class.getSimpleName();
    
    public static void persistSites(final Context context, final ArrayList<Site> sites)
    {
        AppUtils.runOnBackgroundThread(new Runnable()
        {
            @Override
            public void run()
            {
                SiteDAO.insertAll(context, sites);
            }
        });
    }

    public static void persistAccounts(final Context context, final ArrayList<Account> accounts)
    {
        AppUtils.runOnBackgroundThread(new Runnable()
        {
            @Override
            public void run()
            {
                UserAccountsDAO.insertAll(context, accounts);
            }
        });
    }

    public static void persistPermissions(final Context context, final Site site,
                    final ArrayList<WritePermission> permissions)
    {
        AppUtils.runOnBackgroundThread(new Runnable()
        {
            @Override
            public void run()
            {
                persist(site, permissions);
            }

            private void persist(final Site site, final ArrayList<WritePermission> permissions)
            {
                WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);

                try
                {
                    writePermissionDAO.open();
                    writePermissionDAO.insert(site, permissions);

                    for (WritePermission permission : permissions)
                    {
                        if (permission.objectType != null)
                            switchOnObjectType(permission);
                    }
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

            private void switchOnObjectType(WritePermission permission)
            {
                switch (permission.objectType)
                {
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
        });
    }
}

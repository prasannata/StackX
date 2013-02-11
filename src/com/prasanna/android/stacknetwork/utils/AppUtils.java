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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;

public class AppUtils
{
    private static final String TAG = AppUtils.class.getSimpleName();

    public static String formatReputation(int reputation)
    {
        if (reputation > 0)
            return formatNumber(reputation);

        return "";
    }

    public static String formatNumber(int number)
    {
        String reputationString = "";

        if (number > 10000)
        {
            float reputationInThousands = ((float) number) / 1000f;
            reputationString += String.format("%.1fk", reputationInThousands);
        }
        else
        {
            reputationString += number;
        }

        return reputationString;
    }

    public static boolean inAuthenticatedRealm(Context context)
    {
        return SharedPreferencesUtil.getAccessToken(context) == null ? false : true;
    }

    public static boolean inRegisteredSite(Context context)
    {
        return inAuthenticatedRealm(context)
                        && SharedPreferencesUtil.getRegisteredSitesForUser(context.getCacheDir()).contains(
                                        OperatingSite.getSite().apiSiteParameter);
    }

    public static Map<String, String> getDefaultQueryParams()
    {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);

        String accessToken = SharedPreferencesUtil.getAccessToken(null);
        if (accessToken != null)
        {
            queryParams.put(StackUri.QueryParams.ACCESS_TOKEN, accessToken);
            queryParams.put(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY);
        }

        return queryParams;
    }

    public static SoftReference<Bitmap> getBitmap(Resources resources, int drawable)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, drawable);
        return new SoftReference<Bitmap>(bitmap);
    }

    public static boolean allowedToWrite(Context context)
    {
        if (context == null)
            return false;

        long lastCommentWrite = SharedPreferencesUtil.getLong(context, WritePermission.PREF_LAST_COMMENT_WRITE, 0);
        long minSecondsBetweenWrite = SharedPreferencesUtil.getLong(context,
                        WritePermission.PREF_SECS_BETWEEN_COMMENT_WRITE, 0);
        return ((System.currentTimeMillis() - lastCommentWrite) / 1000 > minSecondsBetweenWrite);
    }

    public static ViewGroup getErrorView(Context context, HttpException e)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout errorLayout = (RelativeLayout) inflater.inflate(R.layout.error, null);
        String errorMsg = getStackXErrorMsg(e);

        TextView textView = (TextView) errorLayout.findViewById(R.id.errorMsg);
        textView.setText(errorMsg);
        return errorLayout;
    }

    public static String getStackXErrorMsg(HttpException e)
    {
        String errorMsg = "Unknown error";

        if (e != null)
        {
            if (e.getCode() == null)
            {
                StackXError error = StackXError.deserialize(e.getErrorResponse());
                if (error != null)
                    errorMsg = error.name;
                else
                    errorMsg = e.getStatusCode() + " " + e.getStatusDescription();
            }
            else
                errorMsg = e.getCode().getDescription();
        }
        return errorMsg;
    }

    public static void showSoftInput(Context context, View v)
    {
        toggleSoftInput(context, v, false);
    }

    public static void hideSoftInput(Context context, View v)
    {
        toggleSoftInput(context, v, true);
    }

    private static void toggleSoftInput(Context context, View v, boolean hide)
    {
        if (context != null && v != null)
        {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (hide)
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            else
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static Thread runOnBackgroundThread(final Runnable runnable)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };
        t.start();
        return t;
    }

    public static boolean anHourSince(long ms)
    {
        return System.currentTimeMillis() - ms > IntegerConstants.MS_IN_AN_HOUR;
    }

    public static boolean aDaySince(long ms)
    {
        return System.currentTimeMillis() - ms > IntegerConstants.MS_IN_A_DAY;
    }

    public static boolean aHalfAnHourSince(long ms)
    {
        return System.currentTimeMillis() - ms > IntegerConstants.MS_IN_HALF_AN_HOUR;
    }

    public static void persistAccounts(final Context context, final ArrayList<Account> newAccounts)
    {
        AppUtils.runOnBackgroundThread(new Runnable()
        {
            @Override
            public void run()
            {

                UserAccountsDAO userAccountsDao = new UserAccountsDAO(context);
                try
                {
                    userAccountsDao.open();
                    userAccountsDao.insert(newAccounts);
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

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

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.SearchCriteriaListActivity;
import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;

public class AppUtils
{
    private static String userAccessToken;
    public static final boolean DEBUG = true;

    public static void setAccessToken(Context context, String accessToken)
    {
        if (userAccessToken == null && accessToken != null)
        {
            SharedPreferencesUtil.setString(context, StringConstants.ACCESS_TOKEN, accessToken);
            userAccessToken = accessToken;
        }
    }

    public static String getAccessToken(Context context)
    {
        if (userAccessToken == null)
            userAccessToken = SharedPreferencesUtil.getString(context, StringConstants.ACCESS_TOKEN, null);

        return userAccessToken;
    }

    public static void loadAccessToken(Context context)
    {
        getAccessToken(context);
    }

    public static void clearSharedPreferences(Context context)
    {
        SharedPreferencesUtil.clearSharedPreferences(context);
        userAccessToken = null;
    }

    public static boolean isFirstRun(Context context)
    {
        return SharedPreferencesUtil.isSet(context, StringConstants.IS_FIRST_RUN, true);
    }

    public static void setFirstRunComplete(Context context)
    {
        SharedPreferencesUtil.setBoolean(context, StringConstants.IS_FIRST_RUN, false);
    }

    public static void setDefaultSite(Context context, Site site)
    {
        if (site != null && context != null)
        {
            File dir = new File(context.getCacheDir(), StringConstants.DEFAULTS);
            SharedPreferencesUtil.writeObject(site, dir, StringConstants.SITE);
        }
    }

    public static Site getDefaultSite(Context context)
    {
        if (context != null)
        {
            File dir = new File(context.getCacheDir(), StringConstants.DEFAULTS);
            if (dir.exists() && dir.isDirectory())
            {
                File file = new File(dir, StringConstants.SITE);
                if (file.exists() && file.isFile())
                    return (Site) SharedPreferencesUtil.readObject(file);
            }
        }

        return null;
    }

    public static void clearDefaultSite(Context context)
    {
        if (context != null)
        {
            File dir = new File(context.getCacheDir(), StringConstants.DEFAULTS);
            if (dir.exists() && dir.isDirectory())
            {
                File file = new File(dir, StringConstants.SITE);
                if (file.exists() && file.isFile())
                    SharedPreferencesUtil.deleteFile(file);
            }
        }
    }

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
            reputationString += String.format("%.1fk", ((float) number) / 1000f);
        else
            reputationString += number;

        return reputationString;
    }

    public static boolean inAuthenticatedRealm(Context context)
    {
        return getAccessToken(context) == null ? false : true;
    }

    public static boolean inRegisteredSite(Context context)
    {
        return inAuthenticatedRealm(context)
                        && SiteDAO.isRegisteredForSite(context, OperatingSite.getSite().apiSiteParameter);
    }

    public static Map<String, String> getDefaultQueryParams()
    {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);

        String accessToken = getAccessToken(null);
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

    public static TextView getEmptyItemsView(Context context)
    {
        return (TextView) LayoutInflater.from(context).inflate(R.layout.empty_items, null);
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

    public static void incrementNumSavedSearches(Context context)
    {
        long num = getNumSavedSearches(context);

        SharedPreferencesUtil.setLong(context, SettingsFragment.KEY_PREF_NUM_SAVED_SEARCHES, ++num);
    }

    public static void decrementNumSavedSearches(Context context)
    {
        long num = getNumSavedSearches(context);

        if (num > 0)
            SharedPreferencesUtil.setLong(context, SettingsFragment.KEY_PREF_NUM_SAVED_SEARCHES, --num);
    }

    public static void decrementNumSavedSearches(Context context, int by)
    {
        long num = getNumSavedSearches(context);

        if (num > 0 && num - by >= 0)
            SharedPreferencesUtil.setLong(context, SettingsFragment.KEY_PREF_NUM_SAVED_SEARCHES, num - by);
    }

    public static long getNumSavedSearches(Context context)
    {
        return SharedPreferencesUtil.getLong(context, SettingsFragment.KEY_PREF_NUM_SAVED_SEARCHES, 0);
    }

    public static boolean savedSearchesMaxed(Context context)
    {
        return getNumSavedSearches(context) == SearchCriteriaListActivity.MAX_SAVED_SEARCHES;
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

    public static boolean isNetworkAvailable(Context context)
    {
        if (context == null)
            return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}

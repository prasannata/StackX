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

package com.prasanna.android.stacknetwork.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.StackXError;

public class AppUtils
{
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
                        && SharedPreferencesUtil.getRegisteredSitesForUser(context.getCacheDir()).containsKey(
                                        OperatingSite.getSite().name);
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

    public static ViewGroup getErrorView(Context context, HttpException e)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout errorLayout = (RelativeLayout) inflater.inflate(R.layout.error, null);
        StackXError error = StackXError.deserialize(e.getErrorResponse());
        TextView textView = (TextView) errorLayout.findViewById(R.id.errorMsg);
        textView.setText(error.name);
        return errorLayout;
    }
}

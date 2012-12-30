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

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AppUtils
{
    public static String formatReputation(int reputation)
    {
	String reputationString = "";

	if (reputation > 10000)
	{
	    float reputationInThousands = ((float) reputation) / 1000f;
	    reputationString += String.format("%.1fk", reputationInThousands);
	}
	else
	{
	    reputationString += reputation;
	}
	return reputationString;
    }

    public static String formatNumber(int reputation)
    {
	String reputationString = "";

	if (reputation > 10000)
	{
	    float reputationInThousands = ((float) reputation) / 1000f;
	    reputationString += String.format("%.1fk", reputationInThousands);
	}
	else
	{
	    reputationString += reputation;
	}

	return reputationString;
    }

    public static boolean inAuthenticatedRealm()
    {
	return SharedPreferencesUtil.getAccessToken(null) == null ? false : true;
    }

    public static boolean inRegisteredSite(File cacheDir)
    {
	return inAuthenticatedRealm()
	                && SharedPreferencesUtil.getRegisteredSitesForUser(cacheDir).containsKey(OperatingSite.getSite().name);
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
}

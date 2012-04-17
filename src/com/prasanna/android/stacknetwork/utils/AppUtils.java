package com.prasanna.android.stacknetwork.utils;

import java.util.HashMap;
import java.util.Map;

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
        return CacheUtils.getAccessToken(null) == null ? false : true;
    }

    public static Map<String, String> getDefaultQueryParams()
    {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);

        String accessToken = CacheUtils.getAccessToken(null);
        if (accessToken != null)
        {
            queryParams.put(StackUri.QueryParams.ACCESS_TOKEN, CacheUtils.getAccessToken(null));
            queryParams.put(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY);
        }

        return queryParams;
    }
}

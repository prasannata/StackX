package com.prasanna.android.stacknetwork.utils;

import java.util.HashMap;
import java.util.Map;

public class AppUtils
{
    public static String formatUserReputation(int reputation)
    {
	String reputationString = "";

	if (reputation > 10000)
	{
	    float reputationInThousands = ((float) reputation) / 1000f;
	    reputationString += " " + String.format("(%.1fk)", reputationInThousands);
	}
	else
	{
	    reputationString += " (" + reputation + ")";
	}
	return reputationString;
    }

    public static Map<String, String> getAuthenticatedUserQueryParams(String accessToken)
    {
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUri.QueryParams.ACCESS_TOKEN, accessToken);
	queryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);
	queryParams.put(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY);

	return queryParams;
    }
}

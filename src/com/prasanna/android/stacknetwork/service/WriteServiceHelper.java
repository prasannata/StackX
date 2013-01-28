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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.util.Log;

import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackUri;

public class WriteServiceHelper extends AbstractBaseServiceHelper
{
    private static final String TAG = WriteServiceHelper.class.getSimpleName();

    private static final WriteServiceHelper INSTANCE = new WriteServiceHelper();

    private WriteServiceHelper()
    {
    }

    public static WriteServiceHelper getInstance()
    {
        return INSTANCE;
    }

    public Comment addComment(long postId, String body)
    {
        String restEndPoint = "/posts/" + postId + "/comments/add";

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.ACCESS_TOKEN, SharedPreferencesUtil.getAccessToken(null));
        queryParams.put(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.DEFAULT_FILTER);
        queryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);
        queryParams.put(StackUri.QueryParams.BODY, body);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);

        JSONObjectWrapper jsonObject = executeHttpPostequest(restEndPoint, queryParams, null);
        
        try
        {
            return getSerializedCommentObject(jsonObject);
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }
}

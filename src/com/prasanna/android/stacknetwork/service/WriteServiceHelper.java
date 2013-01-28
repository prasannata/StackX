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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.util.Log;

import com.prasanna.android.http.ClientException;
import com.prasanna.android.http.HttpContentTypes;
import com.prasanna.android.http.HttpHeaderParams;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.QueryParamDefaultValues;

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

        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();

        parameters.add(new BasicNameValuePair(StackUri.QueryParams.ACCESS_TOKEN, SharedPreferencesUtil
                        .getAccessToken(null)));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.FILTER, QueryParamDefaultValues.ITEM_DETAIL_FILTER));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.CLIENT_ID, QueryParamDefaultValues.CLIENT_ID));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.BODY, body));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter));

        Map<String, String> requestHeaders = new HashMap<String, String>();

        requestHeaders.put(HttpHeaderParams.CONTENT_TYPE, HttpContentTypes.APPLICATION_FROM_URL_ENCODED);
        requestHeaders.put(HttpHeaderParams.ACCEPT, HttpContentTypes.APPLICATION_JSON);

        try
        {
            JSONObjectWrapper jsonObject = executeHttpPostequest(restEndPoint, requestHeaders, null,
                            new UrlEncodedFormEntity(parameters));
            return getSerializedCommentObject(jsonObject);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ClientException(ClientException.ClientErrorCode.INVALID_ENCODING);
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

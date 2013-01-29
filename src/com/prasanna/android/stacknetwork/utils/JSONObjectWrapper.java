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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONObjectWrapper
{
    private static final String TAG = JSONObjectWrapper.class.getSimpleName();
    public static final int ERROR = -1;
    private final JSONObject jsonObject;

    public static JSONObjectWrapper wrap(JSONObject jsonObject)
    {
        JSONObjectWrapper wrap = null;

        if (jsonObject != null)
        {
            wrap = new JSONObjectWrapper(jsonObject);
        }

        return wrap;
    }

    public JSONObjectWrapper(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    public JSONObjectWrapper getJSONObject(String name)
    {
        JSONObjectWrapper value = null;
        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = new JSONObjectWrapper(jsonObject.getJSONObject(name));
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return value;
    }

    public JSONArray getJSONArray(String name)
    {
        JSONArray value = null;
        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = jsonObject.getJSONArray(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return value;
    }

    public long getLong(String name)
    {
        long value = ERROR;

        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = jsonObject.getLong(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return value;
    }

    public int getInt(String name, int defaultValue)
    {
        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                return jsonObject.getInt(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return defaultValue;
    }

    public int getInt(String name)
    {
        return getInt(name, ERROR);
    }

    public double getDouble(String name)
    {
        double value = ERROR;

        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = jsonObject.getDouble(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return value;
    }

    public boolean getBoolean(String name)
    {
        boolean value = false;

        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = jsonObject.getBoolean(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return value;
    }

    public String getString(String name)
    {
        String value = null;
        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = jsonObject.getString(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }
        return value;
    }

    public boolean isErrorResponse()
    {
        return has(StringConstants.ERROR);
    }

    public boolean has(String name)
    {
        if (jsonObject != null)
        {
            return jsonObject.has(name);
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        if (jsonObject != null)
        {
            return jsonObject.toString();
        }

        return null;
    }
}

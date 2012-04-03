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

    public int getInt(String name)
    {
        int value = ERROR;

        if (jsonObject != null && jsonObject.has(name))
        {
            try
            {
                value = jsonObject.getInt(name);
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }

        return value;
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
}

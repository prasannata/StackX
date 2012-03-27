package com.prasanna.android.stacknetwork.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONObjectWrapper extends JSONObject
{
    private static final String TAG = JSONObjectWrapper.class.getSimpleName();
    public static final int ERROR = -1;

    public JSONObjectWrapper(String jsonText) throws JSONException
    {
	super(jsonText);
    }

    @Override
    public Object get(String name)
    {
	Object returnValue = null;
	try
	{
	    returnValue = super.get(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return returnValue;
    }

    @Override
    public boolean getBoolean(String name)
    {
	boolean returnValue = false;
	try
	{
	    returnValue = super.getBoolean(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return returnValue;
    }

    @Override
    public double getDouble(String name)
    {
	double returnValue = ERROR;
	try
	{
	    returnValue = super.getDouble(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return returnValue;
    }

    @Override
    public int getInt(String name)
    {
	int returnValue = ERROR;
	try
	{
	    returnValue = super.getInt(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return returnValue;
    }

    @Override
    public JSONArray getJSONArray(String name)
    {
	JSONArray jsonArray = null;
	try
	{
	    jsonArray = super.getJSONArray(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return jsonArray;
    }

    @Override
    public JSONObject getJSONObject(String name)
    {
	JSONObject jsonObject = null;
	try
	{
	    jsonObject = super.getJSONObject(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return jsonObject;
    }

    @Override
    public long getLong(String name)
    {
	long returnValue = ERROR;
	try
	{
	    returnValue = super.getLong(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return returnValue;
    }

    @Override
    public String getString(String name)
    {
	String returnValue = null;
	try
	{
	    returnValue = super.getString(name);
	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return returnValue;
    }

}

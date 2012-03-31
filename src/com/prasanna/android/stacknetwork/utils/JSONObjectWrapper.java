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

    public JSONObjectWrapper(JSONObject jsonObject) throws JSONException
    {
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
    public JSONObjectWrapper getJSONObject(String name)
    {
	JSONObjectWrapper jsonObject = null;
	try
	{
	    JSONObject object = super.getJSONObject(name);

	    if (object != null)
	    {
		jsonObject = new JSONObjectWrapper(object.toString());
	    }

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

    public JSONObjectWrapper getObjectFromArray(String arrayName, int index)
    {
	JSONObjectWrapper jsonObject = null;
	try
	{
	    JSONArray jsonArray = super.getJSONArray(arrayName);
	    if (jsonArray != null && jsonArray.length() > index)
	    {
		jsonObject = new JSONObjectWrapper(jsonArray.getJSONObject(index).toString());
	    }

	}
	catch (JSONException e)
	{
	    Log.d(TAG, e.getMessage());
	}
	return jsonObject;
    }
}

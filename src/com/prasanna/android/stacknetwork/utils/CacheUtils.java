package com.prasanna.android.stacknetwork.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Site;

public class CacheUtils
{
    private static final String TAG = CacheUtils.class.getSimpleName();

    private static String accessToken;

    public static class CacheFileName
    {
	public static final String SITE_CACHE_FILE_NAME = "sites";
    }

    public static boolean hasSiteListCache(Context context)
    {
	boolean present = false;
	File file = new File(context.getCacheDir(), CacheFileName.SITE_CACHE_FILE_NAME);

	if (file != null)
	{
	    present = file.exists();
	}

	Log.d(TAG, "Sites cached: " + present);
	return present;
    }

    public static void cacheSiteList(Context context, ArrayList<Site> sites)
    {
	if (sites != null && sites.isEmpty() == false)
	{
	    Log.d(TAG, "Caching sites");

	    File cacheFile = new File(context.getCacheDir(), CacheFileName.SITE_CACHE_FILE_NAME);

	    if (cacheFile != null)
	    {
		try
		{
		    ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(cacheFile));
		    oo.writeObject(sites);
		    oo.close();
		}
		catch (FileNotFoundException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		catch (IOException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Site> fetchFromSiteListCache(Context context)
    {
	ArrayList<Site> sites = null;
	File file = new File(context.getCacheDir(), CacheFileName.SITE_CACHE_FILE_NAME);

	if (file != null && file.exists() == true)
	{
	    Log.d(TAG, "Fetch cached sites");

	    try
	    {
		ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file));
		sites = (ArrayList<Site>) oi.readObject();
		oi.close();
	    }
	    catch (StreamCorruptedException e)
	    {
		Log.e(TAG, e.getMessage());
	    }
	    catch (FileNotFoundException e)
	    {
		Log.e(TAG, e.getMessage());
	    }
	    catch (IOException e)
	    {
		Log.e(TAG, e.getMessage());
	    }
	    catch (ClassNotFoundException e)
	    {
		Log.e(TAG, e.getMessage());
	    }
	}

	return sites;
    }

    public static void clear(Context context)
    {
	if (context != null)
	{
	    File cacheDir = context.getCacheDir();
	    if (cacheDir != null && cacheDir.isDirectory())
	    {
		deleteDir(cacheDir);
	    }
	}
    }

    public static boolean deleteDir(File dir)
    {
	if (dir != null && dir.isDirectory())
	{
	    String[] children = dir.list();
	    for (int i = 0; i < children.length; i++)
	    {
		deleteDir(new File(dir, children[i]));
	    }
	}
	return dir.delete();
    }

    public static void cacheAccessToken(Context context, String accessToken)
    {
	if (accessToken != null)
	{
	    Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
	    prefEditor.putString(StringConstants.ACCESS_TOKEN, accessToken);
	    prefEditor.commit();
	}
    }

    public static String getAccessToken(Context context)
    {
	if (accessToken == null)
	{
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	    accessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);
	}

	return accessToken;
    }
}

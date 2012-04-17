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

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;

public class CacheUtils
{
    private static final String TAG = CacheUtils.class.getSimpleName();

    private static String userAccessToken;

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
	if (context != null && sites != null && sites.isEmpty() == false)
	{
	    Log.d(TAG, "Caching sites");

	    cacheObject(sites, context.getCacheDir(), CacheFileName.SITE_CACHE_FILE_NAME);
	}
    }

    public static void cacheQuestion(Context context, Question question)
    {
	Log.d(TAG, "Caching question");

	if (question != null && question.id > 0)
	{
	    File directory = new File(context.getCacheDir(), "questions");

	    cacheObject(question, directory, String.valueOf(question.id));
	}
    }

    public static void cacheObject(Object object, File directory, String fileName)
    {
	if (object != null && directory != null && fileName != null)
	{
	    if (directory.exists() == false)
	    {
		directory.mkdir();
	    }

	    File cacheFile = new File(directory, fileName);

	    if (cacheFile != null)
	    {
		try
		{
		    ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(cacheFile));
		    oo.writeObject(object);
		    oo.close();
		}
		catch (FileNotFoundException e)
		{
		    Log.e(TAG, e.getMessage());
		}
		catch (IOException e)
		{
		    Log.e(TAG, e.getMessage());
		}
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Site> fetchSiteListFromCache(Context context)
    {
	Log.d(TAG, context.getCacheDir().toString());
	return (ArrayList<Site>) readCachedObject(context.getCacheDir(), CacheFileName.SITE_CACHE_FILE_NAME);
    }

    public static ArrayList<Object> readCachedObjectDir(File directory)
    {
	ArrayList<Object> objects = null;
	
	if(directory != null && directory.isDirectory() && directory.exists() == true)
	{
	    objects = new ArrayList<Object>();
	    String[] fileNames = directory.list();
	    for(String fileName: fileNames)
	    {
		objects.add(readCachedObject(directory, fileName));
	    }
	}
	
	return objects;
    }

    public static Object readCachedObject(File directory, String fileName)
    {
	Object object = null;
	if (directory != null && directory.isDirectory() && fileName != null)
	{
	    File file = new File(directory, fileName);

	    if (file != null && file.exists() == true)
	    {
		Log.d(TAG, "Fetch cached objects");

		try
		{
		    ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file));
		    object = oi.readObject();
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
	}
	return object;
    }

    public static void clear(Context context)
    {
	if (context != null)
	{
	    Log.d(TAG, "Clearing cache");

	    File cacheDir = context.getCacheDir();
	    if (cacheDir != null && cacheDir.isDirectory())
	    {
		deleteDir(cacheDir);
	    }

	    Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
	    prefEditor.remove(StringConstants.ACCESS_TOKEN);
	    prefEditor.commit();
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
	    userAccessToken = accessToken;
	}
    }

    public static String getAccessToken(Context context)
    {
	if (userAccessToken == null && context != null)
	{
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	    userAccessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);
	}

	return userAccessToken;
    }
}

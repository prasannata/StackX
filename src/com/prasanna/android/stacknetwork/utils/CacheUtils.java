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
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Site;

public class CacheUtils
{
    private static final String TAG = CacheUtils.class.getSimpleName();

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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
            catch (ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return sites;
    }
}

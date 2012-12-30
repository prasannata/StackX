package com.prasanna.android.stacknetwork.utils;

import android.graphics.Bitmap;

public class IconCache extends LruCache<String, Bitmap>
{
    private static int CACHE_SIZE = 5;
    private static final IconCache INSTANCE = new IconCache();

    private IconCache()
    {
	super(CACHE_SIZE);
    }

    public static IconCache getInstance()
    {
	return INSTANCE;
    }
}

/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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

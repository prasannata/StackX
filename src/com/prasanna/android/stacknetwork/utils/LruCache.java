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

import java.lang.ref.SoftReference;

import com.prasanna.android.cache.LRU;

public class LruCache<K, T>
{
    private final int size;
    private final LRU<K, SoftReference<T>> lru;

    public LruCache(int size)
    {
	this.size = size;
	lru = new LRU<K, SoftReference<T>>(size);
    }

    public void add(K key, T value)
    {
	if (key != null && value != null)
	    lru.put(key, new SoftReference<T>(value));
    }

    public T get(K key)
    {
	T value = null;

	if (key != null && lru.containsKey(key))
	{
	    value = lru.get(key).get();
	    if (value == null)
		lru.remove(key);
	}

	return value;
    }

    public boolean containsKey(K key)
    {
	return lru.containsKey(key);
    }
    
    public int getSize()
    {
	return size;
    }
}
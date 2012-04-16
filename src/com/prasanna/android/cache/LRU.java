package com.prasanna.android.cache;

import java.util.LinkedHashMap;

public class LRU<K, V> extends LinkedHashMap<K, V>
{
    private static final long serialVersionUID = 1975258297688152046L;
    private final int capacity;

    public LRU(int capacity)
    {
        super(capacity, 1.1f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest)
    {
        return size() > capacity;
    }
}

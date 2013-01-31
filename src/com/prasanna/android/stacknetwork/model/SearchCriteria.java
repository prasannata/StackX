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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.prasanna.android.stacknetwork.utils.Validate;

public class SearchCriteria implements Serializable
{
    private static final long serialVersionUID = -2988724752735247609L;

    public static final String Q = "q";
    public static final String TITLE = "title";
    public static final String TAGGED = "tagged";
    public static final String ANSWERS = "answers";
    public static final String ACCEPTED = "accepted";
    public static final String PAGE = "page";
    public static final String PAGESIZE = "pagesize";
    public static final String SORT = "sort";

    public enum SearchSort
    {
        ACTIVITY("activity"),
        CREATION("creation"),
        VOTES("votes"),
        RELEVANCE("relevance");

        private final String val;

        private SearchSort(String val)
        {
            this.val = val;
        }

        public String getValue()
        {
            return val;
        }

        public static SearchSort getEnum(String value)
        {
            if (value == null)
                return null;

            try
            {
                return valueOf(value.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }
    }

    private static final int DEFAULT_SIZE = 15;
    private static WeakReference<SearchCriteria> selfWeakReference;
    private final HashMap<String, String> criteria;
    private ArrayList<String> includeTags;
    private ArrayList<String> excludeTags;
    int page = 1;

    public static SearchCriteria newCriteria(String query)
    {
        selfWeakReference = new WeakReference<SearchCriteria>(new SearchCriteria(query));
        return selfWeakReference.get();
    }

    public static SearchCriteria newCriteria()
    {
        selfWeakReference = new WeakReference<SearchCriteria>(new SearchCriteria());
        return selfWeakReference.get();
    }

    private SearchCriteria()
    {
        criteria = new HashMap<String, String>();
    }

    private SearchCriteria(String query)
    {
        criteria = new HashMap<String, String>();
        criteria.put(Q, query);
    }

    public SearchCriteria setMinAnswers(int minAns)
    {
        criteria.put(ANSWERS, String.valueOf(minAns));
        return this;
    }

    public SearchCriteria mustBeAnswered()
    {
        criteria.put(ACCEPTED, String.valueOf(true));
        return this;
    }

    public SearchCriteria sortBy(SearchSort searchSort)
    {
        criteria.put(SORT, searchSort.getValue());
        return this;
    }

    public SearchCriteria setPageSize(int pageSize)
    {
        criteria.put(PAGESIZE, String.valueOf(pageSize));
        return this;
    }

    public SearchCriteria addTagInclude(String tag)
    {
        if (includeTags == null)
            includeTags = new ArrayList<String>();

        if (!Validate.isEmptyString(tag))
            includeTags.add(tag);

        return this;
    }

    public SearchCriteria addTagExclude(String tag)
    {
        if (excludeTags == null)
            includeTags = new ArrayList<String>();

        if (!Validate.isEmptyString(tag))
            excludeTags.add(tag);

        return this;
    }

    public SearchCriteria build()
    {
        if (page == 1)
            criteria.put(PAGE, String.valueOf(page));

        if (!criteria.containsKey(PAGESIZE))
            setPageSize(DEFAULT_SIZE);

        return this;
    }

    public SearchCriteria nextPage()
    {
        criteria.put(PAGE, String.valueOf(page++));
        return this;
    }

    public SearchCriteria queryMustInTitle()
    {
        criteria.put(TITLE, criteria.get(Q));
        return this;
    }

    public void addCriteria(String name, String value)
    {
        criteria.put(name, value);
    }

    public Map<String, String> getMap()
    {
        return criteria;
    }

}

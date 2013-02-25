/*
 Copyright (C) 2013 Prasanna Thirumalai

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
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.prasanna.android.stacknetwork.utils.Validate;

public class SearchCriteria implements Serializable
{
    private static final long serialVersionUID = -2988724752735247609L;

    public static final String Q = "q";
    public static final String TITLE = "title";
    public static final String TAGGED = "tagged";
    public static final String NOT_TAGGED = "nottagged";
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
    private static SoftReference<SearchCriteria> self;
    private final HashMap<String, String> criteria;
    private HashSet<String> includeTags;
    private HashSet<String> excludeTags;
    private int page = 1;

    public static SearchCriteria newCriteria(String query)
    {
        self = new SoftReference<SearchCriteria>(new SearchCriteria(query));
        return self.get();
    }

    public static SearchCriteria newCriteria()
    {
        self = new SoftReference<SearchCriteria>(new SearchCriteria());
        return self.get();
    }

    private SearchCriteria()
    {
        criteria = new HashMap<String, String>();
    }

    private SearchCriteria(String query)
    {
        criteria = new HashMap<String, String>();
        setQuery(query);
    }

    public SearchCriteria setQuery(String query)
    {
        if (query != null)
            criteria.put(Q, query);
        return this;
    }

    public SearchCriteria setMinAnswers(int minAns)
    {
        if (minAns > 0)
            criteria.put(ANSWERS, String.valueOf(minAns));
        else
            criteria.remove(ANSWERS);
        return this;
    }

    public SearchCriteria clearAnswered()
    {
        criteria.remove(ACCEPTED);
        return this;
    }

    public SearchCriteria mustBeAnswered()
    {
        criteria.put(ACCEPTED, String.valueOf(true));
        return this;
    }

    public SearchCriteria sortBy(SearchSort searchSort)
    {
        if (searchSort != null)
            criteria.put(SORT, searchSort.getValue());
        return this;
    }

    public SearchCriteria setPageSize(int pageSize)
    {
        criteria.put(PAGESIZE, String.valueOf(pageSize));
        return this;
    }

    public SearchCriteria includeTag(String tag)
    {
        if (includeTags == null)
            includeTags = new HashSet<String>();

        if (!Validate.isEmptyString(tag))
            includeTags.add(tag);

        return this;
    }

    public SearchCriteria includeTags(Collection<String> tags)
    {
        if (tags != null)
        {
            if (includeTags == null)
                includeTags = new HashSet<String>();

            includeTags.addAll(tags);
        }
        return this;
    }

    public SearchCriteria excludeTag(String tag)
    {
        if (excludeTags == null)
            excludeTags = new HashSet<String>();

        if (!Validate.isEmptyString(tag))
            excludeTags.add(tag);

        return this;
    }

    public SearchCriteria excludeTags(Collection<String> tags)
    {
        if (tags != null)
        {
            if (excludeTags == null)
                excludeTags = new HashSet<String>();

            excludeTags.addAll(tags);
        }
        return this;
    }

    public SearchCriteria addIncludedTagsAsSemiColonDelimitedString(String tags)
    {
        if (tags != null)
            criteria.put(TAGGED, tags);

        return this;
    }

    public SearchCriteria addExcludedTagsAsSemiColonDelimitedString(String tags)
    {
        if (tags != null)
            criteria.put(NOT_TAGGED, tags);

        return this;
    }

    public String getQuery()
    {
        return criteria.get(Q);
    }

    public boolean isAnswered()
    {
        return criteria.get(ACCEPTED) != null && Boolean.valueOf(criteria.get(ACCEPTED));
    }

    public int getAnswerCount()
    {
        return criteria.get(ANSWERS) == null ? 0 : Integer.valueOf(criteria.get(ANSWERS));
    }

    public String getIncludedTagsAsSemicolonDelimitedString()
    {
        return criteria.get(TAGGED);
    }

    public String getExcludedTagsAsSemicolonDelimitedString()
    {
        return criteria.get(NOT_TAGGED);
    }

    public SearchCriteria build()
    {
        if (page == 1)
            criteria.put(PAGE, String.valueOf(page));

        if (!criteria.containsKey(PAGESIZE))
            setPageSize(DEFAULT_SIZE);

        if (includeTags != null && !includeTags.isEmpty())
            addCriteria(TAGGED, getAsDelimitedString(includeTags, ";"));

        if (excludeTags != null && !excludeTags.isEmpty())
            addCriteria(NOT_TAGGED, getAsDelimitedString(excludeTags, ";"));

        return this;
    }

    private String getAsDelimitedString(HashSet<String> tags, String delim)
    {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = tags.iterator();

        while (iterator.hasNext())
        {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(delim);
        }

        return sb.toString();
    }

    public String getSort()
    {
        return criteria.get(SORT);
    }

    public SearchCriteria nextPage()
    {
        criteria.put(PAGE, String.valueOf(++page));
        return this;
    }

    public SearchCriteria queryMustInTitle()
    {
        criteria.put(TITLE, criteria.get(Q));
        return this;
    }

    public String[] getTaggedArray()
    {
        String tags = getIncludedTagsAsSemicolonDelimitedString();
        if (tags == null)
            return null;

        return tags.split(";");
    }

    public String[] getNotTaggedArray()
    {
        String tags = getExcludedTagsAsSemicolonDelimitedString();
        if (tags == null)
            return null;

        return tags.split(";");
    }

    public void addCriteria(String name, String value)
    {
        criteria.put(name, value);
    }

    public Map<String, String> getMap()
    {
        return criteria;
    }

    public SearchCriteria clear()
    {
        if (criteria != null)
            criteria.clear();

        if (includeTags != null)
            includeTags.clear();

        if (excludeTags != null)
            excludeTags.clear();

        page = 1;
        return this;
    }

    public static SearchCriteria copy(SearchCriteria that)
    {
        if (that != null)
        {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setQuery(that.getQuery());
            criteria.setMinAnswers(criteria.getAnswerCount());
            if (criteria.isAnswered())
                criteria.mustBeAnswered();
            String[] tagArray = that.getTaggedArray();
            if (tagArray != null)
            {
                for (String tag : tagArray)
                    criteria.includeTag(tag);
            }

            tagArray = that.getNotTaggedArray();
            if (tagArray != null)
            {
                for (String tag : that.getNotTaggedArray())
                    criteria.excludeTag(tag);
            }
            return criteria;
        }

        return null;
    }
}

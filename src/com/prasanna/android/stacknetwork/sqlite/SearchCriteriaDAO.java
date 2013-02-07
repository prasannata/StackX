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

package com.prasanna.android.stacknetwork.sqlite;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;

public class SearchCriteriaDAO extends AbstractBaseDao
{
    public static final String TABLE_NAME = "SEARCH_CRITERIA";

    public static final class SearchCriteriaTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_Q = "q";
        public static final String COLUMN_SORT = "sort";
        public static final String COLUMN_ANSWERS = "answers";
        public static final String COLUMN_ANSWERED = "answered";
        public static final String COLUMN_TAGGED = "tagged";
        public static final String COLUMN_NOT_TAGGED = "not_tagged";
        public static final String COLUMN_TAB = "tab";
        public static final String COLUMN_RUN_COUNT = "run_count";
        public static final String COLUMN_LAST_RUN = "last_run";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_LAST_MODIFIED = "last_modified";

        protected static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID
                        + " integer primary key autoincrement, " + COLUMN_NAME + " text not null, " + COLUMN_SITE
                        + " text not null, " + COLUMN_Q + " text, " + COLUMN_SORT + " text, " + COLUMN_ANSWERS
                        + " integer, " + COLUMN_ANSWERED + " integer, " + COLUMN_TAGGED + " text, " + COLUMN_NOT_TAGGED
                        + " text, " + COLUMN_TAB + " integer DEFAULT \'0\', " + COLUMN_RUN_COUNT
                        + " long DEFAULT \'0\', " + COLUMN_LAST_RUN + " long DEFAULTL \'0\', " + COLUMN_CREATED
                        + " long not null, " + COLUMN_LAST_MODIFIED + " long not null);";
    }

    public SearchCriteriaDAO(Context context)
    {
        super(context);
    }

    public long insert(SearchCriteriaDomain searchCriteriaDomain)
    {
        if (searchCriteriaDomain != null && searchCriteriaDomain.searchCriteria != null)
        {
            ContentValues values = new ContentValues();
            values.put(SearchCriteriaTable.COLUMN_NAME, searchCriteriaDomain.name);
            values.put(SearchCriteriaTable.COLUMN_SITE, searchCriteriaDomain.site);
            values.put(SearchCriteriaTable.COLUMN_RUN_COUNT, searchCriteriaDomain.runCount);
            values.put(SearchCriteriaTable.COLUMN_LAST_RUN, searchCriteriaDomain.lastRun);
            values.put(SearchCriteriaTable.COLUMN_Q, searchCriteriaDomain.searchCriteria.getQuery());
            values.put(SearchCriteriaTable.COLUMN_SORT, searchCriteriaDomain.searchCriteria.getSort());
            values.put(SearchCriteriaTable.COLUMN_ANSWERS, searchCriteriaDomain.searchCriteria.getAnswerCount());
            values.put(SearchCriteriaTable.COLUMN_ANSWERED, searchCriteriaDomain.searchCriteria.isAnswered());
            values.put(SearchCriteriaTable.COLUMN_TAGGED,
                            searchCriteriaDomain.searchCriteria.getIncludedTagsAsSemicolonDelimitedString());
            values.put(SearchCriteriaTable.COLUMN_NOT_TAGGED,
                            searchCriteriaDomain.searchCriteria.getExcludedTagsAsSemicolonDelimitedString());

            long currentTimeMillis = System.currentTimeMillis();
            values.put(SearchCriteriaTable.COLUMN_CREATED, currentTimeMillis);
            values.put(SearchCriteriaTable.COLUMN_LAST_MODIFIED, currentTimeMillis);

            return database.insert(TABLE_NAME, null, values);
        }

        return -1L;
    }

    public SearchCriteriaDomain update(SearchCriteriaDomain searchCriteriaDomain)
    {
        if (searchCriteriaDomain != null)
        {
            database.beginTransaction();

            try
            {
                delete(searchCriteriaDomain.id);
                searchCriteriaDomain.id = insert(searchCriteriaDomain);
                database.setTransactionSuccessful();
            }
            catch (SQLException e)
            {
                Log.d(TABLE_NAME, "Update failed: " + e.getMessage());
            }
            finally
            {
                database.endTransaction();
            }

        }

        return searchCriteriaDomain;
    }

    public void updateCriteriaAsTabbed(long id, boolean add)
    {
        String whereClause = SearchCriteriaTable.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        ContentValues values = new ContentValues();
        values.put(SearchCriteriaTable.COLUMN_TAB, add);
        database.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public ArrayList<SearchCriteriaDomain> readAll(String site)
    {
        String selection = SearchCriteriaTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };

        String orderBy = SearchCriteriaTable.COLUMN_CREATED + " ASC";

        Cursor cursor = database.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);

        if (cursor == null || cursor.getCount() == 0)
            return null;

        ArrayList<SearchCriteriaDomain> criteriaList = new ArrayList<SearchCriteriaDomain>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            criteriaList.add(getCriteria(cursor));
            cursor.moveToNext();
        }

        return criteriaList;
    }

    private SearchCriteriaDomain getCriteria(Cursor cursor)
    {
        SearchCriteriaDomain domain = new SearchCriteriaDomain();
        SearchCriteria criteria = SearchCriteria.newCriteria();

        domain.id = cursor.getInt(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_ID));
        domain.name = cursor.getString(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_NAME));
        domain.site = cursor.getString(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_SITE));
        domain.created = cursor.getLong(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_CREATED));
        domain.tab = cursor.getInt(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_TAB)) == 1;
        domain.runCount = cursor.getInt(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_RUN_COUNT));
        domain.lastRun = cursor.getLong(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_LAST_RUN));
        domain.lastModified = cursor.getLong(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_LAST_MODIFIED));

        String sort = cursor.getString(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_SORT));
        if (sort != null)
            criteria.sortBy(SearchSort.getEnum(sort));
        criteria.setQuery(cursor.getString(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_Q)));
        criteria.setMinAnswers(cursor.getInt(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_ANSWERS)));
        criteria.addIncludedTagsAsSemiColonDelimitedString(cursor.getString(cursor
                        .getColumnIndex(SearchCriteriaTable.COLUMN_TAGGED)));
        criteria.addExcludedTagsAsSemiColonDelimitedString(cursor.getString(cursor
                        .getColumnIndex(SearchCriteriaTable.COLUMN_NOT_TAGGED)));
        if (cursor.getInt(cursor.getColumnIndex(SearchCriteriaTable.COLUMN_ANSWERED)) == 1)
            criteria.mustBeAnswered();

        domain.searchCriteria = criteria;
        return domain;
    }

    public void delete(long id)
    {
        String whereClause = SearchCriteriaTable.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };

        database.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public void deleteAll(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
                delete(id);
        }
    }
}

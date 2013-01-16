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

package com.prasanna.android.stacknetwork.sqlite;

import java.util.HashSet;
import java.util.LinkedHashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.TagsTable;

public class TagDAO
{
    private static final String TAG = TagDAO.class.getSimpleName();

    private final DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public TagDAO(Context context)
    {
        databaseHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException
    {
        database = databaseHelper.getWritableDatabase();
    }

    public void close()
    {
        databaseHelper.close();
    }

    public void insert(HashSet<String> tags)
    {
        insertTags(DatabaseHelper.TABLE_TAGS, tags);
    }

    public void insertMyTags(HashSet<String> tags)
    {
        insertTags(DatabaseHelper.TABLE_MY_TAGS, tags);
    }

    private void insertTags(String tableName, HashSet<String> tags)
    {
        if (tags != null)
        {
            Log.d(TAG, "inserting tags into DB");

            for (String tag : tags)
            {
                ContentValues values = new ContentValues();
                values.put(TagsTable.COLUMN_VALUE, tag);
                database.insert(tableName, null, values);
            }
        }
    }

    public LinkedHashSet<String> getTags()
    {
        return selectTags(DatabaseHelper.TABLE_TAGS, new String[]
        { TagsTable.COLUMN_VALUE });
    }

    public LinkedHashSet<String> getMyTags()
    {
        return selectTagsSortAlphabetically(DatabaseHelper.TABLE_MY_TAGS, new String[]
        { TagsTable.COLUMN_VALUE });
    }

    private LinkedHashSet<String> selectTags(String tableName, String[] cols)
    {
        Cursor cursor = database.query(tableName, cols, null, null, null, null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Tags retrieved from DB");

        return getTagSet(cursor);
    }

    private LinkedHashSet<String> selectTagsSortAlphabetically(String tableName, String[] cols)
    {
        Cursor cursor = database.query(tableName, cols, null, null, null, null, TagsTable.COLUMN_VALUE
                        + " Collate NOCASE");
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Tags retrieved from DB");

        return getTagSet(cursor);
    }

    private LinkedHashSet<String> getTagSet(Cursor cursor)
    {
        LinkedHashSet<String> tags = new LinkedHashSet<String>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            tags.add(cursor.getString(0));
            cursor.moveToNext();
        }

        return tags;
    }

    public void deleteAll()
    {
        database.delete(DatabaseHelper.TABLE_MY_TAGS, null, null);
    }
}

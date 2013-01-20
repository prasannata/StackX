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

import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.TagsAuditTable;
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

    public void insert(String site, HashSet<String> tags)
    {
        if (tags != null && !tags.isEmpty())
        {
            Log.d(TAG, "inserting tags into DB");

            for (String tag : tags)
            {
                ContentValues values = new ContentValues();
                values.put(TagsTable.COLUMN_VALUE, tag);
                values.put(TagsTable.COLUMN_SITE, site);
                database.insert(DatabaseHelper.TABLE_TAGS, null, values);
            }

            insertAuditEntry(site);
        }
    }

    private void insertAuditEntry(String site)
    {
        ContentValues values = new ContentValues();
        values.put(TagsAuditTable.COLUMN_SITE, site);
        values.put(TagsAuditTable.COLUMN_LAST_UPDATE_TIME, System.currentTimeMillis());
        Log.d(TAG, "Audit entry for tags: " +  values.toString());
        database.insert(DatabaseHelper.TABLE_TAGS_AUDIT, null, values);
    }

    public long getLastUpdateTime(String site)
    {
        String[] cols = new String[] { TagsAuditTable.COLUMN_LAST_UPDATE_TIME };
        String selection = DatabaseHelper.TagsAuditTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_TAGS_AUDIT, cols, selection, selectionArgs, null,
                        null, null);

        if (cursor == null || cursor.getCount() == 0)
        {
            Log.d(TAG, "getLastUpdateTime for " + site + ", no entries found");
            return 0L;
        }

        cursor.moveToFirst();

        return cursor.getLong(0);
    }

    public LinkedHashSet<String> getTags(String site)
    {
        String[] cols = new String[] { TagsTable.COLUMN_VALUE };
        String selection = DatabaseHelper.TagsTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };
        String orderBy = TagsTable.COLUMN_VALUE + " Collate NOCASE";

        Cursor cursor = database.query(DatabaseHelper.TABLE_TAGS, cols, selection, selectionArgs, null, null, orderBy);
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
        database.delete(DatabaseHelper.TABLE_TAGS, null, null);
    }

    public void deleteTagsForSite(String site)
    {
        String whereClause = TagsTable.COLUMN_SITE + " = ?";
        String[] whereArgs = { site };
        database.delete(DatabaseHelper.TABLE_TAGS, whereClause, whereArgs);
        
        deleteAuditEntry(site);
    }

    private void deleteAuditEntry(String site)
    {
        String whereClause = TagsTable.COLUMN_SITE + " = ?";
        String[] whereArgs = { site };
        database.delete(DatabaseHelper.TABLE_TAGS_AUDIT, whereClause, whereArgs);
    }

}

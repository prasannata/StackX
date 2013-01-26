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

import com.prasanna.android.stacknetwork.model.Tag;
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

    public void insert(String site, String tag, boolean isLocalAdd)
    {
        if (site != null && tag != null)
        {
            Log.d(TAG, "inserting " + tag + " into DB for site " + site);

            ContentValues values = new ContentValues();
            values.put(TagsTable.COLUMN_ID, tag.toLowerCase().hashCode());
            values.put(TagsTable.COLUMN_VALUE, tag);
            values.put(TagsTable.COLUMN_SITE, site);
            values.put(TagsTable.COLUMN_LOCAL_ADD, isLocalAdd);
            database.insert(DatabaseHelper.TABLE_TAGS, null, values);
        }
    }

    public void insert(String site, HashSet<Tag> tags)
    {
        if (tags != null && !tags.isEmpty())
        {
            Log.d(TAG, "inserting tags into DB for site " + site);

            for (Tag tag : tags)
            {
                ContentValues values = new ContentValues();
                values.put(TagsTable.COLUMN_ID, tag.name.hashCode());
                values.put(TagsTable.COLUMN_VALUE, tag.name);
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
        Log.d(TAG, "Audit entry for tags: " + values.toString());
        database.insert(DatabaseHelper.TABLE_TAGS_AUDIT, null, values);
    }

    public long getLastUpdateTime(String site)
    {
        String[] cols = new String[] { TagsAuditTable.COLUMN_LAST_UPDATE_TIME };
        String selection = DatabaseHelper.TagsAuditTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_TAGS_AUDIT, cols, selection, selectionArgs, null, null,
                        null);

        if (cursor == null || cursor.getCount() == 0)
        {
            Log.d(TAG, "getLastUpdateTime for " + site + ", no entries found");
            return 0L;
        }

        cursor.moveToFirst();

        return cursor.getLong(0);
    }

    public LinkedHashSet<Tag> getTags(String site)
    {
        String[] cols = new String[] { TagsTable.COLUMN_VALUE, TagsTable.COLUMN_LOCAL_ADD };
        String selection = DatabaseHelper.TagsTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };
        String orderBy = TagsTable.COLUMN_VALUE + " Collate NOCASE";

        Cursor cursor = database.query(DatabaseHelper.TABLE_TAGS, cols, selection, selectionArgs, null, null, orderBy);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Tags retrieved from DB");

        return getTagSet(cursor);
    }

    private LinkedHashSet<Tag> getTagSet(Cursor cursor)
    {
        LinkedHashSet<Tag> tags = new LinkedHashSet<Tag>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Tag tag = new Tag(cursor.getString(0));
            tag.local = cursor.getInt(1) == 1;
            tags.add(tag);
            cursor.moveToNext();
        }

        return tags;
    }

    public void deleteAll()
    {
        database.delete(DatabaseHelper.TABLE_TAGS, null, null);
    }

    public void deleteTagsFromServerForSite(String site)
    {
        String whereClause = TagsTable.COLUMN_SITE + " = ? and " + TagsTable.COLUMN_LOCAL_ADD + " = ?";
        String[] whereArgs = { site, "0" };

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

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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Tag;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.AuditTable;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.TagsTable;

public class TagDAO extends AbstractBaseDao
{
    private static final String TAG = TagDAO.class.getSimpleName();
    public static final String AUDIT_ENTRY_TYPE = "tag";

    public TagDAO(Context context)
    {
        super(context);
    }

    public void insert(String site, String tag, boolean isLocalAdd)
    {
        if (site != null && tag != null)
        {
            Log.d(TAG, "inserting " + tag + " into DB for site " + site);

            ContentValues values = new ContentValues();
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
        values.put(AuditTable.COLUMN_SITE, site);
        values.put(AuditTable.COLUMN_TYPE, AUDIT_ENTRY_TYPE);
        values.put(AuditTable.COLUMN_LAST_UPDATE_TIME, System.currentTimeMillis());
        Log.d(TAG, "Audit entry for tags: " + values.toString());
        database.insert(DatabaseHelper.TABLE_AUDIT, null, values);
    }

    public long getLastUpdateTime(String site)
    {
        String[] cols = new String[] { AuditTable.COLUMN_LAST_UPDATE_TIME };
        String selection = DatabaseHelper.AuditTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_AUDIT, cols, selection, selectionArgs, null, null,
                        null);

        if (cursor == null || cursor.getCount() == 0)
        {
            Log.d(TAG, "getLastUpdateTime for " + site + ", no entries found");
            return 0L;
        }

        cursor.moveToFirst();

        return cursor.getLong(0);
    }

    public LinkedHashSet<Tag> getTagSet(String site)
    {
        Cursor cursor = getCursor(site);
        
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Tags retrieved from DB");

        LinkedHashSet<Tag> tags = new LinkedHashSet<Tag>();
        getTagCollection(cursor, tags);
        return tags;
    }

    public ArrayList<String> getTagStringList(String site)
    {
        Cursor cursor = getCursor(site);
        
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Tags retrieved from DB");

        ArrayList<String> tags = new ArrayList<String>();
        getTagStringCollection(cursor, tags);
        return tags;
    }

    private void getTagStringCollection(Cursor cursor, ArrayList<String> tags)
    {
        cursor.moveToFirst();
        
        while (!cursor.isAfterLast())
        {
            Tag tag = new Tag(cursor.getString(cursor.getColumnIndex(TagsTable.COLUMN_VALUE)));
            tag.local = cursor.getInt(cursor.getColumnIndex(TagsTable.COLUMN_LOCAL_ADD)) == 1;
            tags.add(tag.name);
            cursor.moveToNext();
        }
    }

    private Cursor getCursor(String site)
    {
        String[] cols = new String[] { TagsTable.COLUMN_VALUE, TagsTable.COLUMN_LOCAL_ADD };
        String selection = DatabaseHelper.TagsTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };
        String orderBy = TagsTable.COLUMN_VALUE + " Collate NOCASE";

        Cursor cursor = database.query(DatabaseHelper.TABLE_TAGS, cols, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    public LinkedHashSet<Tag> getTags(String site, boolean includeLocalTags)
    {
        String[] cols = new String[] { TagsTable.COLUMN_VALUE, TagsTable.COLUMN_LOCAL_ADD };
        String selection = DatabaseHelper.TagsTable.COLUMN_SITE + " = ? and "
                        + DatabaseHelper.TagsTable.COLUMN_LOCAL_ADD + " = ?";
        String[] selectionArgs = { site, includeLocalTags ? "1" : "0" };
        String orderBy = TagsTable.COLUMN_VALUE + " Collate NOCASE";

        Cursor cursor = database.query(DatabaseHelper.TABLE_TAGS, cols, selection, selectionArgs, null, null, orderBy);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Tags retrieved from DB");

        LinkedHashSet<Tag> tags = new LinkedHashSet<Tag>();
        getTagCollection(cursor, tags);
        return tags;
    }

    private void getTagCollection(Cursor cursor, Collection<Tag> destination)
    {
        cursor.moveToFirst();
        
        while (!cursor.isAfterLast())
        {
            Tag tag = new Tag(cursor.getString(cursor.getColumnIndex(TagsTable.COLUMN_VALUE)));
            tag.local = cursor.getInt(cursor.getColumnIndex(TagsTable.COLUMN_LOCAL_ADD)) == 1;
            destination.add(tag);
            cursor.moveToNext();
        }
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
        database.delete(DatabaseHelper.TABLE_AUDIT, whereClause, whereArgs);
    }

}

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
import android.database.SQLException;

import com.prasanna.android.stacknetwork.model.Tag;
import com.prasanna.android.utils.LogWrapper;

public class TagDAO extends AbstractBaseDao
{
    private static final String TAG = TagDAO.class.getSimpleName();
    public static final String TABLE_NAME = "TAGS";
    public static final String AUDIT_ENTRY_TYPE = "tag";

    public static final class TagsTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_LOCAL_ADD = "local_add";

        protected static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID
                        + " integer primary key autoincrement, " + COLUMN_VALUE + " text not null, " + COLUMN_SITE
                        + " text not null, " + COLUMN_LOCAL_ADD + " integer DEFAULT 0);";
    }

    public TagDAO(Context context)
    {
        super(context);
    }

    public void insert(String site, String tag, boolean isLocalAdd)
    {
        if (site != null && tag != null)
        {
            LogWrapper.d(TAG, "inserting " + tag + " into DB for site " + site);

            ContentValues values = new ContentValues();
            values.put(TagsTable.COLUMN_VALUE, tag);
            values.put(TagsTable.COLUMN_SITE, site);
            values.put(TagsTable.COLUMN_LOCAL_ADD, isLocalAdd);
            database.insert(TABLE_NAME, null, values);
        }
    }

    public boolean insert(String site, HashSet<Tag> tags)
    {
        if (tags != null && !tags.isEmpty())
        {
            LogWrapper.d(TAG, "inserting tags into DB for site " + site);

            try
            {
                database.beginTransaction();

                for (Tag tag : tags)
                {
                    ContentValues values = new ContentValues();
                    values.put(TagsTable.COLUMN_VALUE, tag.name);
                    values.put(TagsTable.COLUMN_SITE, site);
                    database.insert(TABLE_NAME, null, values);
                }

                insertAuditEntry(AUDIT_ENTRY_TYPE, site);

                database.setTransactionSuccessful();
                return true;
            }
            catch (SQLException e)
            {
                LogWrapper.e(TABLE_NAME, "insert failed: " + e.getMessage());
            }
            finally
            {
                database.endTransaction();
            }
        }

        return false;
    }

    public long getLastUpdateTime(String site)
    {
        return getLastUpdateTime(AUDIT_ENTRY_TYPE, site);
    }

    public LinkedHashSet<Tag> getTagSet(String site)
    {
        Cursor cursor = getCursor(site);

        if (cursor == null)
            return null;

        LinkedHashSet<Tag> tags = new LinkedHashSet<Tag>();
        getTagCollection(cursor, tags);
        return tags;
    }

    public ArrayList<String> getTagStringList(String site)
    {
        Cursor cursor = getCursor(site);

        if (cursor == null || cursor.getCount() == 0)
            return null;

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
            if (!tags.contains(tag.name))
                tags.add(tag.name);
            cursor.moveToNext();
        }
    }

    private Cursor getCursor(String site)
    {
        String[] cols = new String[]
        { TagsTable.COLUMN_VALUE, TagsTable.COLUMN_LOCAL_ADD };
        String selection = TagsTable.COLUMN_SITE + " = ?";
        String[] selectionArgs =
        { site };
        String orderBy = TagsTable.COLUMN_VALUE + " Collate NOCASE";

        Cursor cursor = database.query(TABLE_NAME, cols, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    public LinkedHashSet<Tag> getTags(String site, boolean includeLocalTags)
    {
        String[] cols = new String[]
        { TagsTable.COLUMN_VALUE, TagsTable.COLUMN_LOCAL_ADD };
        String selection = TagsTable.COLUMN_SITE + " = ? and " + TagsTable.COLUMN_LOCAL_ADD + " = ?";
        String[] selectionArgs =
        { site, includeLocalTags ? "1" : "0" };
        String orderBy = TagsTable.COLUMN_VALUE + " Collate NOCASE";

        Cursor cursor = database.query(TABLE_NAME, cols, selection, selectionArgs, null, null, orderBy);
        if (cursor == null || cursor.getCount() == 0)
            return null;

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
        database.delete(TABLE_NAME, null, null);
        deleteAuditEntry(AUDIT_ENTRY_TYPE, null);
    }

    public void deleteTagsFromServerForSite(String site)
    {
        String whereClause = TagsTable.COLUMN_SITE + " = ? and " + TagsTable.COLUMN_LOCAL_ADD + " = ?";
        String[] whereArgs =
        { site, "0" };

        database.delete(TABLE_NAME, whereClause, whereArgs);
        deleteAuditEntry(AUDIT_ENTRY_TYPE, site);
    }

    public static ArrayList<String> get(Context context, String site)
    {
        TagDAO tagDao = new TagDAO(context);
        try
        {
            tagDao.open();
            return tagDao.getTagStringList(site);
        }
        catch (SQLException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        finally
        {
            tagDao.close();
        }
        return null;
    }

    public static LinkedHashSet<Tag> getTagSet(Context context, String site)
    {
        TagDAO tagDao = new TagDAO(context);
        try
        {
            tagDao.open();
            return tagDao.getTagSet(site);
        }
        catch (SQLException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        finally
        {
            tagDao.close();
        }
        return null;
    }

    public static void purge(Context context)
    {
        TagDAO tagDAO = new TagDAO(context);
        try
        {
            tagDAO.open();
            tagDAO.deleteAll();
        }
        catch (SQLException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        finally
        {
            tagDAO.close();
        }
    }
}

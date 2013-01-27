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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.WritePermissionTable;

public class WritePermissionDAO
{
    private static final String TAG = WritePermissionDAO.class.getSimpleName();

    private final DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public WritePermissionDAO(Context context)
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

    public void insertAll(Site site, ArrayList<WritePermission> permissions)
    {
        if (permissions != null)
        {
            Log.d(TAG, "Storing permissions");

            for (WritePermission permission : permissions)
            {
                Log.d(TAG, permission.objectType + " add: " + permission.canAdd + ", edit: " + permission.canEdit
                                + ", delete: " + permission.canDelete);
                ContentValues values = new ContentValues();
                values.put(WritePermissionTable.COLUMN_ADD, permission.canAdd);
                values.put(WritePermissionTable.COLUMN_EDIT, permission.canEdit);
                values.put(WritePermissionTable.COLUMN_DEL, permission.canDelete);
                if (permission.objectType != null)
                    values.put(WritePermissionTable.COLUMN_OBJECT_TYPE, permission.objectType.getValue());
                values.put(WritePermissionTable.COLUMN_MAX_DAILY_ACTIONS, permission.maxDailyActions);
                values.put(WritePermissionTable.COLUMN_WAIT_TIME, permission.minSecondsBetweenActions);
                values.put(WritePermissionTable.COLUMN_SITE, site.apiSiteParameter);
                values.put(WritePermissionTable.COLUMN_SITE_URL, site.link);

                database.insert(DatabaseHelper.TABLE_WRITE_PERMISSION, null, values);
            }
        }
    }

    public WritePermission getPermission(String site, ObjectType objectType)
    {
        if (site == null || objectType == null)
            return null;

        String selection = WritePermissionTable.COLUMN_SITE + " = ? and" + WritePermissionTable.COLUMN_OBJECT_TYPE
                        + " = ?";
        String[] selectionArgs = { site, objectType.getValue() };

        Cursor cursor = database.query(DatabaseHelper.TABLE_WRITE_PERMISSION, null, selection, selectionArgs, null,
                        null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Permission retrieved from DB");

        return getPermission(cursor);
    }

    public ArrayList<String> getSites()
    {
        String[] cols = { WritePermissionTable.COLUMN_SITE };
        String selection = WritePermissionTable.COLUMN_ADD + " = ? and" + WritePermissionTable.COLUMN_DEL + " = ? and "
                        + WritePermissionTable.COLUMN_EDIT + "= ?";
        String[] selectionArgs = { "1", "1", "1" };

        Cursor cursor = database.query(DatabaseHelper.TABLE_WRITE_PERMISSION, cols, selection, selectionArgs, null,
                        null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        ArrayList<String> sites = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            sites.add(cursor.getString(0));
            cursor.moveToNext();
        }
        return sites;
    }

    public ArrayList<WritePermission> getPermissions(String site)
    {
        if (site == null)
            return null;

        String selection = WritePermissionTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_WRITE_PERMISSION, null, selection, selectionArgs, null,
                        null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Permissions retrieved from DB");

        return getPermissions(cursor);
    }

    private ArrayList<WritePermission> getPermissions(Cursor cursor)
    {
        ArrayList<WritePermission> permissions = new ArrayList<WritePermission>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            permissions.add(getPermission(cursor));
            cursor.moveToNext();
        }

        return permissions;
    }

    private WritePermission getPermission(Cursor cursor)
    {
        WritePermission permission = new WritePermission();
        permission.canAdd = cursor.getInt(cursor.getColumnIndex(WritePermissionTable.COLUMN_ADD)) == 1;
        permission.canDelete = cursor.getInt(cursor.getColumnIndex(WritePermissionTable.COLUMN_DEL)) == 1;
        permission.canEdit = cursor.getInt(cursor.getColumnIndex(WritePermissionTable.COLUMN_EDIT)) == 1;
        permission.maxDailyActions = cursor
                        .getInt(cursor.getColumnIndex(WritePermissionTable.COLUMN_MAX_DAILY_ACTIONS));
        permission.minSecondsBetweenActions = cursor.getInt(cursor
                        .getColumnIndex(WritePermissionTable.COLUMN_WAIT_TIME));
        permission.objectType = ObjectType.getEnum(cursor.getString(cursor
                        .getColumnIndex(WritePermissionTable.COLUMN_OBJECT_TYPE)));
        return permission;
    }
}

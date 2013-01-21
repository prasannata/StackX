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

import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.model.Permission;
import com.prasanna.android.stacknetwork.model.Permission.ObjectType;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.WritePermissionTable;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

public class WritePermissionDAO
{
    private static final String TAG = WritePermissionDAO.class.getSimpleName();

    private final DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    private Context context;

    public WritePermissionDAO(Context context)
    {
        this.context = context;
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

    public void insertAll(String site, ArrayList<Permission> permissions)
    {
        if (permissions != null)
        {
            Log.d(TAG, "Storing permissions");

            for (Permission permission : permissions)
            {
                ContentValues values = new ContentValues();
                values.put(WritePermissionTable.COLUMN_ADD, permission.canAdd);
                values.put(WritePermissionTable.COLUMN_EDIT, permission.canEdit);
                values.put(WritePermissionTable.COLUMN_DEL, permission.canDelete);
                if (permission.objectType != null)
                {
                    values.put(WritePermissionTable.COLUMN_OBJECT_TYPE, permission.objectType.getValue());
                    SharedPreferencesUtil.setOnOff(context, SettingsFragment.PREFIX_KEY_PREF_WRITE_PERMISSION + site
                                    + "_" + permission.objectType.getValue(), permission.canAdd & permission.canDelete
                                    & permission.canEdit);
                }
                values.put(WritePermissionTable.COLUMN_MAX_DAILY_ACTIONS, permission.maxDailyActions);
                values.put(WritePermissionTable.COLUMN_WAIT_TIME, permission.minSecondsBetweenActions);
                values.put(WritePermissionTable.COLUMN_SITE, site);
                database.insert(DatabaseHelper.TABLE_WRITE_PERMISSION, null, values);
            }
        }
    }

    public Permission getPermission(String site, ObjectType objectType)
    {
        if (site == null || objectType == null)
            return null;

        String selection = WritePermissionTable.COLUMN_SITE + " = ? and" + WritePermissionTable.COLUMN_OBJECT_TYPE
                        + " = ?";
        String[] selectionArgs =
        { site, objectType.getValue() };

        Cursor cursor = database.query(DatabaseHelper.TABLE_WRITE_PERMISSION, null, selection, selectionArgs, null,
                        null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Permission retrieved from DB");

        return getPermission(cursor);
    }

    public ArrayList<Permission> getPermissions(String site)
    {
        if (site == null)
            return null;

        String selection = WritePermissionTable.COLUMN_SITE + " = ?";
        String[] selectionArgs =
        { site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_WRITE_PERMISSION, null, selection, selectionArgs, null,
                        null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Permissions retrieved from DB");

        return getPermissions(cursor);
    }

    private ArrayList<Permission> getPermissions(Cursor cursor)
    {
        ArrayList<Permission> permissions = new ArrayList<Permission>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            permissions.add(getPermission(cursor));

            cursor.moveToNext();
        }

        return permissions;
    }

    private Permission getPermission(Cursor cursor)
    {
        int colIdx = 0;
        Permission permission = new Permission();
        permission.canAdd = cursor.getInt(colIdx++) == 1;
        permission.canDelete = cursor.getInt(colIdx++) == 1;
        permission.canEdit = cursor.getInt(colIdx++) == 1;
        permission.maxDailyActions = cursor.getInt(colIdx++);
        permission.minSecondsBetweenActions = cursor.getInt(colIdx++);
        permission.objectType = ObjectType.getEnum(cursor.getString(colIdx++));
        return permission;
    }
}

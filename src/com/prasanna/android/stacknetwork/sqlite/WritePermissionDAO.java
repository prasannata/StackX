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
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;

public class WritePermissionDAO extends AbstractBaseDao
{
    private static final String TAG = WritePermissionDAO.class.getSimpleName();
    public static final String TABLE_NAME = "WRITE_PERMISSION";

    public static final class WritePermissionTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ADD = "can_add";
        public static final String COLUMN_DEL = "can_del";
        public static final String COLUMN_EDIT = "can_edit";
        public static final String COLUMN_MAX_DAILY_ACTIONS = "maxDailyActions";
        public static final String COLUMN_WAIT_TIME = "waitBetweenWrite";
        public static final String COLUMN_OBJECT_TYPE = "objectType";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SITE_URL = "site_url";

        protected static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID
                        + " integer primary key autoincrement, " + COLUMN_ADD + " integer not null, " + COLUMN_DEL
                        + " integer not null, " + COLUMN_EDIT + " integer not null, " + COLUMN_MAX_DAILY_ACTIONS
                        + " integer not null, " + COLUMN_WAIT_TIME + " integer not null, " + COLUMN_OBJECT_TYPE
                        + " text not null, " + COLUMN_SITE + " text not null, " + COLUMN_SITE_URL + " text not null);";
    }

    public WritePermissionDAO(Context context)
    {
        super(context);
    }

    public void insert(Site site, ArrayList<WritePermission> permissions)
    {
        if (site != null && permissions != null)
        {
            Log.d(TAG, "Storing permissions");

            database.beginTransaction();
            try
            {
                for (WritePermission permission : permissions)
                {
                    if (permission.objectType != null)
                    {
                        Log.d(TAG, permission.objectType + " add: " + permission.canAdd + ", edit: "
                                        + permission.canEdit + ", delete: " + permission.canDelete);
                        database.insert(TABLE_NAME, null, getContentValues(site, permission));
                    }
                    else
                    {
                        Log.w(TAG, "Object type null for permission for site: " + site.apiSiteParameter);
                    }
                }

                database.setTransactionSuccessful();
            }
            catch (SQLException e)
            {

            }
            finally
            {
                database.endTransaction();
            }
        }
    }

    private ContentValues getContentValues(Site site, WritePermission permission)
    {
        ContentValues values = new ContentValues();
        values.put(WritePermissionTable.COLUMN_ADD, permission.canAdd);
        values.put(WritePermissionTable.COLUMN_EDIT, permission.canEdit);
        values.put(WritePermissionTable.COLUMN_DEL, permission.canDelete);
        values.put(WritePermissionTable.COLUMN_MAX_DAILY_ACTIONS, permission.maxDailyActions);
        values.put(WritePermissionTable.COLUMN_WAIT_TIME, permission.minSecondsBetweenActions);
        values.put(WritePermissionTable.COLUMN_SITE, site.apiSiteParameter);
        values.put(WritePermissionTable.COLUMN_SITE_URL, site.link);
        if (permission.objectType != null)
            values.put(WritePermissionTable.COLUMN_OBJECT_TYPE, permission.objectType.getValue());
        return values;
    }

    public void update(long id, Site site, WritePermission writePermission)
    {
        String whereClause = WritePermissionTable.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };

        database.update(TABLE_NAME, getContentValues(site, writePermission), whereClause, whereArgs);
    }

    public WritePermission getPermission(String site, ObjectType objectType)
    {
        if (site == null || objectType == null)
            return null;

        String selection = WritePermissionTable.COLUMN_SITE + " = ? and" + WritePermissionTable.COLUMN_OBJECT_TYPE
                        + " = ?";
        String[] selectionArgs = { site, objectType.getValue() };

        Cursor cursor = database.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
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

        Cursor cursor = database.query(TABLE_NAME, cols, selection, selectionArgs, null, null, null);
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

    public HashMap<ObjectType, WritePermission> getPermissions(String site)
    {
        if (site == null)
            return null;

        String selection = WritePermissionTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { site };

        Cursor cursor = database.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Permissions retrieved from DB for " + site);

        return getPermissions(cursor);
    }

    private HashMap<ObjectType, WritePermission> getPermissions(Cursor cursor)
    {
        HashMap<ObjectType, WritePermission> permissions = new HashMap<ObjectType, WritePermission>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            WritePermission permission = getPermission(cursor);
            if (permission.objectType != null)
                permissions.put(permission.objectType, permission);
            cursor.moveToNext();
        }

        return permissions;
    }

    private WritePermission getPermission(Cursor cursor)
    {
        WritePermission permission = new WritePermission();
        permission.id = cursor.getLong(cursor.getColumnIndex(WritePermissionTable.COLUMN_ID));
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

    public void delete(ArrayList<Account> deletedAccounts)
    {
        if (deletedAccounts != null)
        {
            for (Account account : deletedAccounts)
            {
                Log.d(TAG, "Deleting write permission for " + account.siteUrl);
                String whereClause = WritePermissionTable.COLUMN_SITE_URL + " = ?";
                String[] whereArgs = new String[] { account.siteUrl };
                database.delete(TABLE_NAME, whereClause, whereArgs);
            }
        }
    }

    public void deleteAll()
    {
        database.delete(TABLE_NAME, null, null);
    }

    public static void update(Context context, long id, Site site, WritePermission writePermission)
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);
        try
        {
            writePermissionDAO.open();
            writePermissionDAO.update(id, site, writePermission);
        }
        catch (SQLException e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            writePermissionDAO.close();
        }

    }

    public static HashMap<ObjectType, WritePermission> getPermissions(Context context, String site)
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);
        try
        {
            writePermissionDAO.open();
            return writePermissionDAO.getPermissions(site);
        }
        catch (SQLException e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            writePermissionDAO.close();
        }

        return null;
    }

    public static void deleteAll(Context context)
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);
        try
        {
            writePermissionDAO.open();
            writePermissionDAO.deleteAll();
        }
        finally
        {
            writePermissionDAO.close();
        }

    }

    public static void delete(final Context context, final ArrayList<Account> deletedAccounts)
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);
        try
        {
            writePermissionDAO.open();
            writePermissionDAO.delete(deletedAccounts);
        }
        catch (SQLException e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            writePermissionDAO.close();
        }
    }

}

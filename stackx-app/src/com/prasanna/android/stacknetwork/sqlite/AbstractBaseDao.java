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
import android.database.sqlite.SQLiteDatabase;

import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.AuditTable;

public abstract class AbstractBaseDao {
    private final DatabaseHelper databaseHelper;
    protected SQLiteDatabase database;

    public AbstractBaseDao(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    protected SQLiteDatabase getDatabase() {
        return database;
    }

    protected DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public boolean isOpen() {
        return database != null && database.isOpen();
    }

    public void open() throws SQLException {
        if (!isOpen())
            database = getDatabaseHelper().getWritableDatabase();
    }

    public void openReadOnly() throws SQLException {
        database = getDatabaseHelper().getReadableDatabase();
    }

    public void close() {
        getDatabaseHelper().close();
    }

    protected void insertAuditEntry(String type, String site) {
        ContentValues values = new ContentValues();
        if (site != null)
            values.put(AuditTable.COLUMN_SITE, site);
        values.put(AuditTable.COLUMN_TYPE, type);
        values.put(AuditTable.COLUMN_LAST_UPDATE_TIME, System.currentTimeMillis());
        database.insert(DatabaseHelper.TABLE_AUDIT, null, values);
    }

    public void updateAuditEntry(String type, String site) {
        String whereClause = AuditTable.COLUMN_TYPE + "= ?";
        String[] whereArgs = new String[] { type };

        ContentValues values = new ContentValues();
        if (site != null)
            values.put(AuditTable.COLUMN_SITE, site);
        values.put(AuditTable.COLUMN_TYPE, type);
        values.put(AuditTable.COLUMN_LAST_UPDATE_TIME, System.currentTimeMillis());
        database.update(DatabaseHelper.TABLE_AUDIT, values, whereClause, whereArgs);
    }

    protected void deleteAuditEntry(String type, String site) {
        ArrayList<String> whereArgs = new ArrayList<String>();
        whereArgs.add(type);

        String whereClause = AuditTable.COLUMN_TYPE + " = ?";
        if (site != null) {
            whereClause += " and " + AuditTable.COLUMN_SITE + " = ?";
            whereArgs.add(site);
        }

        database.delete(DatabaseHelper.TABLE_AUDIT, whereClause, whereArgs.toArray(new String[whereArgs.size()]));
    }

    protected long getLastUpdateTime(String type, String site) {
        ArrayList<String> selectionArgs = new ArrayList<String>();
        selectionArgs.add(type);

        String[] cols = new String[] { AuditTable.COLUMN_LAST_UPDATE_TIME };
        String selection = AuditTable.COLUMN_TYPE + " = ?";

        if (site != null) {
            selection += " and " + AuditTable.COLUMN_SITE + " = ?";
            selectionArgs.add(site);
        }

        Cursor cursor =
                database.query(DatabaseHelper.TABLE_AUDIT, cols, selection,
                        selectionArgs.toArray(new String[selectionArgs.size()]), null, null, null);

        if (cursor == null || cursor.getCount() == 0)
            return 0L;

        cursor.moveToFirst();

        return cursor.getLong(0);
    }
}

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.prasanna.android.stacknetwork.sqlite.ProfileDAO.ProfileTable;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO.SearchCriteriaTable;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO.SiteTable;
import com.prasanna.android.stacknetwork.sqlite.TagDAO.TagsTable;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO.UserAccountsTable;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO.WritePermissionTable;

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String TABLE_AUDIT = "AUDIT";

    private static final String DATABASE_NAME = "stackx.db";
    private static final int DATABASE_VERSION = 1;

    public static final class AuditTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_LAST_UPDATE_TIME = "last_update_time";

        private static final String CREATE_TABLE = "create table " + TABLE_AUDIT + "(" + COLUMN_ID
                        + " integer primary key autoincrement, " + COLUMN_TYPE + " text not null, "
                        + COLUMN_SITE + " text, " + COLUMN_LAST_UPDATE_TIME + " long not null);";
    }

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SiteTable.CREATE_TABLE);
        db.execSQL(TagsTable.CREATE_TABLE);
        db.execSQL(AuditTable.CREATE_TABLE);
        db.execSQL(WritePermissionTable.CREATE_TABLE);
        db.execSQL(ProfileTable.CREATE_TABLE);
        db.execSQL(UserAccountsTable.CREATE_TABLE);
        db.execSQL(SearchCriteriaTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO Auto-generated method stub
    }
}

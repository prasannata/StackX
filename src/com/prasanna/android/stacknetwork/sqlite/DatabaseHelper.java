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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String TABLE_TAGS = "TAGS";
    public static final String TABLE_MY_TAGS = "MY_TAGS";
    public static final String TABLE_USER_PROFILE = "USER_PROFILE";

    private static final String DATABASE_NAME = "stackx.db";
    private static final int DATABASE_VERSION = 1;

    public static final class TagsTable
    {
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_VALUE = "_value";

	private static final String CREATE_TABLE = "create table " + TABLE_TAGS + "(" + COLUMN_ID
	                + " integer primary key autoincrement, " + COLUMN_VALUE + ");";
	private static final String CREATE_MY_TABLE = "create table " + TABLE_MY_TAGS + "(" + COLUMN_ID
	                + " integer primary key autoincrement, " + COLUMN_VALUE + ");";
    }

    public DatabaseHelper(Context context)
    {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
	db.execSQL(TagsTable.CREATE_TABLE);
	db.execSQL(TagsTable.CREATE_MY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
	// TODO Auto-generated method stub
    }

}

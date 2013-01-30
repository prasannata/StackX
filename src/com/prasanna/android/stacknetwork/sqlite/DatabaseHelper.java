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

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String TABLE_TAGS = "TAGS";
    public static final String TABLE_AUDIT = "AUDIT";
    public static final String TABLE_USER_PROFILE = "USER_PROFILE";
    public static final String TABLE_WRITE_PERMISSION = "WRITE_PERMISSION";
    public static final String TABLE_PROFILE = "PROFILE";
    public static final String TABLE_USER_ACCOUNTS = "USER_ACCOUNTS";

    private static final String DATABASE_NAME = "stackx.db";
    private static final int DATABASE_VERSION = 1;

    public static final class TagsTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_LOCAL_ADD = "local_add";

        private static final String CREATE_TABLE = "create table " + TABLE_TAGS + "(" + COLUMN_ID
                + " integer primary key autoincrement, " + COLUMN_VALUE + " text not null, " + COLUMN_SITE
                + " text not null, " + COLUMN_LOCAL_ADD + " integer DEFAULT 0);";
    }

    public static final class AuditTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_LAST_UPDATE_TIME = "last_update_time";

        private static final String CREATE_TABLE = "create table " + TABLE_AUDIT + "(" + COLUMN_ID
                + " integer primary key autoincrement, " + COLUMN_TYPE + " text, " + COLUMN_SITE + " text, "
                + COLUMN_LAST_UPDATE_TIME + " long not null);";
    }

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

        private static final String CREATE_TABLE = "create table " + TABLE_WRITE_PERMISSION + "(" + COLUMN_ID
                + " integer primary key autoincrement, " + COLUMN_ADD + " integer not null, " + COLUMN_DEL
                + " integer not null, " + COLUMN_EDIT + " integer not null, " + COLUMN_MAX_DAILY_ACTIONS
                + " integer not null, " + COLUMN_WAIT_TIME + " integer not null, " + COLUMN_OBJECT_TYPE
                + " text not null, " + COLUMN_SITE + " text not null, " + COLUMN_SITE_URL + " text not null);";
    }

    public static final class ProfileTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_ME = "me";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_GOLD_BADGES = "gold_badges";
        public static final String COLUMN_SILVER_BADGES = "silver_badges";
        public static final String COLUMN_BRONZE_BADGES = "bronze_badges";
        public static final String COLUMN_QUESTION_COUNT = "q_count";
        public static final String COLUMN_ANSWER_COUNT = "a_count";
        public static final String COLUMN_UPVOTE_COUNT = "u_count";
        public static final String COLUMN_DOWNVOTE_COUNT = "d_count";
        public static final String COLUMN_ACCEPT_RATE = "accept_rate";
        public static final String COLUMN_REPUTATION = "reputation";
        public static final String COLUMN_VIEWS = "views";
        public static final String COLUMN_REG_DATE = "reg_date";
        public static final String COLUMN_LAST_ACCESS = "last_access";
        public static final String COLUMN_PROFILE_IMAGE = "profile_image";
        public static final String COLUMN_LAST_UPDATE = "last_update";

        private static final String CREATE_TABLE = "create table " + TABLE_PROFILE + "(" + COLUMN_ID
                + " integer primary key autoincrement, " + COLUMN_SITE + " text not null, " + COLUMN_ME
                + " integer DEFAULT 0, " + COLUMN_DISPLAY_NAME + " text not null, " + COLUMN_GOLD_BADGES
                + " int not null, " + COLUMN_SILVER_BADGES + " int not null, " + COLUMN_BRONZE_BADGES
                + " int not null, " + COLUMN_QUESTION_COUNT + " integer not null, " + COLUMN_ANSWER_COUNT
                + " integer not null, " + COLUMN_UPVOTE_COUNT + " integer not null, " + COLUMN_DOWNVOTE_COUNT
                + " integer not null, " + COLUMN_ACCEPT_RATE + " integer not null, " + COLUMN_REPUTATION
                + " integer not null, " + COLUMN_VIEWS + " integer not null, " + COLUMN_REG_DATE + " long not null, "
                + COLUMN_LAST_ACCESS + " long not null, " + COLUMN_PROFILE_IMAGE + " BLOB, " + COLUMN_LAST_UPDATE
                + " long not null);";

    }

    public static final class UserAccountsTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String COLUMN_SITE_NAME = "site_name";
        public static final String COLUMN_SITE_URL = "site_url";
        public static final String COLUMN_USER_TYPE = "user_type";

        private static final String CREATE_TABLE = "create table " + TABLE_USER_ACCOUNTS + "(" + COLUMN_ID
                + " integer primary key autoincrement, " + COLUMN_USER_ID + " integer not null, " + COLUMN_ACCOUNT_ID
                + " integer not null, " + COLUMN_SITE_NAME + " text not null, " + COLUMN_SITE_URL + " text not null, "
                + COLUMN_USER_TYPE + " text);";
    }

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TagsTable.CREATE_TABLE);
        db.execSQL(AuditTable.CREATE_TABLE);
        db.execSQL(WritePermissionTable.CREATE_TABLE);
        db.execSQL(ProfileTable.CREATE_TABLE);
        db.execSQL(UserAccountsTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO Auto-generated method stub
    }

}

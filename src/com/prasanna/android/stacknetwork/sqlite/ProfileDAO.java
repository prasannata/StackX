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

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.ProfileTable;

public class ProfileDAO extends AbstractBaseDao
{
    private static final String TAG = ProfileDAO.class.getSimpleName();

    public ProfileDAO(Context context)
    {
        super(context);
    }

    public void insert(String site, User user, boolean me)
    {
        if (site != null && user != null)
        {
            Log.d(TAG, "Inserting user " + user.id + " to db");

            ContentValues values = new ContentValues();
            values.put(ProfileTable.COLUMN_ID, user.id);
            values.put(ProfileTable.COLUMN_SITE, site);
            values.put(ProfileTable.COLUMN_ME, me);
            values.put(ProfileTable.COLUMN_DISPLAY_NAME, user.displayName);
            if (user.badgeCounts != null && user.badgeCounts.length == 3)
            {
                values.put(ProfileTable.COLUMN_GOLD_BADGES, user.badgeCounts[0]);
                values.put(ProfileTable.COLUMN_SILVER_BADGES, user.badgeCounts[1]);
                values.put(ProfileTable.COLUMN_BRONZE_BADGES, user.badgeCounts[2]);
            }
            values.put(ProfileTable.COLUMN_QUESTION_COUNT, user.questionCount);
            values.put(ProfileTable.COLUMN_ANSWER_COUNT, user.answerCount);
            values.put(ProfileTable.COLUMN_UPVOTE_COUNT, user.upvoteCount);
            values.put(ProfileTable.COLUMN_DOWNVOTE_COUNT, user.downvoteCount);
            values.put(ProfileTable.COLUMN_REPUTATION, user.reputation);
            values.put(ProfileTable.COLUMN_VIEWS, user.profileViews);
            values.put(ProfileTable.COLUMN_ACCEPT_RATE, user.acceptRate);
            values.put(ProfileTable.COLUMN_REG_DATE, user.creationDate);
            values.put(ProfileTable.COLUMN_LAST_ACCESS, user.lastAccessTime);

            if (user.avatar != null)
            {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                user.avatar.compress(Bitmap.CompressFormat.PNG, 100, stream);
                values.put(ProfileTable.COLUMN_PROFILE_IMAGE, stream.toByteArray());
            }

            values.put(ProfileTable.COLUMN_LAST_UPDATE, System.currentTimeMillis());

            database.insert(DatabaseHelper.TABLE_PROFILE, null, values);
        }
    }

    public User getMe(String site)
    {
        String selection = ProfileTable.COLUMN_ME + " = ? and " + ProfileTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { "1", site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_PROFILE, null, selection, selectionArgs, null, null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "Me retrieved from DB");

        return getUser(cursor);
    }

    public User getProfile(long userId, String site)
    {
        String selection = ProfileTable.COLUMN_ID + " = ? and " + ProfileTable.COLUMN_SITE + " = ?";
        String[] selectionArgs = { String.valueOf(userId), site };

        Cursor cursor = database.query(DatabaseHelper.TABLE_PROFILE, null, selection, selectionArgs, null, null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        Log.d(TAG, "User retrieved from DB");

        return getUser(cursor);
    }

    private User getUser(Cursor cursor)
    {
        cursor.moveToFirst();

        User user = new User();
        user.id = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_ID));
        user.displayName = cursor.getString(cursor.getColumnIndex(ProfileTable.COLUMN_DISPLAY_NAME));
        user.badgeCounts = new int[3];
        user.badgeCounts[0] = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_GOLD_BADGES));
        user.badgeCounts[1] = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_SILVER_BADGES));
        user.badgeCounts[2] = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_BRONZE_BADGES));
        user.questionCount = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_QUESTION_COUNT));
        user.answerCount = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_ANSWER_COUNT));
        user.upvoteCount = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_UPVOTE_COUNT));
        user.downvoteCount = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_DOWNVOTE_COUNT));
        user.acceptRate = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_ACCEPT_RATE));
        user.reputation = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_REPUTATION));
        user.profileViews = cursor.getInt(cursor.getColumnIndex(ProfileTable.COLUMN_VIEWS));
        user.creationDate = cursor.getLong(cursor.getColumnIndex(ProfileTable.COLUMN_REG_DATE));
        user.lastAccessTime = cursor.getLong(cursor.getColumnIndex(ProfileTable.COLUMN_LAST_ACCESS));
        byte[] image = cursor.getBlob(cursor.getColumnIndex(ProfileTable.COLUMN_PROFILE_IMAGE));
        if (image != null)
            user.avatar = BitmapFactory.decodeByteArray(image, 0, image.length);
        user.lastUpdateTime = cursor.getLong(cursor.getColumnIndex(ProfileTable.COLUMN_LAST_UPDATE));
        return user;
    }

    public void deleteAll()
    {
        database.delete(DatabaseHelper.TABLE_PROFILE, null, null);
    }

    public void deleteMe(String site)
    {
        String whereClause = ProfileTable.COLUMN_ME + " = ? and " + ProfileTable.COLUMN_SITE + " = ?";
        String[] whereArgs = { "1", site };

        database.delete(DatabaseHelper.TABLE_PROFILE, whereClause, whereArgs);
    }

    public void deleteUser(long userId, String site)
    {
        String whereClause = ProfileTable.COLUMN_ID + " = ? and " + ProfileTable.COLUMN_SITE + " = ?";
        String[] whereArgs = { String.valueOf(userId), site };

        database.delete(DatabaseHelper.TABLE_PROFILE, whereClause, whereArgs);
    }
}

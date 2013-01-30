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
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.AuditTable;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.UserAccountsTable;

public class UserAccountsDao extends AbstractBaseDao
{
    private static final String TAG = UserAccountsDao.class.getSimpleName();
    public static final String AUDIT_ENTRY_TYPE = UserAccountsDao.class.getSimpleName();

    public UserAccountsDao(Context context)
    {
        super(context);
    }

    public void insert(ArrayList<Account> accounts)
    {
        if (accounts != null && !accounts.isEmpty())
        {
            Log.d(TAG, "Inserting accounts into DB for user");

            for (Account account : accounts)
            {
                ContentValues values = new ContentValues();
                values.put(UserAccountsTable.COLUMN_USER_ID, account.userId);
                values.put(UserAccountsTable.COLUMN_ACCOUNT_ID, account.id);
                values.put(UserAccountsTable.COLUMN_SITE_NAME, account.siteName);
                values.put(UserAccountsTable.COLUMN_SITE_URL, account.siteUrl);

                if (account.userType != null)
                    values.put(UserAccountsTable.COLUMN_USER_TYPE, account.userType.getValue());

                database.insert(DatabaseHelper.TABLE_USER_ACCOUNTS, null, values);
            }

            insertAuditEntry();
        }
    }

    private void insertAuditEntry()
    {
        ContentValues values = new ContentValues();
        values.put(AuditTable.COLUMN_TYPE, AUDIT_ENTRY_TYPE);
        values.put(AuditTable.COLUMN_LAST_UPDATE_TIME, System.currentTimeMillis());
        Log.d(TAG, "Audit entry for tags: " + values.toString());
        database.insert(DatabaseHelper.TABLE_AUDIT, null, values);
    }

    public long getLastUpdateTime()
    {
        String[] cols = { AuditTable.COLUMN_LAST_UPDATE_TIME };
        String selection = AuditTable.COLUMN_TYPE + " = ?";
        String[] selectionArgs = { AUDIT_ENTRY_TYPE };

        Cursor cursor = database.query(DatabaseHelper.TABLE_AUDIT, cols, selection, selectionArgs, null, null, null);
        if (cursor == null || cursor.getCount() == 0)
            return -1;

        cursor.moveToFirst();
        return cursor.getLong(cursor.getColumnIndex(AuditTable.COLUMN_LAST_UPDATE_TIME));
    }

    public ArrayList<Account> getAccounts(long accountId)
    {
        String selection = UserAccountsTable.COLUMN_ACCOUNT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(accountId) };

        Cursor cursor = database.query(DatabaseHelper.TABLE_USER_ACCOUNTS, null, selection, selectionArgs, null, null,
                        null);
        if (cursor == null || cursor.getCount() == 0)
            return null;

        ArrayList<Account> accounts = new ArrayList<Account>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            accounts.add(getAccount(cursor));
            cursor.moveToNext();
        }

        return accounts;
    }

    private Account getAccount(Cursor cursor)
    {
        Account account = new Account();
        account.id = cursor.getLong(cursor.getColumnIndex(UserAccountsTable.COLUMN_ACCOUNT_ID));
        account.userId = cursor.getLong(cursor.getColumnIndex(UserAccountsTable.COLUMN_USER_ID));
        account.siteName = cursor.getString(cursor.getColumnIndex(UserAccountsTable.COLUMN_SITE_NAME));
        account.siteUrl = cursor.getString(cursor.getColumnIndex(UserAccountsTable.COLUMN_SITE_URL));
        String userType = cursor.getString(cursor.getColumnIndex(UserAccountsTable.COLUMN_USER_TYPE));

        if (userType != null)
            account.userType = UserType.getEnum(userType);
        return account;
    }
}

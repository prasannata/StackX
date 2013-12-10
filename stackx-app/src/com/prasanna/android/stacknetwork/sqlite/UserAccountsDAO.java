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

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.sqlite.DatabaseHelper.AuditTable;
import com.prasanna.android.utils.LogWrapper;

public class UserAccountsDAO extends AbstractBaseDao {
  private static final String TAG = UserAccountsDAO.class.getSimpleName();
  public static final String AUDIT_ENTRY_TYPE = "accounts";
  public static final String TABLE_NAME = "USER_ACCOUNTS";

  public static final class UserAccountsTable {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_SITE_NAME = "site_name";
    public static final String COLUMN_SITE_URL = "site_url";
    public static final String COLUMN_USER_TYPE = "user_type";

    protected static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID
        + " integer primary key autoincrement, " + COLUMN_USER_ID + " integer not null, " + COLUMN_ACCOUNT_ID
        + " integer not null, " + COLUMN_SITE_NAME + " text not null, " + COLUMN_SITE_URL + " text not null, "
        + COLUMN_USER_TYPE + " text);";
  }

  public UserAccountsDAO(Context context) {
    super(context);
  }

  public void insert(ArrayList<Account> accounts) {
    if (accounts != null && !accounts.isEmpty()) {
      LogWrapper.d(TAG, "Inserting accounts into DB for user");

      for (Account account : accounts) {
        ContentValues values = new ContentValues();
        values.put(UserAccountsTable.COLUMN_USER_ID, account.userId);
        values.put(UserAccountsTable.COLUMN_ACCOUNT_ID, account.id);
        values.put(UserAccountsTable.COLUMN_SITE_NAME, account.siteName);
        values.put(UserAccountsTable.COLUMN_SITE_URL, account.siteUrl);

        if (account.userType != null)
          values.put(UserAccountsTable.COLUMN_USER_TYPE, account.userType.getValue());

        database.insert(TABLE_NAME, null, values);
      }

      insertAuditEntry();
    }
  }

  private void insertAuditEntry() {
    ContentValues values = new ContentValues();
    values.put(AuditTable.COLUMN_TYPE, AUDIT_ENTRY_TYPE);
    values.put(AuditTable.COLUMN_LAST_UPDATE_TIME, System.currentTimeMillis());
    LogWrapper.d(TAG, "Audit entry for tags: " + values.toString());
    database.insert(DatabaseHelper.TABLE_AUDIT, null, values);
  }

  public long getLastUpdateTime() {
    String[] cols = { AuditTable.COLUMN_LAST_UPDATE_TIME };
    String selection = AuditTable.COLUMN_TYPE + " = ?";
    String[] selectionArgs = { AUDIT_ENTRY_TYPE };

    Cursor cursor = database.query(DatabaseHelper.TABLE_AUDIT, cols, selection, selectionArgs, null, null, null);
    if (cursor == null || cursor.getCount() == 0)
      return -1;
    try {
      cursor.moveToFirst();
      return cursor.getLong(cursor.getColumnIndex(AuditTable.COLUMN_LAST_UPDATE_TIME));
    }
    finally {
      cursor.close();
    }

  }

  public ArrayList<Account> getAccounts(long accountId) {
    String selection = UserAccountsTable.COLUMN_ACCOUNT_ID + " = ?";
    String[] selectionArgs = { String.valueOf(accountId) };

    Cursor cursor = database.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
    if (cursor == null || cursor.getCount() == 0)
      return null;

    try {
      ArrayList<Account> accounts = new ArrayList<Account>();
      cursor.moveToFirst();
      while (!cursor.isAfterLast()) {
        accounts.add(getAccount(cursor));
        cursor.moveToNext();
      }

      return accounts;
    }
    finally {
      cursor.close();
    }

  }

  private Account getAccount(Cursor cursor) {
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

  public void deleteAll() {
    database.delete(TABLE_NAME, null, null);
  }

  public void deleteList(ArrayList<Account> deletedAccounts) {
    if (deletedAccounts != null) {
      for (Account account : deletedAccounts) {
        LogWrapper.d(TAG, "User account deleted for " + account.siteUrl);

        String whereClause = UserAccountsTable.COLUMN_SITE_URL + " = ?";
        String[] whereArgs = new String[] { account.siteUrl };
        database.delete(TABLE_NAME, whereClause, whereArgs);
      }
    }
  }

  public static void insertAll(Context context, ArrayList<Account> accounts) {
    UserAccountsDAO userAccountsDao = new UserAccountsDAO(context);
    try {
      userAccountsDao.open();
      userAccountsDao.insert(accounts);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      userAccountsDao.close();
    }
  }

  public static ArrayList<Account> get(final Context context, final long id) {
    UserAccountsDAO accountsDAO = new UserAccountsDAO(context);

    try {
      accountsDAO.open();
      return accountsDAO.getAccounts(id);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      accountsDAO.close();
    }

    return null;
  }

  public static void delete(Context context, final ArrayList<Account> deletedAccounts) {
    UserAccountsDAO userAccountsDao = new UserAccountsDAO(context);

    try {
      userAccountsDao.open();
      userAccountsDao.deleteList(deletedAccounts);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      userAccountsDao.close();
    }
  }

}

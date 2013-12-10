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

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.utils.LogWrapper;

public class SiteDAO extends AbstractBaseDao {
  private static final String AUDIT_ENTRY_TYPE = "sites";

  public static final String TABLE_NAME = "MY_SITES";
  private static final String TAG = SiteDAO.class.getSimpleName();
  private Context context;

  public static final class SiteTable {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AUDIENCE = "audience";
    public static final String COLUMN_API_SITE_PARAMTETER = "api_site_parameter";
    public static final String COLUMN_SITE_URL = "site_url";
    public static final String COLUMN_ICON_URL = "icon_url";
    public static final String COLUMN_LOGO_URL = "logo_url";
    public static final String COLUMN_USER_TYPE = "user_type";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_LAST_UPDATE = "last_update";

    protected static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID
        + " integer primary key autoincrement, " + COLUMN_NAME + " text not null, " + COLUMN_API_SITE_PARAMTETER
        + " text not null unique, " + COLUMN_AUDIENCE + " text not null, " + COLUMN_SITE_URL + " text not null, "
        + COLUMN_ICON_URL + " text not null, " + COLUMN_LOGO_URL + " text not null, " + COLUMN_USER_TYPE
        + " text not null, " + COLUMN_USER_ID + " long, " + COLUMN_LAST_UPDATE + " long not null);";

  }

  public SiteDAO(Context context) {
    super(context);
    this.context = context;
  }

  public long insert(Site site) {
    long id = 0L;

    if (site != null) {
      database.beginTransaction();
      try {
        id = getDatabase().insert(TABLE_NAME, null, getContentValues(site));
        insertAuditEntry(AUDIT_ENTRY_TYPE, null);
        getDatabase().setTransactionSuccessful();
      }
      catch (SQLException e) {
        LogWrapper.e(TAG, e.getMessage());
      }
      finally {
        database.endTransaction();
      }
    }

    return id;
  }

  public void insert(ArrayList<Site> sites) {
    if (sites != null) {
      getDatabase().beginTransaction();
      try {
        for (Site site : sites)
          getDatabase().insert(TABLE_NAME, null, getContentValues(site));

        insertAuditEntry(AUDIT_ENTRY_TYPE, null);
        getDatabase().setTransactionSuccessful();
      }
      catch (SQLException e) {
        LogWrapper.e(TAG, e.getMessage());
      }
      finally {
        getDatabase().endTransaction();
      }
    }
  }

  private ContentValues getContentValues(Site site) {
    ContentValues values = new ContentValues();
    values.put(SiteTable.COLUMN_NAME, site.name);
    values.put(SiteTable.COLUMN_AUDIENCE, site.audience);
    values.put(SiteTable.COLUMN_API_SITE_PARAMTETER, site.apiSiteParameter);
    values.put(SiteTable.COLUMN_SITE_URL, site.link);
    values.put(SiteTable.COLUMN_ICON_URL, site.iconUrl);
    values.put(SiteTable.COLUMN_LOGO_URL, site.logoUrl);
    if (site.userType != null)
      values.put(SiteTable.COLUMN_USER_TYPE, site.userType.getValue());

    values.put(SiteTable.COLUMN_USER_ID, site.userId);
    values.put(SiteTable.COLUMN_LAST_UPDATE, System.currentTimeMillis());
    return values;
  }

  public long getLastUpdateTime() {
    return getLastUpdateTime(AUDIT_ENTRY_TYPE, null);
  }

  public ArrayList<Site> getSites() {

    Cursor cursor = getDatabase().query(TABLE_NAME, null, null, null, null, null, null);

    if (cursor == null || cursor.getCount() == 0) {
      LogWrapper.d(TAG, "No entries");
      return null;
    }
    try {
      cursor.moveToFirst();

      ArrayList<Site> registerdSites = new ArrayList<Site>();
      ArrayList<Site> sites = new ArrayList<Site>();
      while (!cursor.isAfterLast()) {
        Site site = getSiteObject(cursor);
        if (site.userType != null && UserType.REGISTERED.equals(site.userType))
          registerdSites.add(site);
        else
          sites.add(site);
        cursor.moveToNext();
      }

      Site defaultSite = AppUtils.getDefaultSite(context);
      if (defaultSite != null && sites.indexOf(defaultSite) > 0)
        sites.remove(defaultSite);

      if (registerdSites.isEmpty()) {
        if (defaultSite != null && !sites.contains(defaultSite))
          sites.add(defaultSite);
        return sites;
      }
      else {
        ArrayList<Site> sitesWithRegisteredFirst = new ArrayList<Site>();
        if (defaultSite != null)
          sitesWithRegisteredFirst.add(defaultSite);

        if (registerdSites.contains(defaultSite))
          registerdSites.remove(defaultSite);

        sitesWithRegisteredFirst.addAll(registerdSites);
        sitesWithRegisteredFirst.addAll(sites);
        return sitesWithRegisteredFirst;
      }

    }
    finally {
      cursor.close();
    }
  }

  public HashMap<String, Site> getLinkSitesMap() {
    Cursor cursor = getDatabase().query(TABLE_NAME, null, null, null, null, null, null);

    if (cursor == null || cursor.getCount() == 0) {
      LogWrapper.d(TAG, "No entries");
      return null;
    }

    try {
      cursor.moveToFirst();

      HashMap<String, Site> sites = new HashMap<String, Site>();
      while (!cursor.isAfterLast()) {
        Site site = getSiteObject(cursor);
        sites.put(site.link, site);
        cursor.moveToNext();
      }
      return sites;
    }
    finally {
      cursor.close();
    }
  }

  private Site getSiteObject(Cursor cursor) {
    Site site = new Site();
    site.dbId = cursor.getLong(cursor.getColumnIndex(SiteTable.COLUMN_ID));
    site.name = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_NAME));
    site.audience = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_AUDIENCE));
    site.apiSiteParameter = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_API_SITE_PARAMTETER));
    site.link = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_SITE_URL));
    site.iconUrl = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_ICON_URL));
    site.logoUrl = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_LOGO_URL));
    if (!cursor.isNull(cursor.getColumnIndex(SiteTable.COLUMN_USER_TYPE))) {
      String userType = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_USER_TYPE));
      site.userType = UserType.getEnum(userType);
    }
    site.userId = cursor.getLong(cursor.getColumnIndex(SiteTable.COLUMN_USER_ID));
    site.writePermissions = getWritePermission(site.apiSiteParameter);
    return site;
  }

  private ArrayList<WritePermission> getWritePermission(String apiSiteParameter) {
    WritePermissionDAO dao = new WritePermissionDAO(context);
    try {
      dao.openReadOnly();
      HashMap<ObjectType, WritePermission> permissions = dao.getPermissions(apiSiteParameter);
      if (permissions != null)
        return new ArrayList<WritePermission>(permissions.values());
    }
    finally {
      dao.close();
    }

    return null;
  }

  public void deleteAll() {
    getDatabase().delete(TABLE_NAME, null, null);
    deleteAuditEntry(AUDIT_ENTRY_TYPE, null);
  }

  public void update(Site site) {
    String whereClause = SiteTable.COLUMN_API_SITE_PARAMTETER + "= ?";
    String[] whereArgs = new String[] { site.apiSiteParameter };
    getDatabase().update(TABLE_NAME, getContentValues(site), whereClause, whereArgs);
  }

  public void updateRegistrationInfo(ArrayList<Account> newAccounts, boolean allRegistered) {
    for (Account account : newAccounts) {
      String whereClause = SiteTable.COLUMN_SITE_URL + "= ?";
      String[] whereArgs = new String[] { account.siteUrl };

      ContentValues values = new ContentValues();

      if (allRegistered)
        values.put(SiteTable.COLUMN_USER_TYPE, account.userType.getValue());
      else
        values.put(SiteTable.COLUMN_USER_TYPE, "");

      LogWrapper.d(TAG, "Update user type for " + account.siteUrl + " to " + values.get(SiteTable.COLUMN_USER_TYPE));
      getDatabase().update(TABLE_NAME, values, whereClause, whereArgs);
    }
  }

  public boolean isRegistered(String site) {
    String[] cols = new String[] { SiteTable.COLUMN_API_SITE_PARAMTETER };
    String selection = SiteTable.COLUMN_API_SITE_PARAMTETER + " = ? and " + SiteTable.COLUMN_USER_TYPE + " = ?";
    String[] selectionArgs = { site, UserType.REGISTERED.getValue() };

    Cursor cursor = getDatabase().query(TABLE_NAME, cols, selection, selectionArgs, null, null, null);

    try {
      return cursor != null && cursor.getCount() > 0;
    }
    finally {
      cursor.close();
    }
  }

  public static void insert(Context context, Site site) {
    SiteDAO dao = new SiteDAO(context);

    try {
      dao.open();
      dao.insert(site);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      dao.close();
    }
  }

  public static void insertAll(Context context, ArrayList<Site> sites) {
    SiteDAO dao = new SiteDAO(context);

    try {
      dao.open();
      dao.insert(sites);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      dao.close();
    }
  }

  public static void updateSites(final Context context, final ArrayList<Account> accounts, final boolean allRegistered) {
    SiteDAO siteDAO = new SiteDAO(context);
    try {
      siteDAO.open();
      siteDAO.updateRegistrationInfo(accounts, allRegistered);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      siteDAO.close();
    }
  }

  public static HashMap<String, Site> getAll(final Context context) {
    SiteDAO siteDAO = new SiteDAO(context);

    try {
      siteDAO.open();
      return siteDAO.getLinkSitesMap();
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      siteDAO.close();
    }

    return null;
  }

  public static ArrayList<Site> getSiteList(final Context context) {
    SiteDAO siteDAO = new SiteDAO(context);

    try {
      siteDAO.open();
      return siteDAO.getSites();
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      siteDAO.close();
    }

    return null;
  }

  public static long getLastUpdateTime(final Context context) {
    SiteDAO siteDAO = new SiteDAO(context);

    try {
      siteDAO.open();
      return siteDAO.getLastUpdateTime();
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      siteDAO.close();
    }

    return 0L;
  }

  public static boolean isRegisteredForSite(final Context context, final String site) {
    SiteDAO siteDAO = new SiteDAO(context);

    try {
      siteDAO.open();
      return siteDAO.isRegistered(site);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      siteDAO.close();
    }

    return false;
  }

  public static void deleteAll(Context context) {
    SiteDAO dao = new SiteDAO(context);

    try {
      dao.open();
      dao.deleteAll();
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      dao.close();
    }
  }

  public static void updateLastUpdateTime(Context context) {
    SiteDAO dao = new SiteDAO(context);

    try {
      dao.open();
      dao.updateAuditEntry(AUDIT_ENTRY_TYPE, null);
    }
    catch (SQLException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    finally {
      dao.close();
    }

  }

}

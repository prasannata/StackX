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
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;

public class SiteDAO extends AbstractBaseDao
{
    private static final String AUDIT_ENTRY_TYPE = "sites";

    public static final String TABLE_NAME = "MY_SITES";
    private static final String TAG = SiteDAO.class.getSimpleName();
    private Context context;

    public static final class SiteTable
    {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AUDIENCE = "audience";
        public static final String COLUMN_API_SITE_PARAMTETER = "api_site_parameter";
        public static final String COLUMN_SITE_URL = "site_url";
        public static final String COLUMN_ICON_URL = "icon_url";
        public static final String COLUMN_LOGO_URL = "logo_url";
        public static final String COLUMN_USER_TYPE = "user_type";
        public static final String COLUMN_LAST_UPDATE = "last_update";

        protected static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID
                        + " integer primary key autoincrement, " + COLUMN_NAME + " text not null, "
                        + COLUMN_API_SITE_PARAMTETER + " text not null unique, " + COLUMN_AUDIENCE + " text not null, "
                        + COLUMN_SITE_URL + " text not null, " + COLUMN_ICON_URL + " text not null, " + COLUMN_LOGO_URL
                        + " text not null, " + COLUMN_USER_TYPE + " text not null, " + COLUMN_LAST_UPDATE
                        + " long not null);";

    }

    public SiteDAO(Context context)
    {
        super(context);
        this.context = context;
    }

    public void insert(ArrayList<Site> sites)
    {
        if (sites != null)
        {
            database.beginTransaction();
            try
            {
                for (Site site : sites)
                    database.insert(TABLE_NAME, null, getContentValues(site));

                insertAuditEntry(AUDIT_ENTRY_TYPE, null);
                database.setTransactionSuccessful();
            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                database.endTransaction();
            }
        }
    }

    private ContentValues getContentValues(Site site)
    {
        ContentValues values = new ContentValues();
        values.put(SiteTable.COLUMN_NAME, site.name);
        values.put(SiteTable.COLUMN_AUDIENCE, site.audience);
        values.put(SiteTable.COLUMN_API_SITE_PARAMTETER, site.apiSiteParameter);
        values.put(SiteTable.COLUMN_SITE_URL, site.link);
        values.put(SiteTable.COLUMN_ICON_URL, site.iconUrl);
        values.put(SiteTable.COLUMN_LOGO_URL, site.logoUrl);
        if (site.userType != null)
            values.put(SiteTable.COLUMN_USER_TYPE, site.userType.getValue());
        values.put(SiteTable.COLUMN_LAST_UPDATE, System.currentTimeMillis());
        return values;
    }

    public long getLastUpdateTime()
    {
        return getLastUpdateTime(AUDIT_ENTRY_TYPE, null);
    }

    public ArrayList<Site> getSites()
    {
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() == 0)
        {
            Log.d(TAG, "No entries");
            return null;
        }

        cursor.moveToFirst();

        ArrayList<Site> sites = new ArrayList<Site>();
        while (!cursor.isAfterLast())
        {
            sites.add(getSiteObject(cursor));
            cursor.moveToNext();
        }

        return sites;
    }

    public HashMap<String, Site> getLinkSitesMap()
    {
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() == 0)
        {
            Log.d(TAG, "No entries");
            return null;
        }

        cursor.moveToFirst();

        HashMap<String, Site> sites = new HashMap<String, Site>();
        while (!cursor.isAfterLast())
        {
            Site site = getSiteObject(cursor);
            sites.put(site.link, site);
            cursor.moveToNext();
        }

        return sites;
    }

    private Site getSiteObject(Cursor cursor)
    {
        Site site = new Site();
        site.dbId = cursor.getLong(cursor.getColumnIndex(SiteTable.COLUMN_ID));
        site.name = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_NAME));
        site.apiSiteParameter = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_API_SITE_PARAMTETER));
        site.link = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_SITE_URL));
        site.iconUrl = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_ICON_URL));
        site.logoUrl = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_LOGO_URL));
        if (!cursor.isNull(cursor.getColumnIndex(SiteTable.COLUMN_USER_TYPE)))
        {
            String userType = cursor.getString(cursor.getColumnIndex(SiteTable.COLUMN_USER_TYPE));
            site.userType = UserType.getEnum(userType);
        }
        site.writePermissions = getWritePermission(site.apiSiteParameter);
        return site;
    }

    private ArrayList<WritePermission> getWritePermission(String apiSiteParameter)
    {
        WritePermissionDAO dao = new WritePermissionDAO(context);
        try
        {
            dao.openReadOnly();
            HashMap<ObjectType, WritePermission> permissions = dao.getPermissions(apiSiteParameter);
            if (permissions != null)
                return new ArrayList<WritePermission>(permissions.values());
        }
        finally
        {
            dao.close();
        }

        return null;
    }

    public void deleteAll()
    {
        database.delete(TABLE_NAME, null, null);
        deleteAuditEntry(AUDIT_ENTRY_TYPE, null);
    }

    public static void purge(Context context)
    {
        SiteDAO dao = new SiteDAO(context);

        dao.open();
        try
        {
            dao.deleteAll();
        }
        catch (SQLException e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            dao.close();
        }
    }

    public void update(Site site)
    {
        String whereClause = SiteTable.COLUMN_API_SITE_PARAMTETER + "= ?";
        String[] whereArgs = new String[] { site.apiSiteParameter };
        database.update(TABLE_NAME, getContentValues(site), whereClause, whereArgs);
    }

    public void updateRegistrationInfo(ArrayList<Account> newAccounts)
    {
        for (Account account : newAccounts)
        {
            String whereClause = SiteTable.COLUMN_SITE_URL + "= ?";
            String[] whereArgs = new String[] { account.siteUrl };

            ContentValues values = new ContentValues();

            if (account.userType != null)
                values.put(SiteTable.COLUMN_USER_TYPE, account.userType.getValue());
            else
                values.put(SiteTable.COLUMN_USER_TYPE, (String) null);

            database.update(TABLE_NAME, values, whereClause, whereArgs);
        }
    }
}

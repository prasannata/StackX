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

package com.prasanna.android.stacknetwork.utils;

import java.util.HashMap;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;

public class WritePermissionUtil
{
    private static final String TAG = WritePermissionUtil.class.getSimpleName();

    public static boolean canAdd(Context context, String site, ObjectType objectType)
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(context);
        try
        {
            // Only check if add comment is available
            writePermissionDAO.open();
            HashMap<ObjectType, WritePermission> writePermissions = writePermissionDAO.getPermissions(OperatingSite
                            .getSite().apiSiteParameter);
            if (writePermissions != null)
            {
                WritePermission writePermission = writePermissions.get(ObjectType.COMMENT);
                return (writePermission != null && writePermission.canAdd);
            }
        }
        catch (SQLException e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            writePermissionDAO.close();
        }

        return false;
    }
}

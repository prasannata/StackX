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

package com.prasanna.android.utils;

import android.util.Log;

import com.prasanna.android.stacknetwork.utils.AppUtils;

public class LogWrapper
{

    public static void d(String tag, String msg)
    {
        if (AppUtils.DEBUG)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg)
    {
        if (AppUtils.DEBUG)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg)
    {
        if (AppUtils.DEBUG)
            Log.v(tag, msg);
    }

    public static void w(String tag, String msg)
    {
        if (AppUtils.DEBUG)
            Log.w(tag, msg);
    }

}

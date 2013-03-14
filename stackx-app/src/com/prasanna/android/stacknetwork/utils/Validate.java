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

public class Validate
{
    private static final String DEFAULT_FAIL_MESSAGE = "Validation failed";

    private static String getMessage(String message)
    {
        if (message != null)
            return message;

        return DEFAULT_FAIL_MESSAGE;
    }

    public static void notNull(Object object, String message)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    public static void notNull(Object... objects)
    {
        if (objects == null)
        {
            throw new IllegalArgumentException(getMessage(DEFAULT_FAIL_MESSAGE));
        }

        for (int i = 0; i < objects.length; i++)
        {
            notNull(objects[i], "Arg " + i + " is null");
        }
    }
    
    public static boolean isEmptyString(String string)
    {
        return string == null || string.equals("");
    }
    

}

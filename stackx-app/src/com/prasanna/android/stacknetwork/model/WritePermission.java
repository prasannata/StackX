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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class WritePermission implements Serializable, Comparable<WritePermission>
{
    private static final long serialVersionUID = -1311818798901923216L;

    public static final String PREF_LAST_COMMENT_WRITE = "pref_last_comment_write";
    public static final String PREF_SECS_BETWEEN_QUESTION_WRITE = "pref_secs_between_question_write";
    public static final String PREF_SECS_BETWEEN_ANSWER_WRITE = "pref_secs_between_answer_write";
    public static final String PREF_SECS_BETWEEN_COMMENT_WRITE = "pref_secs_between_comment_write";

    public enum ObjectType
    {
        ANSWER("answer"),
        COMMENT("comment"),
        QUESTION("question");

        private final String value;

        ObjectType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public static ObjectType getEnum(String value)
        {
            if (value == null)
                return null;

            try
            {
                return valueOf(value.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
            }

            return null;
        }
    }

    public long id;
    public boolean canAdd = false;
    public boolean canDelete = false;
    public boolean canEdit = false;
    public int maxDailyActions = 0;
    public int minSecondsBetweenActions = -1;
    public ObjectType objectType;
    public long userId;
    
    @Override
    public int compareTo(WritePermission another)
    {
        int EQUAL = 0;
        int NOT_EQUAL = -1;
        
        if(another == null)
            return NOT_EQUAL;
        
        if(canAdd != another.canAdd)
            return NOT_EQUAL;
        if(canDelete != another.canDelete)
            return NOT_EQUAL;
        if(canEdit != another.canEdit)
            return NOT_EQUAL;
        if(maxDailyActions != another.maxDailyActions)
            return NOT_EQUAL;
        if(minSecondsBetweenActions != another.minSecondsBetweenActions)
            return NOT_EQUAL;
        
        return EQUAL;
    }
}

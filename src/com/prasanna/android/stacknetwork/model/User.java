/*
    Copyright 2012 Prasanna Thirumalai
    
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
import java.util.ArrayList;

import android.graphics.Bitmap;

public class User implements Serializable
{
    public static enum UserType
    {
        REGISTERED("registered"),
        UNREGISTERED("unregistered"),
        MODERATOR("moderator"),
        DOES_NOT_EXIST("does_not_exist");

        private final String value;

        UserType(String value)
        {
            this.value = value;

        }

        public String getValue()
        {
            return value;
        }

        public static UserType toEnum(String value)
        {
            UserType userType = null;

            if (value != null)
            {
                try
                {
                    for (UserType type : UserType.values())
                    {
                        if (type.getValue().equals(value))
                        {
                            userType = type;
                            break;
                        }
                    }
                }
                catch (IllegalArgumentException e)
                {
                    userType = null;
                }
            }
            return userType;
        }
    }

    private static final long serialVersionUID = -5427063287288616795L;

    public long id;

    public long accountId;

    public String displayName;

    public Bitmap avatar;

    public int reputation;

    public int[] badgeCounts;

    public String profileImageLink;

    public int acceptRate = -1;

    public int questionCount;

    public int answerCount;

    public int upvoteCount;

    public int downvoteCount;

    public int profileViews;

    public ArrayList<Account> accounts;

    public long lastAccessTime;

    public String accessToken;
}

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
import java.util.ArrayList;

import android.graphics.Bitmap;

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public class User extends IdentifiableItem implements Serializable {
  public static enum UserType {
    REGISTERED("registered"),
    UNREGISTERED("unregistered"),
    MODERATOR("moderator"),
    DOES_NOT_EXIST("does_not_exist");

    private final String value;

    UserType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static UserType toEnum(String value) {
      if (value != null) {
        for (UserType type : UserType.values()) {
          if (type.getValue().equals(value)) {
            return type;
          }
        }
      }
      
      return null;
    }

    public static UserType getEnum(String userType) {
      if (userType == null)
        return null;

      try {
        return valueOf(userType.toUpperCase());
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
  }

  public static User copyShallowUser(User that) {
    if (that == null)
      return null;

    User user = new User();
    user.id = that.id;
    user.type = that.type;
    user.acceptRate = that.acceptRate;
    user.displayName = that.displayName;
    user.reputation = that.reputation;
    user.link = that.link;
    return user;
  }

  public static User parseAsSnippet(final JSONObjectWrapper jsonObjectWrapper) {
    User user = null;

    if (jsonObjectWrapper != null) {
      user = new User();
      user.id = jsonObjectWrapper.getLong(JsonFields.User.USER_ID);
      user.type = UserType.toEnum(jsonObjectWrapper.getString(JsonFields.User.USER_TYPE));
      user.displayName = jsonObjectWrapper.getString(JsonFields.User.DISPLAY_NAME);
      user.reputation = jsonObjectWrapper.getInt(JsonFields.User.REPUTATION);
      user.profileImageLink = jsonObjectWrapper.getString(JsonFields.User.PROFILE_IMAGE);
      user.acceptRate = jsonObjectWrapper.getInt(JsonFields.User.ACCEPT_RATE);
    }

    return user;
  }

  private static final long serialVersionUID = -5427063287288616795L;

  public long accountId = 0L;

  public String displayName = "";

  public UserType type = null;

  public Bitmap avatar;

  public String profileImageLink;

  public int reputation = -1;

  public int[] badgeCounts;

  public int acceptRate = 0;

  public int questionCount = 0;

  public int answerCount = 0;

  public int upvoteCount = 0;

  public int downvoteCount = 0;

  public int profileViews = 0;

  public long lastAccessTime = 0L;

  public ArrayList<Account> accounts;

  public long lastUpdateTime;
  
  public String getDisplayName() {
    return displayName == null ? "Unknown" : displayName;
  }
  
  public boolean isRegistered() {
    return type != null && (type.equals(UserType.REGISTERED) || type.equals(UserType.MODERATOR));
  }
}

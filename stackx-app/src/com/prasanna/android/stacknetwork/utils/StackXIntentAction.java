/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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

public class StackXIntentAction {
  public interface IntentAction {
    String getAction();
  }

  public enum UserIntentAction implements IntentAction {
    LOGOUT("com.prasanna.stacknetwork.logout"),
    NEW_MSG("com.prasanna.stacknetwork.newMsg"),
    TOTAL_NEW_MSGS("com.prasanna.stacknetwork.newMsgTotal");

    private final String action;

    private UserIntentAction(String action) {
      this.action = action;
    }

    public String getAction() {
      return action;
    }
  }

  public enum ErrorIntentAction implements IntentAction {
    HTTP_ERROR("com.prasanna.stacknetwork.http.error");

    private final String action;

    private ErrorIntentAction(String action) {
      this.action = action;
    }

    @Override
    public String getAction() {
      return action;
    }
  }
}
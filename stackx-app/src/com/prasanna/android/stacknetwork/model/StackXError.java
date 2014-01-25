/*
 Copyright (C) 2014 Prasanna Thirumalai

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

import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class StackXError implements Serializable {
  private static final String TAG = StackXError.class.getSimpleName();

  private static final long serialVersionUID = -8292786331139417559L;

  public int statusCode;

  public int id;

  public String name = "Failed";

  public String msg = "Server failed without a valid error";

  public static StackXError deserialize(String jsonText) {
    StackXError error = new StackXError();

    try {
      JSONObject response = new JSONObject(jsonText);
      error.id = response.getInt(StringConstants.HttpError.ERROR_ID);
      error.name = response.getString(StringConstants.HttpError.ERROR_NAME);
      error.msg = response.getString(StringConstants.HttpError.ERROR_MESSAGE);
    } catch (JSONException e) {
      LogWrapper.e(TAG, "Json parsing failed: " + e.getMessage());
    }

    return error;
  }
}

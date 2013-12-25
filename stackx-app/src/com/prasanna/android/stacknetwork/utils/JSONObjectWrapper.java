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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.utils.LogWrapper;

public class JSONObjectWrapper {
  private static final String TAG = JSONObjectWrapper.class.getSimpleName();
  public static final int ERROR = -1;
  private final JSONObject jsonObject;

  public static JSONObjectWrapper wrap(JSONObject jsonObject) {
    JSONObjectWrapper wrap = null;

    if (jsonObject != null) {
      wrap = new JSONObjectWrapper(jsonObject);
    }

    return wrap;
  }

  public JSONObjectWrapper(JSONObject jsonObject) {
    if (jsonObject == null)
      throw new IllegalArgumentException("Constructor argument cannot be null");

    this.jsonObject = jsonObject;
  }

  public JSONObjectWrapper getJSONObject(String name) {
    if (jsonObject.has(name)) {
      try {
        return new JSONObjectWrapper(jsonObject.getJSONObject(name));
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return null;
  }

  public JSONArray getJSONArray(String name) {
    if (jsonObject.has(name)) {
      try {
        return jsonObject.getJSONArray(name);
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return null;
  }

  public long getLong(String name) {
    if (jsonObject.has(name)) {
      try {
        return jsonObject.getLong(name);
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return ERROR;
  }

  public int getInt(String name, int defaultValue) {
    if (jsonObject.has(name)) {
      try {
        return jsonObject.getInt(name);
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return defaultValue;
  }

  public int getInt(String name) {
    return getInt(name, ERROR);
  }

  public double getDouble(String name) {
    if (jsonObject.has(name)) {
      try {
        return jsonObject.getDouble(name);
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return ERROR;
  }

  public boolean getBoolean(String name) {
    if (jsonObject.has(name)) {
      try {
        return jsonObject.getBoolean(name);
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return false;
  }

  public String getString(String name) {
    if (jsonObject.has(name)) {
      try {
        return jsonObject.getString(name);
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }
    return null;
  }

  public boolean isErrorResponse() {
    return has(StringConstants.ERROR);
  }

  public boolean has(String name) {
    if (jsonObject != null)
      return jsonObject.has(name);

    return false;
  }

  @Override
  public String toString() {
    if (jsonObject != null)
      return jsonObject.toString();

    return null;
  }
}

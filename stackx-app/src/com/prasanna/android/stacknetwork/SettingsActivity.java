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

package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.utils.LogWrapper;

public class SettingsActivity extends Activity {
  private static final String TAG = SettingsActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentManager().beginTransaction().replace(android.R.id.content, getSettingsFragment()).commit();
  }

  private SettingsFragment getSettingsFragment() {
    SettingsFragment settingsFragment = new SettingsFragment();

    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      settingsFragment.setAppVersionName(packageInfo.versionName);
      settingsFragment.setAppVersionCode(packageInfo.versionCode);
    }
    catch (NameNotFoundException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
    return settingsFragment;
  }
}

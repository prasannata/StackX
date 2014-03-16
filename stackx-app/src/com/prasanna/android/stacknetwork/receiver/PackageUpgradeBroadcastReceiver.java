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

package com.prasanna.android.stacknetwork.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.LogoutActivity;
import com.prasanna.android.stacknetwork.utils.AlarmUtils;
import com.prasanna.android.stacknetwork.utils.AppUtils;

public class PackageUpgradeBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(final Context context, final Intent intent) {
    if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())
        && intent.getDataString().contains(context.getPackageName())) {
      if (AppUtils.inAuthenticatedRealm(context)) {
        if (!AlarmUtils.isAccountSyncAlarmSet(context.getApplicationContext())) {
          AlarmUtils.activatePeriodicAccountSync(context.getApplicationContext());
        }
        
        if (!AppUtils.isApi2Dot2UpgradeDone(context)) logoutUser(context);
      }
    }
  }

  private void logoutUser(final Context context) {
    final String api2Dot2Msg =
        "Stack Exchange API 2.2 requires that the user logs out and logs in to provide write_acces to this application. Please login again.";

    Intent logoutIntent = new Intent(context, LogoutActivity.class);
    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(logoutIntent);

    Toast.makeText(context, api2Dot2Msg, Toast.LENGTH_LONG).show();
    AppUtils.setApi2Dot2UpgradeDone(context);
  }
}

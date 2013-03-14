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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogBuilder
{
    public static AlertDialog yesNoDialog(Context context, int msgStringResId, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        return alertDialogBuilder.setMessage(msgStringResId).setPositiveButton(android.R.string.yes, listener)
                        .setNegativeButton(android.R.string.no, listener).create();
    }

    public static AlertDialog okDialog(Context context, int msgStringResId, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        return alertDialogBuilder.setMessage(msgStringResId).setPositiveButton(android.R.string.ok, listener).create();
    }

    public static AlertDialog okDialog(Context context, String msg, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        return alertDialogBuilder.setMessage(msg).setPositiveButton(android.R.string.ok, listener).create();
    }
}

package com.prasanna.android.stacknetwork.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.prasanna.android.stacknetwork.R;

public class DialogBuilder
{
    public static AlertDialog yesNoDialog(Context context, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        return alertDialogBuilder.setMessage(R.string.sureQuestion).setPositiveButton(R.string.yes, listener)
                .setNegativeButton(R.string.no, listener).create();
    }
}

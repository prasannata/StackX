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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.utils.DialogBuilder;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.utils.LogWrapper;

public class LoginActivity extends Activity
{
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static Context context;

    public static Context getAppContext()
    {
        return LoginActivity.context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        if (SharedPreferencesUtil.isFirstRun(getApplicationContext()))
        {
            LogWrapper.d(TAG, "Preparing for first run");

            setContentView(R.layout.main);
            setupLogin();
            setupSkipLogin();
        }
        else
        {
            if (SharedPreferencesUtil.getDefaultSiteName(context) != null)
            {
                LogWrapper.d(TAG, "Launching default site");

                OperatingSite.setSite(SharedPreferencesUtil.getDefaultSite(context));
                Intent intent = new Intent(context, QuestionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else
            {
                LogWrapper.d(TAG, "Launching site list");

                Intent intent = new Intent(context, StackNetworkListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onResume()
    {
        LogWrapper.d(TAG, "onResume");

        super.onResume();
    }

    private void setupLogin()
    {
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                SharedPreferencesUtil.setFirstRunComplete(LoginActivity.this);
                Intent oAuthIntent = new Intent(view.getContext(), OAuthActivity.class);
                startActivity(oAuthIntent);
            }
        });
    }

    private void setupSkipLogin()
    {
        TextView skipLoginTextView = (TextView) findViewById(R.id.skipLogin);
        skipLoginTextView.setOnClickListener(new View.OnClickListener()
        {
            private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (DialogInterface.BUTTON_POSITIVE == which)
                    {
                        Toast.makeText(LoginActivity.this, "Login option is available in settings", Toast.LENGTH_LONG)
                                        .show();
                        SharedPreferencesUtil.setFirstRunComplete(LoginActivity.this);
                        startActivity(new Intent(context, StackNetworkListActivity.class));
                    }
                }
            };

            public void onClick(View view)
            {
                DialogBuilder.yesNoDialog(LoginActivity.this, R.string.noLoginWarn, dialogClickListener).show();
            }
        });
    }
}

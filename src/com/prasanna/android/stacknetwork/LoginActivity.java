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

package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.IntentUtils;

public class LoginActivity extends Activity
{
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
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        if (SharedPreferencesUtil.getAccessToken(getApplicationContext()) == null)
        {
            setContentView(R.layout.main);
            handleLogin();
            handleNoLogin();
        }
        else
        {
            startActivity(IntentUtils.createSiteListIntent(getApplicationContext()));
        }
    }

    private void handleLogin()
    {
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent oAuthIntent = new Intent(view.getContext(), OAuthActivity.class);
                SharedPreferencesUtil.clear(getApplicationContext());
                startActivity(oAuthIntent);
            }
        });
    }

    private void handleNoLogin()
    {
        TextView skipLoginTextView = (TextView) findViewById(R.id.noLogin);
        skipLoginTextView.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View view)
            {
                startActivity(IntentUtils.createSiteListIntent(view.getContext()));
            }
        });
    }
}

/*
    Copyright 2012 Prasanna Thirumalai
    
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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class UserTabsWidget extends TabActivity
{

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.tabs_3);

        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        Intent intent = new Intent().setClass(this, QuestionsActivity.class);
        spec = tabHost.newTabSpec("userHome").setIndicator("Home").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, UserInboxActivity.class);
        spec = tabHost.newTabSpec("userInbox").setIndicator("Inbox").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, UserProfileActivity.class);
        spec = tabHost.newTabSpec("songs").setIndicator("Profile").setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
}

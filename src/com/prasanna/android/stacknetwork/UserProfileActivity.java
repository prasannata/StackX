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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;

import com.prasanna.android.stacknetwork.fragment.UserAnswersFragment;
import com.prasanna.android.stacknetwork.fragment.UserProfileFragment;
import com.prasanna.android.stacknetwork.fragment.UserQuestionsFragment;
import com.prasanna.android.stacknetwork.utils.OperatingSite;

public class UserProfileActivity extends AbstractUserActionBarActivity
{
    public class TabListener implements ActionBar.TabListener
    {
        private final Fragment fragment;

        public TabListener(Fragment fragment)
        {
            this.fragment = fragment;
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            ft.add(R.id.fragmentContainer, fragment, null);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            ft.remove(fragment);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
            onTabUnselected(tab, ft);
            onTabSelected(tab, ft);
        }
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ll_whitebg_vertical);

        setupActionBarTabs();
    }

    private void setupActionBarTabs()
    {
        ActionBar actionBar = getActionBar();
        getActionBar().setTitle(OperatingSite.getSite().name);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab profileTab = actionBar.newTab();
        profileTab.setIcon(R.drawable.person).setTabListener(new TabListener(new UserProfileFragment()));
        actionBar.addTab(profileTab);

        Tab questionsTab = actionBar.newTab();
        questionsTab.setIcon(R.drawable.question_mark).setTabListener(new TabListener(new UserQuestionsFragment()));
        actionBar.addTab(questionsTab);

        Tab answersTab = actionBar.newTab();
        answersTab.setIcon(R.drawable.answers).setTabListener(new TabListener(new UserAnswersFragment()));
        actionBar.addTab(answersTab);
    }

    @Override
    public void refresh()
    {
        // TODO: Find a way to inform the fragment within current selected tab
        // to refresh
    }

    @Override
    public Context getCurrentContext()
    {
        return UserProfileActivity.this;
    }
}

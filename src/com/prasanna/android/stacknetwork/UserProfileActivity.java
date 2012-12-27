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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.prasanna.android.stacknetwork.fragment.UserAnswersFragment;
import com.prasanna.android.stacknetwork.fragment.UserProfileFragment;
import com.prasanna.android.stacknetwork.fragment.UserQuestionsFragment;
import com.viewpagerindicator.TitlePageIndicator;

public class UserProfileActivity extends AbstractUserActionBarActivity
{
    private static final String[] PAGES = { "Profile", "Questions", "Answers" };

    public static class ProfileViewPageAdapter extends FragmentPagerAdapter
    {
	public ProfileViewPageAdapter(FragmentManager fm)
	{
	    super(fm);
	}

	@Override
	public int getCount()
	{
	    return PAGES.length;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
	    return PAGES[position];
	}

	@Override
	public Fragment getItem(int position)
	{
	    switch (position)
	    {
		case 0:
		    return new UserProfileFragment();
		case 1:
		    return new UserQuestionsFragment();
		case 2:
		    return new UserAnswersFragment();

		default:
		    return null;
	    }
	}
    }

    private ProfileViewPageAdapter profileViewPageAdapter;
    private ViewPager viewPager;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.user_profile_pager);

	profileViewPageAdapter = new ProfileViewPageAdapter(getFragmentManager());

	viewPager = (ViewPager) findViewById(R.id.viewPager);
	viewPager.setAdapter(profileViewPageAdapter);

	TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
	indicator.setViewPager(viewPager);
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

    @Override
    protected void onCreateOptionsMenuPostProcess(Menu menu)
    {
	menu.removeItem(R.id.menu_my_profile);
    }
}

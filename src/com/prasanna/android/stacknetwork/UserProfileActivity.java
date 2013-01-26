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
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.prasanna.android.stacknetwork.fragment.ItemListFragment.OnContextItemSelectedListener;
import com.prasanna.android.stacknetwork.fragment.UserAnswerListFragment;
import com.prasanna.android.stacknetwork.fragment.UserProfileFragment;
import com.prasanna.android.stacknetwork.fragment.UserQuestionListFragment;
import com.prasanna.android.stacknetwork.model.StackXItem;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.viewpagerindicator.TitlePageIndicator;

public class UserProfileActivity extends AbstractUserActionBarActivity implements
                OnContextItemSelectedListener<StackXItem>
{
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    private static final String[] PAGES =
    { "Profile", "Questions", "Answers", "Favorites" };

    private ProfileViewPageAdapter profileViewPageAdapter;
    private ViewPager viewPager;

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
                    return UserQuestionListFragment.newFragment(UserIntentService.GET_USER_QUESTIONS);
                case 2:
                    return new UserAnswerListFragment();
                case 3:
                    return UserQuestionListFragment.newFragment(UserIntentService.GET_USER_FAVORITES);

                default:
                    return null;
            }
        }
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_title_indicator);

        profileViewPageAdapter = new ProfileViewPageAdapter(getFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(profileViewPageAdapter);

        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        if (getIntent().getBooleanExtra(StringConstants.ME, false))
            menu.removeItem(R.id.menu_my_profile);

        return ret & true;
    }

    @Override
    public void refresh()
    {
        finish();
        getIntent().putExtra(StringConstants.REFRESH, true);
        startActivity(getIntent());
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item, StackXItem stackXItem)
    {
        if (item.getGroupId() == R.id.qContextMenuGroup)
        {

            Log.d(TAG, "Context item selected: " + item.getTitle());

            switch (item.getItemId())
            {
                case R.id.q_ctx_related:
                    startRelatedQuestionsActivity(stackXItem);
                    return true;
                case R.id.q_ctx_similar:
                    startSimilarQuestionsActivity(stackXItem);
                    return true;
                case R.id.q_ctx_menu_email:
                    Intent emailIntent = IntentUtils.createEmailIntent(stackXItem.title, stackXItem.link);
                    startActivity(Intent.createChooser(emailIntent, ""));
                    return true;
                default:
                    return false;
            }
        }
        else if (item.getGroupId() == R.id.qContextTagsMenuGroup)
        {
            Log.d(TAG, "Tag selected: " + item.getTitle());

            Intent questionsIntent = new Intent(this, QuestionsActivity.class);
            questionsIntent.setAction(StringConstants.TAG);
            questionsIntent.putExtra(StringConstants.TAG, item.getTitle());
            startActivity(questionsIntent);

            return true;
        }

        return false;
    }

    private void startRelatedQuestionsActivity(StackXItem stackXItem)
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.RELATED);
        questionsIntent.putExtra(StringConstants.QUESTION_ID, stackXItem.id);
        startActivity(questionsIntent);
    }

    private void startSimilarQuestionsActivity(StackXItem stackXItem)
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.SIMILAR);
        questionsIntent.putExtra(StringConstants.TITLE, stackXItem.title);
        startActivity(questionsIntent);
    }

}

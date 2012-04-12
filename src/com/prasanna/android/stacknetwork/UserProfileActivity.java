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
        private Fragment fragment;

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
        // TODO Auto-generated method stub
    }

    @Override
    public Context getCurrentAppContext()
    {
        return getApplicationContext();
    }
}

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

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment;
import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment.OnRunSearchListener;
import com.prasanna.android.stacknetwork.fragment.SearchQuestionListFragment;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.utils.LogWrapper;

public class AdvancedSearchActivity extends AbstractUserActionBarActivity implements OnRunSearchListener
{
    private static final String TAG = AdvancedSearchActivity.class.getSimpleName();
    private SearchQuestionListFragment questionListFragment;
    private SearchCriteriaFragment searchCriteriaFragment;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.advanced_search);
        searchCriteriaFragment =
                        (SearchCriteriaFragment) getFragmentManager().findFragmentById(R.id.searchCriteriaFragment);
        questionListFragment =
                        (SearchQuestionListFragment) getFragmentManager().findFragmentById(R.id.questionListFragment);

        if (StringConstants.SEARCH_CRITERIA.equals(getIntent().getAction()))
            searchCriteriaFragment.loadCriteria((SearchCriteriaDomain) getIntent().getSerializableExtra(
                            StringConstants.SEARCH_CRITERIA));
    }

    private void showOrHideQuestionListFragment(Configuration configuration)
    {
        if (!searchCriteriaFragment.isRemoving() && !questionListFragment.isRemoving())
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                if (questionListFragment.hasResults())
                {
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    ft.hide(searchCriteriaFragment);
                    ft.show(questionListFragment);
                    AppUtils.hideSoftInput(this, getWindow().getCurrentFocus());
                }
                else
                {
                    getActionBar().setDisplayHomeAsUpEnabled(false);
                    ft.show(searchCriteriaFragment);
                    ft.hide(questionListFragment);
                }
            }
            else
            {
                getActionBar().setDisplayHomeAsUpEnabled(false);
                ft.show(searchCriteriaFragment);
                if (questionListFragment.hasResults())
                    ft.show(questionListFragment);
                else
                    ft.hide(questionListFragment);
            }

            // Why not commit? While in SearchCriteriaFragment, change
            // orientation to landscape, launch SearchCriteriaListActivity.
            // While in SearchCriteriaListActivity, change orientation back to
            // portrait and press back. IllegalStateException is thrown saying
            // commit cannot be performed after onSaveInstanceState has been
            // called. Hence, commitAllowingStateLoss. But not convinced this is
            // ok.
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean ret = super.onPrepareOptionsMenu(menu);

        menu.removeItem(R.id.menu_refresh);
        menu.removeItem(R.id.menu_advanced_search);

        return ret & true;
    }

    @Override
    protected void onStart()
    {
        LogWrapper.d(TAG, "onStart");

        super.onStart();

        showOrHideQuestionListFragment(getResources().getConfiguration());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        showOrHideQuestionListFragment(newConfig);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.menu_save)
        {
            searchCriteriaFragment.saveCriteria(getSaveCriteriaTaskCompletionNotifier());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AsyncTaskCompletionNotifier<Boolean> getSaveCriteriaTaskCompletionNotifier()
    {
        return new AsyncTaskCompletionNotifier<Boolean>()
        {
            @Override
            public void notifyOnCompletion(Boolean result)
            {
                if (result)
                {
                    setActionBarTitle(searchCriteriaFragment.getCriteriaName());
                    Toast.makeText(AdvancedSearchActivity.this, "Search criteria saved", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(AdvancedSearchActivity.this, "Cannot save criteria", Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    protected void refresh()
    {
        throw new UnsupportedOperationException("Refresh not supported for " + TAG);
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    @Override
    public void onRunSearch(SearchCriteria searchCriteria, boolean savedCriteria)
    {
        runSearchAndShowResults(searchCriteria, false, savedCriteria);
    }

    @Override
    protected boolean onActionBarHomeButtonClick(MenuItem menuItem)
    {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                        && searchCriteriaFragment.isAdded() && !searchCriteriaFragment.isVisible())
        {
            if (!getFragmentManager().popBackStackImmediate())
            {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.show(searchCriteriaFragment);
                ft.hide(questionListFragment);
                ft.commit();
            }

            return true;
        }

        return super.onActionBarHomeButtonClick(menuItem);
    }

    private void runSearchAndShowResults(SearchCriteria searchCriteria, boolean addToBackStack, boolean savedCriteria)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.show(questionListFragment);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            ft.hide(searchCriteriaFragment);
            ft.addToBackStack(null);
        }
        else
            searchCriteriaFragment.updateViewForDualPane(getResources().getConfiguration().orientation);

        ft.commit();

        questionListFragment.search(searchCriteria, savedCriteria);
    }
}

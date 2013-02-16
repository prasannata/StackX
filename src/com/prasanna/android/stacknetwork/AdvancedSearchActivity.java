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
import android.util.Log;
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

public class AdvancedSearchActivity extends AbstractUserActionBarActivity implements OnRunSearchListener
{
    private static final String TAG = AdvancedSearchActivity.class.getSimpleName();
    private boolean viewInitialized = false;
    private SearchQuestionListFragment questionListFragment;
    private SearchCriteriaFragment searchCriteriaFragment;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        if (!viewInitialized)
        {
            Log.d(TAG, "initializing view");
            setContentView(R.layout.advanced_search);
            searchCriteriaFragment = (SearchCriteriaFragment) getFragmentManager().findFragmentById(
                            R.id.searchCriteriaFragment);
            questionListFragment = (SearchQuestionListFragment) getFragmentManager().findFragmentById(
                            R.id.questionListFragment);
            viewInitialized = true;

            if (StringConstants.SEARCH_CRITERIA.equals(getIntent().getAction()))
                searchCriteriaFragment.loadCriteria((SearchCriteriaDomain) getIntent().getSerializableExtra(
                                StringConstants.SEARCH_CRITERIA));
        }

        showOrHideQuestionListFragment(getResources().getConfiguration());
    }

    private void showOrHideQuestionListFragment(Configuration configuration)
    {
        if (questionListFragment != null)
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                if (!questionListFragment.hasResults())
                    ft.hide(questionListFragment);
                else
                {
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    ft.hide(searchCriteriaFragment);
                    AppUtils.hideSoftInput(this, getWindow().getCurrentFocus());
                }
            }
            else
            {
                getActionBar().setDisplayHomeAsUpEnabled(false);
                ft.show(searchCriteriaFragment);
                if (questionListFragment.hasResults())
                    ft.show(questionListFragment);
            }
            ft.commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onPrepareOptionsMenu");

        boolean ret = super.onPrepareOptionsMenu(menu);

        menu.removeItem(R.id.menu_refresh);
        menu.removeItem(R.id.menu_advanced_search);

        return ret & true;
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
            AsyncTaskCompletionNotifier<Boolean> asyncTaskCompletionNotifier = new AsyncTaskCompletionNotifier<Boolean>()
            {
                @Override
                public void notifyOnCompletion(Boolean result)
                {
                    if (result)
                    {
                        getActionBar().setTitle(searchCriteriaFragment.getCriteriaName());
                        Toast.makeText(AdvancedSearchActivity.this, "Search criteria saved", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(AdvancedSearchActivity.this, "Cannot save criteria", Toast.LENGTH_LONG).show();
                }
            };

            searchCriteriaFragment.saveCriteria(asyncTaskCompletionNotifier);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onRunSearch(SearchCriteria searchCriteria)
    {
        runSearchAndShowResults(searchCriteria, false);
    }

    @Override
    protected boolean onActionBarHomeButtonClick(MenuItem menuItem)
    {
        Log.d(TAG, "Home button clicked");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                        && searchCriteriaFragment.isAdded() && !searchCriteriaFragment.isVisible())
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.show(searchCriteriaFragment);
            ft.hide(questionListFragment);
            ft.addToBackStack(null);
            ft.commit();

            return true;
        }

        return super.onActionBarHomeButtonClick(menuItem);
    }

    private void runSearchAndShowResults(SearchCriteria searchCriteria, boolean addToBackStack)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.show(questionListFragment);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            ft.hide(searchCriteriaFragment);
            ft.addToBackStack(null);
        }
        ft.commit();

        questionListFragment.search(searchCriteria);
    }
}

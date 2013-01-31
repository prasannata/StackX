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

import com.prasanna.android.stacknetwork.fragment.ItemListFragment.OnContextItemSelectedListener;
import com.prasanna.android.stacknetwork.fragment.QuestionListFragment;
import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment;
import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment.OnRunSearchListener;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.SearchCriteria;

public class AdvancedSearchActivity extends AbstractUserActionBarActivity implements
                OnContextItemSelectedListener<Question>, OnRunSearchListener
{
    private static final String TAG = AdvancedSearchActivity.class.getSimpleName();
    private boolean viewInitialized = false;
    private QuestionListFragment questionListFragment;
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
            questionListFragment = (QuestionListFragment) getFragmentManager().findFragmentById(
                            R.id.questionListFragment);
            viewInitialized = true;
        }

        /*
         * Can this be better done by handling
         * onConfigurationChanged(newConfig)?
         */
        if (questionListFragment != null)
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                ft.hide(questionListFragment);
            else
                ft.show(questionListFragment);
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        if (menu != null)
            menu.removeItem(R.id.menu_refresh);

        return ret & true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
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
    public boolean onContextItemSelected(MenuItem item, Question stackXItem)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onRunSearch(SearchCriteria searchCriteria)
    {
        if (searchCriteria != null)
            showSearchResults();
    }

    private void showSearchResults()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(searchCriteriaFragment);
        ft.show(questionListFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}

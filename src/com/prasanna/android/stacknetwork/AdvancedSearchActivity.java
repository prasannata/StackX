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

import java.util.ArrayList;
import java.util.Arrays;

import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;

import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.Validate;

public class AdvancedSearchActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = AdvancedSearchActivity.class.getSimpleName();
    private Spinner sortSpinner;
    private ArrayList<String> sortOptionArray;
    private EditText searchQuery;
    private AutoCompleteTextView selectedTag;
    private AutoCompleteTextView noLikeTag;
    private ImageView runCriteria;
    private ImageView clearCriteria;
    private CheckBox answered;
    private TableLayout criteriaLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_criteria_builder);

        criteriaLayout = (TableLayout) findViewById(R.id.criteriaTable);

        searchQuery = (EditText) findViewById(R.id.searchQuery);
        selectedTag = (AutoCompleteTextView) findViewById(R.id.searchSelectedTag);
        selectedTag.setAdapter(getTagArrayAdapter());

        answered = (CheckBox) findViewById(R.id.searchAnswered);

        noLikeTag = (AutoCompleteTextView) findViewById(R.id.searchNoLikeTag);
        noLikeTag.setAdapter(getTagArrayAdapter());

        sortSpinner = (Spinner) findViewById(R.id.searchSortSpinner);
        sortOptionArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.searchSortArray)));
        sortSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, sortOptionArray));

        runCriteria = (ImageView) findViewById(R.id.runCriteria);
        runCriteria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SearchCriteria searchCriteria = SearchCriteria.newCriteria();

                if (searchQuery.getText() != null && !Validate.isEmptyString(searchQuery.getText().toString()))
                    searchCriteria.addTagInclude(searchQuery.getText().toString());
                if (selectedTag.getText() != null && !Validate.isEmptyString(selectedTag.getText().toString()))
                    searchCriteria.addTagInclude(selectedTag.getText().toString());
                if (noLikeTag.getText() != null && !Validate.isEmptyString(noLikeTag.getText().toString()))
                    searchCriteria.addTagExclude(noLikeTag.getText().toString());
                if (answered.isChecked())
                    searchCriteria.mustBeAnswered();
                searchCriteria.sortBy(SearchSort.getEnum((String) sortSpinner.getSelectedItem())).build();

                criteriaLayout.startAnimation(AnimationUtils.loadAnimation(AdvancedSearchActivity.this,
                                android.R.anim.slide_out_right));
                criteriaLayout.setVisibility(View.GONE);
            }
        });

        clearCriteria = (ImageView) findViewById(R.id.clearCriteria);
        clearCriteria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchQuery.setText("");
                selectedTag.setText("");
                noLikeTag.setText("");
                sortSpinner.setSelection(0);
            }
        });
    }

    private ArrayAdapter<String> getTagArrayAdapter()
    {
        TagDAO tagDAO = new TagDAO(this);
        ArrayList<String> tags = null;
        try
        {
            tagDAO.open();
            tags = tagDAO.getTagStringList(OperatingSite.getSite().apiSiteParameter);
        }
        catch (SQLException e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            tagDAO.close();
        }

        if (tags == null)
            tags = new ArrayList<String>();

        return new ArrayAdapter<String>(this, R.layout.spinner_item, tags);
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
    protected void refresh()
    {
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }
}

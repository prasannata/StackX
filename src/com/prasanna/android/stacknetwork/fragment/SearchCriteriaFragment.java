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

package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.Fragment;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.Validate;

public class SearchCriteriaFragment extends Fragment
{
    private static final String TAG = SearchCriteriaFragment.class.getSimpleName();
    private Spinner sortSpinner;
    private ArrayList<String> sortOptionArray;
    private EditText searchQuery;
    private AutoCompleteTextView selectedTag;
    private AutoCompleteTextView noLikeTag;
    private ImageView runCriteria;
    private ImageView clearCriteria;
    private CheckBox answered;
    private ScrollView criteriaLayout;
    private OnRunSearchListener onRunSearchListener;
    private CheckBox hasAnswers;

    public interface OnRunSearchListener
    {
        void onRunSearch(SearchCriteria searchCriteria);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.d(TAG, "onAttach");

        super.onAttach(activity);

        if (!(activity instanceof OnRunSearchListener))
            throw new IllegalArgumentException(activity.getLocalClassName() + " must implement OnRunSearchListener");

        onRunSearchListener = (OnRunSearchListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        criteriaLayout = (ScrollView) inflater.inflate(R.layout.search_criteria_builder, null);

        searchQuery = (EditText) criteriaLayout.findViewById(R.id.searchQuery);
        selectedTag = (AutoCompleteTextView) criteriaLayout.findViewById(R.id.searchSelectedTag);
        selectedTag.setAdapter(getTagArrayAdapter());

        hasAnswers = (CheckBox) criteriaLayout.findViewById(R.id.searchOnlyWithAnswers);
        answered = (CheckBox) criteriaLayout.findViewById(R.id.searchAnswered);

        noLikeTag = (AutoCompleteTextView) criteriaLayout.findViewById(R.id.searchNoLikeTag);
        noLikeTag.setAdapter(getTagArrayAdapter());

        sortSpinner = (Spinner) criteriaLayout.findViewById(R.id.searchSortSpinner);
        clearCriteria = (ImageView) criteriaLayout.findViewById(R.id.clearCriteria);

        return criteriaLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);
        sortOptionArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.searchSortArray)));
        sortSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, sortOptionArray));

        runCriteria = (ImageView) criteriaLayout.findViewById(R.id.runCriteria);
        runCriteria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SearchCriteria searchCriteria = SearchCriteria.newCriteria();

                if (searchQuery.getText() != null && !Validate.isEmptyString(searchQuery.getText().toString()))
                    searchCriteria.setQuery(searchQuery.getText().toString().trim());
                if (selectedTag.getText() != null && !Validate.isEmptyString(selectedTag.getText().toString()))
                    searchCriteria.includeTag(selectedTag.getText().toString().trim());
                if (noLikeTag.getText() != null && !Validate.isEmptyString(noLikeTag.getText().toString()))
                    searchCriteria.excludeTag(noLikeTag.getText().toString().trim());

                Log.d(TAG, "Answered: " + answered.isChecked());

                if (hasAnswers.isChecked())
                    searchCriteria.setMinAnswers(1);

                if (answered.isChecked())
                    searchCriteria.mustBeAnswered();
                searchCriteria.sortBy(SearchSort.getEnum((String) sortSpinner.getSelectedItem())).build();

                onRunSearchListener.onRunSearch(searchCriteria);
            }
        });
        clearCriteria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchQuery.setText("");
                selectedTag.setText("");
                noLikeTag.setText("");
                sortSpinner.setSelection(0);
                answered.setChecked(false);
                hasAnswers.setChecked(false);
            }
        });
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause");

        super.onPause();
    }

    private ArrayAdapter<String> getTagArrayAdapter()
    {
        TagDAO tagDAO = new TagDAO(getActivity());
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

        return new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, tags);
    }

}

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
import java.util.HashSet;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.Validate;

public class SearchCriteriaFragment extends Fragment implements TextWatcher
{
    private static final String TAG = SearchCriteriaFragment.class.getSimpleName();
    private static final String SELECTED_TAGS_LL_PREFIX_TAG = "selectedTags:ll:";
    private static final String UNKNOWN_TAG_TV_PREFIX_TAG = "unknownTag:tv:";

    private Spinner sortSpinner;
    private ArrayList<String> sortOptionArray;
    private EditText searchQuery;
    private AutoCompleteTextView tagEditText;
    private ImageView runSearch;
    private ImageView clearCriteria;
    private ToggleButton addUnknownTagToIncluded;
    private ToggleButton addUnknownTagToExcluded;

    private ScrollView criteriaLayout;
    private OnRunSearchListener onRunSearchListener;
    private RadioGroup includeAnswers;
    private LinearLayout selectedTags;
    private int currentNumRowsOfSelectedTags = 0;
    private HashSet<String> includedTags = new HashSet<String>();;
    private HashSet<String> excludedTags = new HashSet<String>();;

    public interface OnRunSearchListener
    {
        void onRunSearch(SearchCriteria searchCriteria);
    }

    static class TagViewHolder
    {
        TextView selectedTagTextView;
        TextView tagTextView;
        ToggleButton toggleIncludeTag;
        ToggleButton toggleExcludeTag;
    }

    public class TagListAdapter extends ArrayAdapter<String>
    {

        public TagListAdapter(Context context, int resource, int textViewResourceId, ArrayList<String> tags)
        {
            super(context, resource, textViewResourceId, tags);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            final TagViewHolder tagViewHolder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.tag_include_exclude, null);
                tagViewHolder = new TagViewHolder();
                tagViewHolder.selectedTagTextView = getTextViewForTag();
                tagViewHolder.tagTextView = (TextView) convertView.findViewById(R.id.tag);
                tagViewHolder.toggleIncludeTag = (ToggleButton) convertView.findViewById(R.id.toggleIncludeTag);
                tagViewHolder.toggleExcludeTag = (ToggleButton) convertView.findViewById(R.id.toggleExcludeTag);

                convertView.setTag(tagViewHolder);
            }
            else
                tagViewHolder = (TagViewHolder) convertView.getTag();

            final String tag = getItem(position);
            tagViewHolder.tagTextView.setClickable(false);
            tagViewHolder.tagTextView.setText(tag);

            prepareToggleIncludeTag(tagViewHolder, tag);
            prepareToggleExcludeTag(tagViewHolder, tag);

            convertView.setTag(tagViewHolder);
            
            return convertView;
        }

        private void prepareToggleIncludeTag(final TagViewHolder tagViewHolder, final String tag)
        {
            if (includedTags.contains(tag))
                tagViewHolder.toggleIncludeTag.setTextColor(getResources().getColor(R.color.delft));
            else
                tagViewHolder.toggleIncludeTag.setTextColor(getResources().getColor(R.color.lightGrey));

            tagViewHolder.toggleIncludeTag.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    Log.d(TAG, "toggleIncludeTag checked " + isChecked);

                    if (isChecked)
                        buttonView.setTextColor(getResources().getColor(R.color.delft));
                    else
                        buttonView.setTextColor(getResources().getColor(R.color.lightGrey));

                    updateIncludedTags(tagViewHolder.selectedTagTextView, tag, isChecked);
                    
                    if (isChecked)
                        tagViewHolder.toggleExcludeTag.setChecked(false);
                }
            });
        }

        private void prepareToggleExcludeTag(final TagViewHolder tagViewHolder, final String tag)
        {
            if (excludedTags.contains(tag))
                tagViewHolder.toggleExcludeTag.setTextColor(getResources().getColor(R.color.delft));
            else
                tagViewHolder.toggleExcludeTag.setTextColor(getResources().getColor(R.color.lightGrey));

            tagViewHolder.toggleExcludeTag.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    Log.d(TAG, "toggleExcludeTag checked " + isChecked);

                    if (isChecked)
                        buttonView.setTextColor(getResources().getColor(R.color.delft));
                    else
                        buttonView.setTextColor(getResources().getColor(R.color.lightGrey));

                    updateExcludedTags(tagViewHolder.selectedTagTextView, tag, isChecked);
                    
                    if (isChecked)
                    {
                        Log.d(TAG, "toggleIncludeTag  set checked false");
                        tagViewHolder.toggleIncludeTag.setChecked(false);
                    }
                }
            });
        }
    }

    private void addTag(TextView tagTextView, String tag)
    {
        tagTextView.setText(tag);
        
        LinearLayout currentRow = getTagRow(tagTextView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(3, 0, 3, 0);
        currentRow.addView(tagTextView, params);
    }

    private LinearLayout getTagRow(TextView tagTextView)
    {
        if (selectedTags.getVisibility() == View.GONE)
            selectedTags.setVisibility(View.VISIBLE);

        int maxWidth = getResources().getDisplayMetrics().widthPixels - 25;

        LinearLayout currentRow = (LinearLayout) selectedTags.findViewWithTag(SELECTED_TAGS_LL_PREFIX_TAG
                        + currentNumRowsOfSelectedTags);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 2, 0, 2);

        if (currentRow == null)
        {
            currentRow = createNewRowForTags(getActivity(), 3);
            selectedTags.addView(currentRow, layoutParams);
        }
        else
        {
            tagTextView.measure(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            currentRow.measure(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if ((tagTextView.getMeasuredWidth() + currentRow.getMeasuredWidth()) > maxWidth)
            {
                currentNumRowsOfSelectedTags++;
                currentRow = createNewRowForTags(getActivity(), 3);
                selectedTags.addView(currentRow, layoutParams);
            }
        }

        return currentRow;
    }

    private LinearLayout createNewRowForTags(final Context context, int topMargin)
    {
        Log.d(TAG, "Creating new tag row");

        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = topMargin;
        rowLayout.setLayoutParams(layoutParams);
        rowLayout.setTag(SELECTED_TAGS_LL_PREFIX_TAG + currentNumRowsOfSelectedTags);
        return rowLayout;
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
        selectedTags = (LinearLayout) criteriaLayout.findViewById(R.id.selectedTags);
        tagEditText = (AutoCompleteTextView) criteriaLayout.findViewById(R.id.tagEditText);
        tagEditText.addTextChangedListener(this);
        tagEditText.setAdapter(getTagArrayAdapter());

        includeAnswers = (RadioGroup) criteriaLayout.findViewById(R.id.includeAnswers);

        sortSpinner = (Spinner) criteriaLayout.findViewById(R.id.searchSortSpinner);
        clearCriteria = (ImageView) criteriaLayout.findViewById(R.id.clearCriteria);

        prepareAddUnknownTagToIncluded();
        prepareAddUnknownTagToExcluded();

        return criteriaLayout;
    }

    private void prepareAddUnknownTagToIncluded()
    {
        addUnknownTagToIncluded = (ToggleButton) criteriaLayout.findViewById(R.id.toggleIncludeUnknownTag);
        addUnknownTagToIncluded.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String unknownTag = tagEditText.getText().toString();
                TextView textView = getTextViewForAddedTag(unknownTag);
                updateIncludedTags(textView, unknownTag, true);
            }
        });
    }

    private void prepareAddUnknownTagToExcluded()
    {
        addUnknownTagToExcluded = (ToggleButton) criteriaLayout.findViewById(R.id.toggleExcludeUnknownTag);
        addUnknownTagToExcluded.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String unknownTag = tagEditText.getText().toString();
                TextView textView = getTextViewForAddedTag(unknownTag);
                updateExcludedTags(textView, unknownTag, true);
            }
        });
    }

    private void updateIncludedTags(TextView textView, String tag, boolean add)
    {
        if (add)
        {
            if (!includedTags.contains(tag))
            {
                includedTags.add(tag);

                Log.d(TAG, tag + " added to included");
                textView.setBackgroundColor(getResources().getColor(R.color.lichen));
                if (excludedTags.contains(tag))
                    excludedTags.remove(tag);
                else
                    addTag(textView, tag);
            }
            else
                Toast.makeText(getActivity(), tag + " already added", Toast.LENGTH_SHORT).show();
        }
        else
        {
            includedTags.remove(tag);
            
            if (!excludedTags.contains(tag))
            {
                LinearLayout parent = (LinearLayout) textView.getParent();
                parent.removeView(textView);
            }
        }
    }

    private void updateExcludedTags(TextView textView, String tag, boolean add)
    {
        if (add)
        {
            if (!excludedTags.contains(tag))
            {
                excludedTags.add(tag);

                Log.d(TAG, tag + " added to excluded");
                textView.setBackgroundColor(getResources().getColor(R.color.pulp));
                if (includedTags.contains(tag))
                    includedTags.remove(tag);
                else
                    addTag(textView, tag);
            }
            else
                Toast.makeText(getActivity(), tag + " already added", Toast.LENGTH_SHORT).show();
        }
        else
        {
            excludedTags.remove(tag);
            
            if (!includedTags.contains(tag))
            {
                LinearLayout parent = (LinearLayout) textView.getParent();
                parent.removeView(textView);
            }
        }
    }

    private TextView getTextViewForAddedTag(String unknownTag)
    {
        TextView textView = (TextView) criteriaLayout.findViewWithTag(UNKNOWN_TAG_TV_PREFIX_TAG + unknownTag);
        if (textView == null)
        {
            textView = getTextViewForTag();
            textView.setTag(UNKNOWN_TAG_TV_PREFIX_TAG + unknownTag);
        }
        return textView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        getActivity().getActionBar().setTitle(getActivity().getString(R.string.advanced_search));

        sortOptionArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.searchSortArray)));
        sortSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, sortOptionArray));

        prepareRunSearch();
        prepareClearCriteria();
    }

    private void prepareRunSearch()
    {
        runSearch = (ImageView) criteriaLayout.findViewById(R.id.runSearch);
        runSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SearchCriteria searchCriteria = SearchCriteria.newCriteria();

                if (searchQuery.getText() != null && !Validate.isEmptyString(searchQuery.getText().toString()))
                    searchCriteria.setQuery(searchQuery.getText().toString().trim());

                switch (includeAnswers.getCheckedRadioButtonId())
                {
                    case R.id.hasAnswers:
                        searchCriteria.setMinAnswers(1);
                        break;
                    case R.id.isAnswered:
                        searchCriteria.mustBeAnswered();
                        break;
                    default:
                        break;
                }

                searchCriteria.includeTags(includedTags).excludeTags(excludedTags);

                searchCriteria.sortBy(SearchSort.getEnum((String) sortSpinner.getSelectedItem()));
                onRunSearchListener.onRunSearch(searchCriteria.build());
                AppUtils.hideSoftInput(getActivity(), v);
            }
        });
    }

    private void prepareClearCriteria()
    {
        clearCriteria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchQuery.setText("");
                tagEditText.setText("");
                sortSpinner.setSelection(0);
                includeAnswers.clearCheck();
                includedTags.clear();
                excludedTags.clear();
                selectedTags.removeAllViews();
                selectedTags.setVisibility(View.GONE);
                AppUtils.hideSoftInput(getActivity(), v);
            }
        });
    }

    private TagListAdapter getTagArrayAdapter()
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

        return new TagListAdapter(getActivity(), R.layout.tag_include_exclude, R.id.tag, tags);
    }

    @Override
    public void afterTextChanged(Editable s)
    {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        if (s.length() > 2 && !tagEditText.isPerformingCompletion())
        {
            if (addUnknownTagToIncluded.getVisibility() == View.GONE)
                addUnknownTagToIncluded.setVisibility(View.VISIBLE);

            if (addUnknownTagToExcluded.getVisibility() == View.GONE)
                addUnknownTagToExcluded.setVisibility(View.VISIBLE);
        }
        else
        {
            if (addUnknownTagToIncluded.getVisibility() == View.VISIBLE)
                addUnknownTagToIncluded.setVisibility(View.GONE);

            if (addUnknownTagToExcluded.getVisibility() == View.VISIBLE)
                addUnknownTagToExcluded.setVisibility(View.GONE);

        }
    }

    private TextView getTextViewForTag()
    {
        final TextView textView = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.tags_layout, null);
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (textView.getText() != null)
                {
                    tagEditText.setText(textView.getText());
                    tagEditText.setSelection(textView.getText().length());
                }
            }
        });

        return textView;
    }
}

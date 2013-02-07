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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
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
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.SearchCriteriaListActivity;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.Validate;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;

public class SearchCriteriaFragment extends Fragment implements TextWatcher
{
    private static final String TAG = SearchCriteriaFragment.class.getSimpleName();
    private static final String SELECTED_TAGS_LL_PREFIX_TAG = "selectedTags:ll:";
    private static final String SELECTED_TAGS_TV_PREFIX_TAG = "selectedTags:tv:";

    private Spinner sortSpinner;
    private ArrayList<String> sortOptionArray;
    private EditText searchQuery;
    private AutoCompleteTextView tagEditText;
    private ImageView runSearch;
    private ImageView loadCriteria;
    private ImageView clearCriteria;
    private ToggleButton toggleButtonTagToInclude;
    private ToggleButton toggleButtonTagToExclude;
    private ScrollView criteriaLayout;
    private OnRunSearchListener onRunSearchListener;
    private RadioGroup includeAnswers;
    private LinearLayout selectedTags;
    private int currentNumRowsOfSelectedTags = 0;
    private HashSet<String> includedTags = new HashSet<String>();;
    private HashSet<String> excludedTags = new HashSet<String>();;
    private Object tagFilterLock = new Object();
    private ArrayList<String> tags = new ArrayList<String>();
    private TagListAdapter tagArrayAdapter;
    private SearchCriteriaDomain searchCriteriaDomain;

    class GetTagsFromDbAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            if (tagArrayAdapter != null)
            {
                TagDAO tagDao = new TagDAO(getActivity());
                try
                {
                    tagDao.open();
                    tags.addAll(tagDao.getTagStringList(OperatingSite.getSite().apiSiteParameter));
                }
                catch (SQLException e)
                {
                    Log.d(TAG, e.getMessage());
                }
                finally
                {
                    tagDao.close();
                }
            }
            return null;
        }

    }

    public static class WriteCriteriaAsyncTask extends AsyncTask<Long, Void, Boolean>
    {
        private final AsyncTaskCompletionNotifier<Boolean> asyncTaskCompletionNotifier;
        private final SearchCriteriaDomain domain;
        private final Context context;
        private final int action;

        public static final int ACTION_ADD = 1;
        public static final int ACTION_UPDATE = 2;
        public static final int ACTION_DEL = 3;
        public static final int ACTION_DEL_MANY = 4;
        public static final int ACTION_ADD_AS_TAB = 5;
        public static final int ACTION_REMOVE_AS_TAB = 6;

        public WriteCriteriaAsyncTask(Context context, SearchCriteriaDomain domain, int action,
                        AsyncTaskCompletionNotifier<Boolean> asyncTaskCompletionNotifier)
        {
            this.context = context;
            this.domain = domain;
            this.action = action;
            this.asyncTaskCompletionNotifier = asyncTaskCompletionNotifier;
        }

        @Override
        protected Boolean doInBackground(Long... params)
        {
            SearchCriteriaDAO dao = new SearchCriteriaDAO(context);
            try
            {
                dao.open();

                Log.d(TAG, "Saving search criteria");

                switch (action)
                {
                    case ACTION_ADD:
                        dao.insert(domain);
                        return true;
                    case ACTION_UPDATE:
                        dao.update(domain);
                        return true;
                    case ACTION_DEL:
                        if (params != null && params.length > 0)
                            dao.delete(params[0]);
                        return true;
                    case ACTION_DEL_MANY:
                        if (params != null && params.length > 0)
                            dao.deleteAll(params);
                        return true;
                    case ACTION_ADD_AS_TAB:
                        dao.updateCriteriaAsTabbed(domain.id, true);
                        return true;
                    case ACTION_REMOVE_AS_TAB:
                        dao.updateCriteriaAsTabbed(domain.id, false);
                        return true;
                }

            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                dao.close();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (asyncTaskCompletionNotifier != null)
                asyncTaskCompletionNotifier.notifyOnCompletion(result);
        }
    }

    public interface OnRunSearchListener
    {
        void onRunSearch(SearchCriteria searchCriteria);
    }

    public class TagFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults result = new FilterResults();

            if (constraint != null && constraint.length() > 0)
            {
                synchronized (tagFilterLock)
                {
                    ArrayList<String> filteredTags = new ArrayList<String>();

                    for (String tag : tags)
                    {
                        if (tag.startsWith((String) constraint) && !filteredTags.contains(tag))
                            filteredTags.add(tag);
                    }

                    result.count = filteredTags.size();
                    result.values = filteredTags;
                }
            }
            else
            {
                result.count = tags.size();
                result.values = tags;
            }

            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            ArrayList<String> filteredTags = (ArrayList<String>) results.values;

            tagArrayAdapter.notifyDataSetInvalidated();
            tagArrayAdapter.clear();

            if (filteredTags != null)
                tagArrayAdapter.addAll(filteredTags);

            tagArrayAdapter.notifyDataSetChanged();
        }

    }

    public class TagListAdapter extends ArrayAdapter<String>
    {
        private Filter filter;

        public TagListAdapter(Context context, int textViewResourceId, ArrayList<String> tags)
        {
            super(context, textViewResourceId, tags);
        }

        @Override
        public Filter getFilter()
        {
            if (filter == null)
                filter = new TagFilter();

            return filter;
        }
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
        Log.d(TAG, "onCreateView");

        super.onCreate(savedInstanceState);

        criteriaLayout = (ScrollView) inflater.inflate(R.layout.search_criteria_builder, null);

        searchQuery = (EditText) criteriaLayout.findViewById(R.id.searchQuery);
        selectedTags = (LinearLayout) criteriaLayout.findViewById(R.id.selectedTags);
        tagEditText = (AutoCompleteTextView) criteriaLayout.findViewById(R.id.tagEditText);
        tagEditText.addTextChangedListener(this);
        tagEditText.setThreshold(1);
        tagArrayAdapter = new TagListAdapter(getActivity(), R.layout.tag_include_exclude, new ArrayList<String>());
        tagEditText.setAdapter(tagArrayAdapter);

        includeAnswers = (RadioGroup) criteriaLayout.findViewById(R.id.includeAnswers);

        sortSpinner = (Spinner) criteriaLayout.findViewById(R.id.searchSortSpinner);
        runSearch = (ImageView) criteriaLayout.findViewById(R.id.runSearch);
        loadCriteria = (ImageView) criteriaLayout.findViewById(R.id.loadCriteria);
        clearCriteria = (ImageView) criteriaLayout.findViewById(R.id.clearCriteria);

        toggleButtonTagToInclude = (ToggleButton) criteriaLayout.findViewById(R.id.toggleIncludeTag);
        toggleButtonTagToExclude = (ToggleButton) criteriaLayout.findViewById(R.id.toggleExcludeTag);

        prepareToggleButtonTagToInclude();
        prepareTogglBeuttonTagToExclude();

        return criteriaLayout;
    }

    private void prepareToggleButtonTagToInclude()
    {
        toggleButtonTagToInclude.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String unknownTag = tagEditText.getText().toString();

                if (isChecked)
                {
                    buttonView.setBackgroundResource(R.drawable.rounded_border_delft);
                    buttonView.setTextColor(getResources().getColor(R.color.delft));
                }
                else
                {
                    buttonView.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
                    buttonView.setTextColor(getResources().getColor(R.color.lightGrey));
                }

                if (unknownTag != null && unknownTag.length() > 0)
                    updateSelectedTags(unknownTag, true, isChecked);

                if (isChecked && toggleButtonTagToExclude.isChecked())
                    toggleButtonTagToExclude.setChecked(false);
            }
        });
    }

    private void prepareTogglBeuttonTagToExclude()
    {
        toggleButtonTagToExclude.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String unknownTag = tagEditText.getText().toString();

                if (isChecked)
                {
                    buttonView.setBackgroundResource(R.drawable.rounded_border_delft);
                    buttonView.setTextColor(getResources().getColor(R.color.delft));
                }
                else
                {
                    buttonView.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
                    buttonView.setTextColor(getResources().getColor(R.color.lightGrey));
                }

                if (unknownTag != null && unknownTag.length() > 0)
                    updateSelectedTags(unknownTag, false, isChecked);

                if (isChecked && toggleButtonTagToInclude.isChecked())
                    toggleButtonTagToInclude.setChecked(false);

                tagEditText.setText("");
            }
        });
    }

    private void updateSelectedTags(String tag, boolean include, boolean add)
    {
        TextView textView = getTextViewForSelectedTag(tag);

        if (add)
        {
            if (include)
            {
                if (!includedTags.contains(tag))
                {
                    includedTags.add(tag);
                    Log.d(TAG, tag + " added to included");
                    addTagView(textView, R.color.lichen);
                }
            }
            else
            {
                if (!excludedTags.contains(tag))
                {
                    excludedTags.add(tag);
                    Log.d(TAG, tag + " added to excluded");
                    addTagView(textView, R.color.pulp);
                }
            }
        }
        else
        {
            if (include)
            {
                includedTags.remove(tag);
                Log.d(TAG, tag + " removed from included");
                removeTagView(tag, include, textView);
            }
            else
            {
                excludedTags.remove(tag);
                Log.d(TAG, tag + " removed from excluded");
                removeTagView(tag, include, textView);
            }
        }
    }

    private void addTagView(TextView tagTextView, int colorResource)
    {
        tagTextView.setBackgroundColor(getResources().getColor(colorResource));

        LinearLayout currentRow = getTagRow(tagTextView);

        TextView findViewWithTag = (TextView) currentRow.findViewWithTag(tagTextView.getTag());

        if (findViewWithTag == null)
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(3, 0, 3, 0);
            currentRow.addView(tagTextView, params);
        }
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

    private void removeTagView(String tag, boolean include, TextView textView)
    {
        LinearLayout parent = (LinearLayout) textView.getParent();
        boolean addedToOther = include ? excludedTags.contains(tag) : includedTags.contains(tag);
        if (parent != null && !addedToOther)
            parent.removeView(textView);
    }

    private TextView getTextViewForSelectedTag(String tag)
    {
        TextView textView = (TextView) criteriaLayout.findViewWithTag(SELECTED_TAGS_TV_PREFIX_TAG + tag);
        if (textView == null)
        {
            textView = getTextViewForTag();
            textView.setTag(SELECTED_TAGS_TV_PREFIX_TAG + tag);
        }
        textView.setText(tag);
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
        prepareLoadCriteria();
        prepareClearCriteria();

        new GetTagsFromDbAsyncTask().execute();

        if (searchCriteriaDomain != null && searchCriteriaDomain.searchCriteria != null)
            showSavedSearchCriteria();
    }

    private void showSavedSearchCriteria()
    {
        getActivity().getActionBar().setTitle(searchCriteriaDomain.name);

        if (searchCriteriaDomain.searchCriteria.getQuery() != null)
            searchQuery.setText(searchCriteriaDomain.searchCriteria.getQuery());

        if (searchCriteriaDomain.searchCriteria.isAnswered())
            ((RadioButton) includeAnswers.findViewById(R.id.isAnswered)).setChecked(true);
        else
        {
            if (searchCriteriaDomain.searchCriteria.getAnswerCount() > 0)
                ((RadioButton) includeAnswers.findViewById(R.id.hasAnswers)).setChecked(true);
        }

        addTags(searchCriteriaDomain.searchCriteria.getTaggedArray(), R.color.lichen, includedTags);
        addTags(searchCriteriaDomain.searchCriteria.getNotTaggedArray(), R.color.pulp, excludedTags);
    }

    private void addTags(String[] tagArray, int colorResource, HashSet<String> destination)
    {
        if (tagArray != null)
        {
            for (String tag : tagArray)
            {
                TextView textView = getTextViewForSelectedTag(tag);
                addTagView(textView, colorResource);
                destination.add(tag);
            }
        }
    }

    private void prepareRunSearch()
    {
        runSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (searchCriteriaDomain == null)
                {
                    searchCriteriaDomain = new SearchCriteriaDomain();
                    searchCriteriaDomain.searchCriteria = SearchCriteria.newCriteria();
                }

                if (searchQuery.getText() != null && !Validate.isEmptyString(searchQuery.getText().toString()))
                    searchCriteriaDomain.searchCriteria.setQuery(searchQuery.getText().toString().trim());

                switch (includeAnswers.getCheckedRadioButtonId())
                {
                    case R.id.hasAnswers:
                        searchCriteriaDomain.searchCriteria.setMinAnswers(1);
                        break;
                    case R.id.isAnswered:
                        searchCriteriaDomain.searchCriteria.mustBeAnswered();
                        break;
                    default:
                        break;
                }

                searchCriteriaDomain.searchCriteria.includeTags(includedTags).excludeTags(excludedTags);

                searchCriteriaDomain.searchCriteria.sortBy(SearchSort.getEnum((String) sortSpinner.getSelectedItem()));
                searchCriteriaDomain.runCount++;
                searchCriteriaDomain.lastRun = System.currentTimeMillis();
                onRunSearchListener.onRunSearch(searchCriteriaDomain.searchCriteria.build());
                AppUtils.hideSoftInput(getActivity(), v);
            }
        });
    }

    private void prepareLoadCriteria()
    {
        loadCriteria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AppUtils.hideSoftInput(getActivity(), v);
                startActivity(new Intent(getActivity(), SearchCriteriaListActivity.class));
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

    @Override
    public void afterTextChanged(final Editable s)
    {
        if (s == null || s.length() == 0)
        {
            toggleButtonTagToInclude.setChecked(false);
            toggleButtonTagToExclude.setChecked(false);
        }
        else
        {
            tagArrayAdapter.getFilter().filter(s, new FilterListener()
            {
                @Override
                public void onFilterComplete(int count)
                {
                    onFilterCompleteHandler(s, count);
                }
            });
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

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

    private void onFilterCompleteHandler(final Editable s, int count)
    {
        if (includedTags.contains(s.toString()))
            toggleButtonTagToInclude.setChecked(true);
        else
            toggleButtonTagToInclude.setChecked(false);

        if (excludedTags.contains(s.toString()))
            toggleButtonTagToExclude.setChecked(true);
        else
            toggleButtonTagToExclude.setChecked(false);
    }

    public void hideSoftInput()
    {
        AppUtils.hideSoftInput(getActivity(), getActivity().getWindow().getCurrentFocus());
    }

    public void saveCriteria(final AsyncTaskCompletionNotifier<Boolean> asyncTaskCompletionNotifier)
    {
        if (searchCriteriaDomain != null)
        {
            if (searchCriteriaDomain.id > 0)
            {
                new WriteCriteriaAsyncTask(getActivity(), searchCriteriaDomain, WriteCriteriaAsyncTask.ACTION_UPDATE,
                                asyncTaskCompletionNotifier).execute();
            }
            else
            {
                AlertDialog.Builder saveAsDailogBuilder = new AlertDialog.Builder(getActivity());
                saveAsDailogBuilder.setTitle("Save As");

                final EditText input = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                saveAsDailogBuilder.setView(input);

                saveAsDailogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        searchCriteriaDomain.name = input.getText().toString();
                        searchCriteriaDomain.site = OperatingSite.getSite().apiSiteParameter;
                        new WriteCriteriaAsyncTask(getActivity(), searchCriteriaDomain,
                                        WriteCriteriaAsyncTask.ACTION_ADD, asyncTaskCompletionNotifier).execute();
                    }
                });

                saveAsDailogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.dismiss();
                    }
                });

                saveAsDailogBuilder.show();
            }
        }
    }

    public void loadCriteria(SearchCriteriaDomain searchCriteriaDomain)
    {
        this.searchCriteriaDomain = searchCriteriaDomain;
    }
}

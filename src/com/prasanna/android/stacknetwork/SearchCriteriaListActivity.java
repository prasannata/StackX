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
import java.util.HashSet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment;
import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment.WriteCriteriaAsyncTask;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO.Sort;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;

public class SearchCriteriaListActivity extends AbstractUserActionBarActivity
{
    public static final int MAX_SAVED_SEARCHES = 10;
    public static final int MAX_CUSTOM_TABS = 3;

    private static final String TAG = SearchCriteriaListActivity.class.getSimpleName();
    private static final String ACTION_BAR_TITLE = "Saved Searches";

    private Spinner sortSpinner;
    private SearchCriteriaArrayAdapter searchCriteriaArrayAdapter;
    private ListView listView;
    private ArrayList<SearchCriteriaDomain> savedSearchList = new ArrayList<SearchCriteriaDomain>();
    private ArrayList<SearchCriteriaDomain> toDeleteList = new ArrayList<SearchCriteriaDomain>();
    private HashSet<Long> criteriaIdsAsTab = new HashSet<Long>();
    private View emptyItemsView;
    private ArrayList<String> sortOptionArray;

    static class SearchCriteriaViewHolder
    {
        CheckBox delCheckBox;
        ToggleButton addTabToggle;
        RelativeLayout itemLayout;
        TextView itemText;
        TextView itemDetails;
        TextView lastRun;
        TextView ran;
    }

    private class AddDelCriteriaTabAsyncTaskCompletionNotifier implements AsyncTaskCompletionNotifier<Boolean>
    {
        private View view;
        private int action;
        private SearchCriteriaDomain domain;

        public AddDelCriteriaTabAsyncTaskCompletionNotifier(View view, SearchCriteriaDomain domain, int action)
        {
            this.view = view;
            this.domain = domain;
            this.action = action;
        }

        @Override
        public void notifyOnCompletion(Boolean result)
        {
            String toastMsg = domain != null ? domain.name : "";

            switch (action)
            {
                case WriteCriteriaAsyncTask.ACTION_DEL:
                    toastMsg += " deleted";
                    if (result)
                    {
                        if (domain.tab)
                            criteriaIdsAsTab.remove(domain.id);
                        searchCriteriaArrayAdapter.remove(domain);
                    }
                    break;
                case WriteCriteriaAsyncTask.ACTION_ADD_AS_TAB:
                    toastMsg += " tab added";
                    if (result)
                    {
                        domain.tab = true;
                        updateTabbedListAndRefreshView(true);
                    }
                    else
                        ((ToggleButton) view).setChecked(false);
                    break;
                case WriteCriteriaAsyncTask.ACTION_REMOVE_AS_TAB:
                    toastMsg += " tab removed";
                    if (result)
                    {
                        domain.tab = false;
                        updateTabbedListAndRefreshView(false);
                    }
                    else
                        ((ToggleButton) view).setChecked(false);
                    break;
            }

            if (result)
                Toast.makeText(SearchCriteriaListActivity.this, toastMsg, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(SearchCriteriaListActivity.this, toastMsg + " (fail)", Toast.LENGTH_LONG).show();
        }

        private void updateTabbedListAndRefreshView(boolean add)
        {
            if (add)
                criteriaIdsAsTab.add(domain.id);
            else
                criteriaIdsAsTab.remove(domain.id);
            searchCriteriaArrayAdapter.notifyDataSetChanged();
            updateSortOrderIfTabbed();
        }
    }

    private class ReadAllSearchCriteriaFromDbAsyncTask extends AsyncTask<Void, Void, ArrayList<SearchCriteriaDomain>>
    {
        private AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>> asyncTaskCompletionNotifier;
        private String site;
        private Sort sort;

        public ReadAllSearchCriteriaFromDbAsyncTask(String site, Sort sort,
                        AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>> asyncTaskCompletionNotifier)
        {
            this.site = site;
            this.sort = sort;
            this.asyncTaskCompletionNotifier = asyncTaskCompletionNotifier;
        }

        @Override
        protected ArrayList<SearchCriteriaDomain> doInBackground(Void... params)
        {
            return SearchCriteriaDAO.getAll(SearchCriteriaListActivity.this, site, sort);
        }

        @Override
        protected void onPostExecute(ArrayList<SearchCriteriaDomain> result)
        {
            if (asyncTaskCompletionNotifier != null)
                asyncTaskCompletionNotifier.notifyOnCompletion(result);
        }
    }

    private class SearchCriteriaArrayAdapter extends ArrayAdapter<SearchCriteriaDomain>
    {
        private static final int MAX_NUM_CHARS_FOR_DETAIL = 1000;

        public SearchCriteriaArrayAdapter(Context context, int resource, int textViewResourceId)
        {
            super(context, resource, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            SearchCriteriaViewHolder viewHolder;
            SearchCriteriaDomain item = getItem(position);

            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_criteria_item, null);
                viewHolder = new SearchCriteriaViewHolder();
                viewHolder.delCheckBox = (CheckBox) convertView.findViewById(R.id.deleteItemCheckbox);
                viewHolder.itemLayout = (RelativeLayout) convertView.findViewById(R.id.item);
                viewHolder.itemText = (TextView) convertView.findViewById(R.id.itemText);
                viewHolder.itemDetails = (TextView) convertView.findViewById(R.id.itemDetails);
                viewHolder.addTabToggle = (ToggleButton) convertView.findViewById(R.id.addTabToggle);
                viewHolder.lastRun = (TextView) convertView.findViewById(R.id.itemLastRun);
                viewHolder.ran = (TextView) convertView.findViewById(R.id.itemRan);

                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (SearchCriteriaViewHolder) convertView.getTag();

            prepareDeleteCheckBox(viewHolder.delCheckBox, item);
            prepareTabToggle(viewHolder.addTabToggle, item);
            prepareItemClick(viewHolder.itemLayout, item);

            if (toDeleteList.contains(item))
                viewHolder.delCheckBox.setChecked(true);
            else
                viewHolder.delCheckBox.setChecked(false);

            if (item.tab)
            {
                viewHolder.addTabToggle.setVisibility(View.VISIBLE);
                viewHolder.addTabToggle.setChecked(true);
            }
            else
            {
                viewHolder.addTabToggle.setChecked(false);
                if (criteriaIdsAsTab.size() == MAX_CUSTOM_TABS)
                    viewHolder.addTabToggle.setVisibility(View.INVISIBLE);
                else
                    viewHolder.addTabToggle.setVisibility(View.VISIBLE);
            }

            viewHolder.itemText.setText(item.name);
            viewHolder.itemDetails.setText(getDetailsText(item));
            viewHolder.lastRun.setText("Last Ran " + DateTimeUtils.getElapsedDurationSince(item.lastRun / 1000));
            viewHolder.ran.setText("Ran " + AppUtils.formatNumber(item.runCount) + " times");
            return convertView;
        }

        private void prepareDeleteCheckBox(CheckBox delCheckBox, final SearchCriteriaDomain item)
        {
            delCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if (isChecked && !toDeleteList.contains(item))
                        toDeleteList.add(item);
                    else
                    {
                        if (toDeleteList.contains(item))
                            toDeleteList.remove(item);
                    }

                    if (toDeleteList.isEmpty())
                    {
                        if (actionBarMenu.findItem(R.id.menu_discard).isVisible())
                            actionBarMenu.findItem(R.id.menu_discard).setVisible(false);
                    }
                    else
                    {
                        if (!actionBarMenu.findItem(R.id.menu_discard).isVisible())
                            actionBarMenu.findItem(R.id.menu_discard).setVisible(true);
                    }
                }
            });
        }

        private void prepareTabToggle(final ToggleButton addTabToggle, final SearchCriteriaDomain domain)
        {
            addTabToggle.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if (isChecked)
                    {
                        buttonView.setBackgroundResource(R.drawable.rounded_border_delft);
                        buttonView.setTextColor(getResources().getColor(R.color.delft));

                        onCheckedChangedExecute(domain, buttonView, !criteriaIdsAsTab.contains(domain.id),
                                        WriteCriteriaAsyncTask.ACTION_ADD_AS_TAB);
                    }
                    else
                    {
                        buttonView.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
                        buttonView.setTextColor(getResources().getColor(R.color.lightGrey));

                        onCheckedChangedExecute(domain, buttonView, criteriaIdsAsTab.contains(domain.id),
                                        WriteCriteriaAsyncTask.ACTION_REMOVE_AS_TAB);
                    }
                }

                private void onCheckedChangedExecute(final SearchCriteriaDomain domain, CompoundButton buttonView,
                                boolean executeDbTask, int action)
                {
                    if (executeDbTask)
                    {
                        AddDelCriteriaTabAsyncTaskCompletionNotifier asyncTaskCompletionNotifier =
                                        new AddDelCriteriaTabAsyncTaskCompletionNotifier(buttonView, domain, action);

                        new SearchCriteriaFragment.WriteCriteriaAsyncTask(SearchCriteriaListActivity.this, domain,
                                        action, asyncTaskCompletionNotifier).execute();
                    }
                }
            });
        }

        private void prepareItemClick(RelativeLayout itemLayout, final SearchCriteriaDomain item)
        {
            itemLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(SearchCriteriaListActivity.this, AdvancedSearchActivity.class);
                    intent.setAction(StringConstants.SEARCH_CRITERIA);
                    intent.putExtra(StringConstants.SEARCH_CRITERIA, item);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }

        private String getDetailsText(SearchCriteriaDomain searchCriteriaDomain)
        {
            StringBuilder builder = new StringBuilder();
            if (searchCriteriaDomain.searchCriteria.getQuery() != null
                            && !searchCriteriaDomain.searchCriteria.getQuery().equals(""))
                builder.append("query: " + searchCriteriaDomain.searchCriteria.getQuery() + ", ");

            builder.append("sort: " + searchCriteriaDomain.searchCriteria.getSort());
            builder.append(", answers: " + (searchCriteriaDomain.searchCriteria.getAnswerCount() > 0));
            builder.append(", answered: " + searchCriteriaDomain.searchCriteria.isAnswered());

            if (searchCriteriaDomain.searchCriteria.getIncludedTagsAsSemicolonDelimitedString() != null)
                builder.append(", tagged: "
                                + searchCriteriaDomain.searchCriteria.getIncludedTagsAsSemicolonDelimitedString());

            if (builder.length() > MAX_NUM_CHARS_FOR_DETAIL)
                return builder.substring(0, MAX_NUM_CHARS_FOR_DETAIL + 1) + "...";

            if (searchCriteriaDomain.searchCriteria.getExcludedTagsAsSemicolonDelimitedString() != null)
                builder.append(", not tagged: "
                                + searchCriteriaDomain.searchCriteria.getExcludedTagsAsSemicolonDelimitedString());

            if (builder.length() > MAX_NUM_CHARS_FOR_DETAIL)
                return builder.substring(0, MAX_NUM_CHARS_FOR_DETAIL + 1) + "...";

            return builder.toString();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_criteria_list_view);
        getActionBar().setTitle(ACTION_BAR_TITLE);
        listView = (ListView) findViewById(android.R.id.list);
        searchCriteriaArrayAdapter = new SearchCriteriaArrayAdapter(this, R.layout.search_criteria_item, R.id.itemText);
        listView.setAdapter(searchCriteriaArrayAdapter);
        setupSortSpinner();
    }

    private void setupSortSpinner()
    {
        sortSpinner = (Spinner) findViewById(R.id.searchSortSpinner);
        sortOptionArray =
                        new ArrayList<String>(Arrays.asList(getResources().getStringArray(
                                        R.array.searchCriteriaSortArray)));
        sortSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, sortOptionArray));
        sortSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
            {
                String sort = (String) adapter.getItemAtPosition(position);
                if (sort != null)
                {
                    Log.d(TAG, "sort = " + sort);

                    new ReadAllSearchCriteriaFromDbAsyncTask(OperatingSite.getSite().apiSiteParameter, Sort
                                    .getEnum(sort), getReadCriteriaTaskCompletionNotifier()).execute();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean ret = super.onPrepareOptionsMenu(menu);

        if (menu != null)
            menu.removeItem(R.id.menu_refresh);

        return ret;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_discard:
                deleteCriterias();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteCriterias()
    {
        Long[] ids = new Long[toDeleteList.size()];
        for (int i = 0; i < toDeleteList.size(); i++)
            ids[i] = toDeleteList.get(i).id;

        AsyncTaskCompletionNotifier<Boolean> asyncTaskCompletionNotifier = new AsyncTaskCompletionNotifier<Boolean>()
        {
            @Override
            public void notifyOnCompletion(Boolean result)
            {
                if (result)
                {
                    Log.d(TAG, "Criteria deleted");

                    savedSearchList.removeAll(toDeleteList);
                    searchCriteriaArrayAdapter.notifyDataSetInvalidated();
                    searchCriteriaArrayAdapter.clear();

                    actionBarMenu.findItem(R.id.menu_discard).setVisible(false);
                    AppUtils.decrementNumSavedSearches(getApplicationContext(), toDeleteList.size());
                    for (SearchCriteriaDomain deletedDomain : toDeleteList)
                    {
                        if (deletedDomain.tab)
                            criteriaIdsAsTab.remove(deletedDomain.id);
                    }
                    toDeleteList.clear();

                    if (!savedSearchList.isEmpty())
                    {
                        searchCriteriaArrayAdapter.addAll(savedSearchList);
                        searchCriteriaArrayAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        addContentView(getEmptyView(), new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                }
            }
        };

        new WriteCriteriaAsyncTask(SearchCriteriaListActivity.this, null, WriteCriteriaAsyncTask.ACTION_DEL_MANY,
                        asyncTaskCompletionNotifier).execute(ids);
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");

        super.onResume();

        if (searchCriteriaArrayAdapter == null || searchCriteriaArrayAdapter.getCount() == 0)
            new ReadAllSearchCriteriaFromDbAsyncTask(OperatingSite.getSite().apiSiteParameter, Sort.TABBED,
                            getReadCriteriaTaskCompletionNotifier()).execute();
        else
            searchCriteriaArrayAdapter.notifyDataSetChanged();
    }

    private AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>> getReadCriteriaTaskCompletionNotifier()
    {
        return new AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>>()
        {
            @Override
            public void notifyOnCompletion(ArrayList<SearchCriteriaDomain> result)
            {
                Log.d(TAG, "Saved searches read from DB");
                savedSearchList.clear();
                searchCriteriaArrayAdapter.notifyDataSetInvalidated();
                searchCriteriaArrayAdapter.clear();

                if (result != null && !result.isEmpty())
                {
                    for (SearchCriteriaDomain domain : result)
                    {
                        if (domain.tab)
                            criteriaIdsAsTab.add(domain.id);
                    }
                    savedSearchList.addAll(result);
                    searchCriteriaArrayAdapter.addAll(result);
                }
                else
                {
                    LinearLayout.LayoutParams layoutParams =
                                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.topMargin = 10;
                    addContentView(getEmptyView(), layoutParams);
                }

                searchCriteriaArrayAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    protected void refresh()
    {
        throw new UnsupportedOperationException("Refresh not supported");
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    private View getEmptyView()
    {
        if (emptyItemsView == null)
            emptyItemsView = AppUtils.getEmptyItemsView(SearchCriteriaListActivity.this);

        return emptyItemsView;
    }

    private void updateSortOrderIfTabbed()
    {
        if (sortSpinner.getSelectedItemPosition() == sortOptionArray.indexOf(Sort.TABBED.getValue()))
        {
            ArrayList<SearchCriteriaDomain> criteriaList =
                            SearchCriteriaDAO.getAll(getApplicationContext(), OperatingSite.getSite().apiSiteParameter,
                                            Sort.TABBED);
            searchCriteriaArrayAdapter.notifyDataSetInvalidated();
            savedSearchList.clear();
            searchCriteriaArrayAdapter.clear();
            savedSearchList.addAll(criteriaList);
            searchCriteriaArrayAdapter.addAll(savedSearchList);
            searchCriteriaArrayAdapter.notifyDataSetChanged();
        }
    }
}

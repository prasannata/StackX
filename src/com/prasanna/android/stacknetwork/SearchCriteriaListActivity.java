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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment;
import com.prasanna.android.stacknetwork.fragment.SearchCriteriaFragment.WriteCriteriaAsyncTask;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;

public class SearchCriteriaListActivity extends ListActivity
{
    private static final String TAG = SearchCriteriaListActivity.class.getSimpleName();

    private SearchCriteriaArrayAdapter searchCriteriaArrayAdapter;

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

    class ReadAllSearchCriteriaFromDbAsyncTask extends AsyncTask<Void, Void, ArrayList<SearchCriteriaDomain>>
    {

        private AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>> asyncTaskCompletionNotifier;

        public ReadAllSearchCriteriaFromDbAsyncTask(
                        AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>> asyncTaskCompletionNotifier)
        {
            this.asyncTaskCompletionNotifier = asyncTaskCompletionNotifier;
        }

        @Override
        protected ArrayList<SearchCriteriaDomain> doInBackground(Void... params)
        {
            SearchCriteriaDAO dao = new SearchCriteriaDAO(SearchCriteriaListActivity.this);
            try
            {
                dao.open();
                return dao.readAll();
            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                dao.close();
            }

            return null;
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

                prepareTabToggle(viewHolder.addTabToggle, item);
                prepareItemClick(viewHolder.itemLayout, item);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (SearchCriteriaViewHolder) convertView.getTag();

            viewHolder.itemText.setText(item.name);
            viewHolder.itemDetails.setText(getDetailsText(item));
            viewHolder.lastRun.setText("Last Ran " + DateTimeUtils.getElapsedDurationSince(item.created / 1000));
            viewHolder.ran.setText("Ran " + AppUtils.formatNumber(item.runCount) + " times");
            return convertView;
        }

        private void prepareTabToggle(final ToggleButton addTabToggle, final SearchCriteriaDomain domain)
        {
            final int TAG_ADD_FAILED = 1;

            final AsyncTaskCompletionNotifier<Boolean> asyncTaskCompletionNotifier = new AsyncTaskCompletionNotifier<Boolean>()
            {
                @Override
                public void notifyOnCompletion(Boolean result)
                {
                    if (result)
                    {
                        Toast.makeText(SearchCriteriaListActivity.this, domain.name + " tab update success",
                                        Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(SearchCriteriaListActivity.this, domain.name + " tab update failed",
                                        Toast.LENGTH_LONG).show();
                        addTabToggle.setTag(TAG_ADD_FAILED);
                        addTabToggle.setChecked(false);
                    }
                }
            };

            addTabToggle.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if (isChecked)
                    {
                        new SearchCriteriaFragment.WriteCriteriaAsyncTask(SearchCriteriaListActivity.this, domain,
                                        WriteCriteriaAsyncTask.ACTION_ADD_AS_TAB, asyncTaskCompletionNotifier)
                                        .execute();

                        buttonView.setBackgroundResource(R.drawable.rounded_border_delft);
                        buttonView.setTextColor(getResources().getColor(R.color.delft));
                    }
                    else
                    {
                        buttonView.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
                        buttonView.setTextColor(getResources().getColor(R.color.lightGrey));

                        if (buttonView.getTag() == null)
                        {
                            new SearchCriteriaFragment.WriteCriteriaAsyncTask(SearchCriteriaListActivity.this, domain,
                                            WriteCriteriaAsyncTask.ACTION_REMOVE_AS_TAB, asyncTaskCompletionNotifier)
                                            .execute();
                        }
                        else
                            buttonView.setTag(null);
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
                    intent.putExtra(StringConstants.SEARCH_CRITERIA, item);
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_view);

        searchCriteriaArrayAdapter = new SearchCriteriaArrayAdapter(this, R.layout.search_criteria_item, R.id.itemText);
        setListAdapter(searchCriteriaArrayAdapter);
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onActivityCreated");

        super.onResume();

        if (searchCriteriaArrayAdapter == null || searchCriteriaArrayAdapter.getCount() == 0)
        {
            new ReadAllSearchCriteriaFromDbAsyncTask(new AsyncTaskCompletionNotifier<ArrayList<SearchCriteriaDomain>>()
            {
                @Override
                public void notifyOnCompletion(ArrayList<SearchCriteriaDomain> result)
                {
                    if (result != null)
                    {
                        searchCriteriaArrayAdapter.clear();
                        searchCriteriaArrayAdapter.addAll(result);
                    }
                }
            }).execute();
        }
        else
            searchCriteriaArrayAdapter.notifyDataSetChanged();
    }
}

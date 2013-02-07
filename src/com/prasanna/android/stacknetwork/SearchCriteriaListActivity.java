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
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;

public class SearchCriteriaListActivity extends ListActivity
{
    private static final String TAG = SearchCriteriaListActivity.class.getSimpleName();

    private SearchCriteriaArrayAdapter searchCriteriaArrayAdapter;

    static class SearchCriteriaViewHolder
    {
        CheckBox delCheckBox;
        TextView itemText;
        ToggleButton addAsTabToggle;
        TextView itemDetails;
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

            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_criteria_item, null);
                viewHolder = new SearchCriteriaViewHolder();
                viewHolder.delCheckBox = (CheckBox) convertView.findViewById(R.id.deleteItemCheckbox);
                viewHolder.itemText = (TextView) convertView.findViewById(R.id.itemText);
                viewHolder.itemDetails = (TextView) convertView.findViewById(R.id.itemDetails);
                viewHolder.addAsTabToggle = (ToggleButton) convertView.findViewById(R.id.addTabToggle);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (SearchCriteriaViewHolder) convertView.getTag();

            Log.d(TAG, "position = " + position);
            Log.d(TAG, "item = " + getItem(position).name);

            viewHolder.itemText.setText(getItem(position).name);
            viewHolder.itemDetails.setText(getDetailsText(getItem(position)));
            return convertView;
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

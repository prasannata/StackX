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

import android.app.ListFragment;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.sqlite.SearchCriteriaDAO;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;

public class SearchCriteriaListFragment extends ListFragment
{
    private static final String TAG = SearchCriteriaListFragment.class.getSimpleName();

    private RelativeLayout listViewLayout;
    private SearchCriteriaArrayAdapter searchCriteriaArrayAdapter;

    static class SearchCriteriaViewHolder
    {
        CheckBox delCheckBox;
        TextView itemText;
        ToggleButton addAsTabToggle;
    }

    class ReadAllSearchCriteriaFromDbAsyncTask extends AsyncTask<Void, Void, ArrayList<SearchCriteria>>
    {

        private AsyncTaskCompletionNotifier<ArrayList<SearchCriteria>> asyncTaskCompletionNotifier;

        public ReadAllSearchCriteriaFromDbAsyncTask(
                        AsyncTaskCompletionNotifier<ArrayList<SearchCriteria>> asyncTaskCompletionNotifier)
        {
            this.asyncTaskCompletionNotifier = asyncTaskCompletionNotifier;
        }

        @Override
        protected ArrayList<SearchCriteria> doInBackground(Void... params)
        {
            SearchCriteriaDAO dao = new SearchCriteriaDAO(getActivity());
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
        protected void onPostExecute(ArrayList<SearchCriteria> result)
        {
            if (asyncTaskCompletionNotifier != null)
                asyncTaskCompletionNotifier.notifyOnCompletion(result);
        }

    }

    private class SearchCriteriaArrayAdapter extends ArrayAdapter<SearchCriteria>
    {
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_criteria_builder, null);
                viewHolder = new SearchCriteriaViewHolder();
                viewHolder.delCheckBox = (CheckBox) convertView.findViewById(R.id.deleteItemCheckbox);
                viewHolder.itemText = (TextView) convertView.findViewById(R.id.itemText);
                viewHolder.addAsTabToggle = (ToggleButton) convertView.findViewById(R.id.addTabToggle);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (SearchCriteriaViewHolder) convertView.getTag();

            viewHolder.itemText.setText(getItem(position).name);

            return convertView;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (listViewLayout == null)
        {
            listViewLayout = (RelativeLayout) inflater.inflate(R.layout.list_view, null);
            searchCriteriaArrayAdapter = new SearchCriteriaArrayAdapter(getActivity(), R.layout.search_criteria_item,
                            R.id.itemText);
        }

        return listViewLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        setListAdapter(searchCriteriaArrayAdapter);

        new ReadAllSearchCriteriaFromDbAsyncTask(new AsyncTaskCompletionNotifier<ArrayList<SearchCriteria>>()
        {
            @Override
            public void notifyOnCompletion(ArrayList<SearchCriteria> result)
            {
                if (result != null)
                {
                    searchCriteriaArrayAdapter.clear();
                    searchCriteriaArrayAdapter.addAll(result);
                    searchCriteriaArrayAdapter.notifyDataSetChanged();
                }
            }
        }).execute();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onActivityCreated");

        super.onResume();

        if (searchCriteriaArrayAdapter != null)
            searchCriteriaArrayAdapter.notifyDataSetChanged();
    }
}

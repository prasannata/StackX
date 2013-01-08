/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetTagsAsyncTask;

;

public class TagListFragment extends ListFragment
{
    private static final String TAG = TagListFragment.class.getSimpleName();
    private ArrayAdapter<String> listAdapter;
    private LinearLayout parentLayout;
    private OnTagSelectListener tagSelectListener;
    private ProgressBar progressBar;

    public class GetUserlistAdapterCompletionNotifier implements
            AsyncTaskCompletionNotifier<ArrayList<String>>
    {
        @Override
        public void notifyOnCompletion(ArrayList<String> result)
        {
            if (result != null)
            {
                getProgressBar().setVisibility(View.GONE);

                listAdapter.add(StringConstants.FRONT_PAGE);
                listAdapter.addAll(result);
            }
        }
    }

    public interface OnTagSelectListener
    {
        void onFrontPageSelected();

        void onTagSelected(String tag);
    }

    public static TagListFragment newFragment(OnTagSelectListener onTagSelectListener)
    {
        TagListFragment tagListFragment = new TagListFragment();
        tagListFragment.setRetainInstance(true);
        tagListFragment.setOnTagSelectListener(onTagSelectListener);
        return tagListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (parentLayout == null)
        {
            Log.d(TAG, "Creating view for tag list");

            parentLayout = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.tags_list_item,
                    new ArrayList<String>());

            getProgressBar().setVisibility(View.VISIBLE);

            GetTagsAsyncTask fetchUserAsyncTask = new GetTagsAsyncTask(
                    new GetUserlistAdapterCompletionNotifier(),
                    AppUtils.inRegisteredSite(getActivity()));
            fetchUserAsyncTask.execute(1);
        }

        return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        registerForContextMenu(getListView());

        super.onActivityCreated(savedInstanceState);

        getListView().addFooterView(getProgressBar());
        setListAdapter(listAdapter);

        getActivity().getActionBar().setTitle(StringConstants.TAGS);

        if (listAdapter != null)
            listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        if (position >= 0 && position < listAdapter.getCount() && tagSelectListener != null)
        {
            if (position == 0)
                tagSelectListener.onFrontPageSelected();
            else
                tagSelectListener.onTagSelected(listAdapter.getItem(position));
        }

    }

    public void setOnTagSelectListener(OnTagSelectListener tagSelectListener)
    {
        this.tagSelectListener = tagSelectListener;
    }

    protected ProgressBar getProgressBar()
    {
        if (progressBar == null)
            progressBar = (ProgressBar) getActivity().getLayoutInflater().inflate(
                    R.layout.progress_bar, null);
        return progressBar;
    }
}

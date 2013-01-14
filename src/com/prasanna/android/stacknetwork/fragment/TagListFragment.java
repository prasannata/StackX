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
import java.util.List;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetTagsAsyncTask;

public class TagListFragment extends ListFragment
{
    private static final String TAG = TagListFragment.class.getSimpleName();
    private OnTagSelectListener onTagSelectListener;
    private ArrayAdapter<String> listAdapter;
    private LinearLayout parentLayout;
    private ProgressBar progressBar;
    private boolean tagsFetched = false;

    public interface OnTagSelectListener
    {
        void onFrontPageSelected();

        void onTagSelected(String tag);
    }

    public class GetUserlistAdapterCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<String>>
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

    public class TagListAdapter extends ArrayAdapter<String>
    {

        public TagListAdapter(Context context, int textViewResourceId, List<String> objects)
        {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater()
                            .inflate(R.layout.tag_list_item, null);
            TextView textView = (TextView) layout.findViewById(R.id.tagName);
            textView.setText(getItem(position));
            return layout;
        }
    }

    private ProgressBar getProgressBar()
    {
        if (progressBar == null)
            progressBar = (ProgressBar) getActivity().getLayoutInflater().inflate(R.layout.progress_bar, null);
        return progressBar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (parentLayout == null)
        {
            parentLayout = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            listAdapter = new TagListAdapter(getActivity(), R.layout.tag_list_item, new ArrayList<String>());
        }

        return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        getListView().addFooterView(getProgressBar());
        setListAdapter(listAdapter);

        if (!tagsFetched)
        {
            getProgressBar().setVisibility(View.VISIBLE);
            GetTagsAsyncTask fetchUserAsyncTask = new GetTagsAsyncTask(new GetUserlistAdapterCompletionNotifier(),
                            new TagDAO(getActivity()), AppUtils.inRegisteredSite(getActivity()));
            fetchUserAsyncTask.execute();
            tagsFetched = true;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        if (position >= 0 && position < listAdapter.getCount() && onTagSelectListener != null)
        {
            if (position == 0)
                onTagSelectListener.onFrontPageSelected();
            else
                onTagSelectListener.onTagSelected(listAdapter.getItem(position));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (parentLayout != null)
        {
            Log.d(TAG, "onHiddenChanged: " + hidden);

            if (hidden)
            {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(this);
                ft.commit();
                parentLayout.setVisibility(View.GONE);
            }
            else
                parentLayout.setVisibility(View.VISIBLE);
        }
    }

    public static TagListFragment newFragment(OnTagSelectListener onTagSelectListener)
    {
        TagListFragment fragment = new TagListFragment();
        fragment.onTagSelectListener = onTagSelectListener;
        return fragment;
    }
}

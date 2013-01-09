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

import android.app.ActionBar;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetTagsAsyncTask;

public class TagListFragment extends ListFragment
{
    private OnTagSelectListener onTagSelectListener;
    private ArrayAdapter<String> listAdapter;
    private LinearLayout parentLayout;

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
		listAdapter.add(StringConstants.FRONT_PAGE);
		listAdapter.addAll(result);
	    }
	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	if (parentLayout == null)
	{
	    parentLayout = (LinearLayout) inflater.inflate(R.layout.list_view, null);
	    listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.tag_list_item, new ArrayList<String>());
	    GetTagsAsyncTask fetchUserAsyncTask = new GetTagsAsyncTask(new GetUserlistAdapterCompletionNotifier(),
		            AppUtils.inRegisteredSite(getActivity()));
	    fetchUserAsyncTask.execute(1);
	}

	if (listAdapter.isEmpty())
	{
	}
	return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	registerForContextMenu(getListView());

	super.onActivityCreated(savedInstanceState);

	getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	
	setListAdapter(listAdapter);
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

    public static TagListFragment newFragment(OnTagSelectListener onTagSelectListener)
    {
	TagListFragment fragment = new TagListFragment();
	fragment.onTagSelectListener = onTagSelectListener;
	return fragment;
    }
}
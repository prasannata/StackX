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
import java.util.LinkedHashSet;
import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Tag;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.AsyncTaskExecutor;
import com.prasanna.android.task.GetTagsAsyncTask;

public class TagListFragment extends ListFragment
{
    public static final String TAGS_DIRTY = "dirty";

    private static final String TAG = TagListFragment.class.getSimpleName();
    private final ArrayList<Tag> tags = new ArrayList<Tag>();

    private boolean activityCreated = false;
    private OnTagSelectListener onTagSelectListener;
    private ArrayAdapter<Tag> listAdapter;
    private LinearLayout parentLayout;
    private ProgressBar progressBar;
    private EditText filterListInputText;
    private Button clearFilterInputText;
    private CharSequence defaultHint;
    private Object tagFilterLock = new Object();

    public interface OnTagSelectListener
    {
        void onFrontPageSelected();

        void onTagSelected(String tag);
    }

    public class GetTagListCompletionNotifier implements AsyncTaskCompletionNotifier<LinkedHashSet<Tag>>
    {
        @Override
        public void notifyOnCompletion(LinkedHashSet<Tag> result)
        {
            getProgressBar().setVisibility(View.GONE);

            if (result != null)
            {
                tags.clear();
                listAdapter.clear();
                tags.add(new Tag(StringConstants.FRONT_PAGE));
                tags.addAll(result);
                listAdapter.addAll(tags);
            }
            else
            {
                showError();
            }
        }
    }

    public class TagFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            Log.d(TAG, "performFiltering");

            FilterResults result = new FilterResults();

            if (constraint != null && constraint.length() > 0)
            {
                synchronized (tagFilterLock)
                {

                    ArrayList<Tag> filteredTags = new ArrayList<Tag>();

                    for (Tag tag : tags)
                    {
                        if (tag.name.startsWith((String) constraint))
                            filteredTags.add(tag);
                    }

                    result.count = filteredTags.size();
                    result.values = filteredTags;
                }
            }
            else
            {
                synchronized (tagFilterLock)
                {
                    result.count = tags.size();
                    result.values = tags;
                }
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            ArrayList<Tag> filteredTags = (ArrayList<Tag>) results.values;

            listAdapter.notifyDataSetChanged();
            listAdapter.clear();

            listAdapter.addAll(filteredTags);
            listAdapter.notifyDataSetInvalidated();
        }

    }

    public class TagArrayAdapter extends ArrayAdapter<Tag>
    {
        private Filter filter;

        public TagArrayAdapter(Context context, int textViewResourceId, List<Tag> objects)
        {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.tag_list_item, null);
            }

            TagArrayAdapter adapter = (TagArrayAdapter) getListAdapter();
            ((TextView) convertView).setText(adapter.getItem(position).name);

            if (adapter.getItem(position).local)
                ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.make_available_offline, 0);
            else
                ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            return convertView;
        }

        @Override
        public Filter getFilter()
        {
            if (filter == null)
                filter = new TagFilter();

            return filter;
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
        Log.d(TAG, "onCreateView");

        if (parentLayout == null)
        {
            parentLayout = (LinearLayout) inflater.inflate(R.layout.list_view_with_search, null);
            listAdapter = new TagArrayAdapter(getActivity(), R.layout.tag_list_item, new ArrayList<Tag>());
            filterListInputText = (EditText) parentLayout.findViewById(R.id.searchList);
            defaultHint = filterListInputText.getHint();
        }

        return parentLayout;
    }

    private void setupFilterEditText()
    {

        filterListInputText.setOnFocusChangeListener(new OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    filterListInputText.setHint("");
                    clearFilterInputText.setVisibility(View.VISIBLE);
                }
                else
                {
                    clearFilterInputText.setVisibility(View.GONE);
                }
            }
        });

        filterListInputText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                listAdapter.getFilter().filter(s);
            }
        });

        clearFilterInputText = (Button) parentLayout.findViewById(R.id.clearTextAndFocus);
        clearFilterInputText.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                filterListInputText.setText("");
                filterListInputText.setHint(defaultHint);
                getListView().requestFocus();
                AppUtils.hideSoftInput(getActivity(), v);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        if (!activityCreated)
        {
            setupFilterEditText();
            getListView().setTextFilterEnabled(true);
            getListView().addFooterView(getProgressBar());
            setListAdapter(listAdapter);

            runGetTagsTask();

            activityCreated = true;
        }
    }

    private void runGetTagsTask()
    {
        Log.d(TAG, "Running get tags task");

        getProgressBar().setVisibility(View.VISIBLE);

        GetTagsAsyncTask fetchUserAsyncTask = new GetTagsAsyncTask(new GetTagListCompletionNotifier(), new TagDAO(
                getActivity()), AppUtils.inRegisteredSite(getActivity()));

        AsyncTaskExecutor.getInstance().executeAsyncTask(getActivity(), fetchUserAsyncTask);
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");

        super.onResume();

        if (SharedPreferencesUtil.isSet(getActivity(), TAGS_DIRTY, false))
        {
            runGetTagsTask();
            SharedPreferencesUtil.setBoolean(getActivity(), TAGS_DIRTY, false);
        }
        else
        {
            if (listAdapter != null)
                listAdapter.notifyDataSetChanged();
            else
                runGetTagsTask();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.d(TAG, "Position clicked : " + position + ", total = " + listAdapter.getCount());

        AppUtils.hideSoftInput(getActivity(), v);
        
        if (position >= 0 && position < listAdapter.getCount() && onTagSelectListener != null)
        {
            if (listAdapter.getItem(position).name.equals(StringConstants.FRONT_PAGE))
                onTagSelectListener.onFrontPageSelected();
            else
                onTagSelectListener.onTagSelected(listAdapter.getItem(position).name);
        }
    }

    private void showError()
    {
        View errorView = getActivity().getLayoutInflater().inflate(R.layout.error, null);
        TextView errorTextView = (TextView) errorView.findViewById(R.id.errorMsg);
        errorTextView.setText("Failed to fetch tags");
        getListView().removeFooterView(progressBar);
        getListView().addFooterView(errorView);
    }

    public static TagListFragment newFragment(OnTagSelectListener onTagSelectListener)
    {
        TagListFragment fragment = new TagListFragment();
        fragment.onTagSelectListener = onTagSelectListener;
        return fragment;
    }

    public void setOnTagSelectListener(OnTagSelectListener onTagSelectListener)
    {
        this.onTagSelectListener = onTagSelectListener;
    }
}

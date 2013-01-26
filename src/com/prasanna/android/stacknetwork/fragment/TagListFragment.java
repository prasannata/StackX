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
import java.util.LinkedHashSet;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetTagsAsyncTask;

public class TagListFragment extends ListFragment
{
    private static final String TAG = TagListFragment.class.getSimpleName();

    public static final String TAGS_DIRTY = "dirty";

    private OnTagSelectListener onTagSelectListener;
    private ArrayAdapter<String> listAdapter;
    private LinearLayout parentLayout;
    private ProgressBar progressBar;
    private boolean activityCreated = false;
    private EditText filterListInputText;
    private Button clearFilterInputText;
    private CharSequence defaultHint;

    public interface OnTagSelectListener
    {
        void onFrontPageSelected();

        void onTagSelected(String tag);
    }

    public class GetTagListCompletionNotifier implements AsyncTaskCompletionNotifier<LinkedHashSet<String>>
    {
        @Override
        public void notifyOnCompletion(LinkedHashSet<String> result)
        {
            getProgressBar().setVisibility(View.GONE);

            if (result != null)
            {
                listAdapter.add(StringConstants.FRONT_PAGE);
                listAdapter.addAll(result);
            }
            else
            {
                showError();
            }
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
            parentLayout = (LinearLayout) inflater.inflate(R.layout.list_view_with_search, null);
            listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.tag_list_item, new ArrayList<String>());
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
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (!activityCreated)
        {
            setupFilterEditText();
            getListView().setTextFilterEnabled(true);
            getListView().addFooterView(getProgressBar());
            setListAdapter(listAdapter);

            runGetTagsTask(true);
            activityCreated = true;
        }
    }

    private void runGetTagsTask(boolean fromDb)
    {
        getProgressBar().setVisibility(View.VISIBLE);

        GetTagsAsyncTask fetchUserAsyncTask = new GetTagsAsyncTask(new GetTagListCompletionNotifier(), new TagDAO(
                        getActivity()), AppUtils.inRegisteredSite(getActivity()), fromDb);
        fetchUserAsyncTask.execute();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");

        super.onResume();

        if (SharedPreferencesUtil.isOn(getActivity(), TAGS_DIRTY, false))
        {
            listAdapter.clear();
            runGetTagsTask(true);
        }
        else
        {
            if (listAdapter != null)
                listAdapter.notifyDataSetChanged();
        }
    }

    public void onStop()
    {
        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (position >= 0 && position < listAdapter.getCount() && onTagSelectListener != null)
        {
            if (listAdapter.getItem(position).equals(StringConstants.FRONT_PAGE))
                onTagSelectListener.onFrontPageSelected();
            else
                onTagSelectListener.onTagSelected(listAdapter.getItem(position));
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
}

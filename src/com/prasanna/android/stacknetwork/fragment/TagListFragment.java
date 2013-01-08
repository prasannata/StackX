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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetTagsAsyncTask;

;

public class TagListFragment extends ListFragment
{
    private static final String TAG = TagListFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG_PREFIX = "spinner-";
    private static final String FRAGMENT_TAG_FRONT_PAGE = FRAGMENT_TAG_PREFIX + "-home";

    private ArrayAdapter<String> listAdapter;
    private LinearLayout parentLayout;

    public class GetUserlistAdapterCompletionNotifier implements
            AsyncTaskCompletionNotifier<ArrayList<String>>
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
            listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.tags_list_item,
                    new ArrayList<String>());
        }

        if (listAdapter.isEmpty())
        {
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

        setListAdapter(listAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        if (position >= 0 && position < listAdapter.getCount())
        {
            if (position == 0)
                loadFrontPage();
            else
                loadFaqForTag(position);
        }

    }

    private void loadFrontPage()
    {
        Log.d(TAG, "Front page selected");

        QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_PREFIX
                + "-home");

        if (fragment == null)
        {
            Log.d(TAG, "Creating new fragment for front page");
            fragment = QuestionListFragment.newFragment(QuestionsIntentService.GET_FRONT_PAGE);
        }

        beginTransaction(fragment, FRAGMENT_TAG_FRONT_PAGE);
    }

    private void loadFaqForTag(int itemPosition)
    {
        Log.d(TAG, listAdapter.getItem(itemPosition) + " selected");

        QuestionListFragment fragment = (QuestionListFragment) findFragment(FRAGMENT_TAG_PREFIX
                + listAdapter.getItem(itemPosition));
        if (fragment == null)
            beginFaqForTagFragment(listAdapter.getItem(itemPosition));
        else
            beginTransaction(fragment, FRAGMENT_TAG_PREFIX + listAdapter.getItem(itemPosition));
    }

    private void beginFaqForTagFragment(String faqTag)
    {
        QuestionListFragment newFragment = QuestionListFragment
                .newFragment(QuestionsIntentService.GET_FAQ_FOR_TAG);
        newFragment.getBundle().putString(StringConstants.TAG, faqTag);
        beginTransaction(newFragment, FRAGMENT_TAG_PREFIX + faqTag);
    }

    private void beginTransaction(Fragment fragment, String fragmentTag)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        Fragment currentFragment = findFragment("tags");

        if (currentFragment != null)
            ft.remove(currentFragment);
        ft.commit();

        ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.add(fragment, fragmentTag);
        ft.commit();
    }

    private Fragment findFragment(String fragmentTag)
    {
        return getFragmentManager().findFragmentByTag(fragmentTag);
    }
}

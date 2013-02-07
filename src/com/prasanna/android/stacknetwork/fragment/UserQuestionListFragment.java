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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserQuestionListFragment extends AbstractQuestionListFragment
{
    private static final String TAG = UserQuestionListFragment.class.getSimpleName();
    private Intent intent;
    private int page = 0;
    private int action;
    private boolean activityCreated = false;

    public static UserQuestionListFragment newFragment(int action)
    {
        UserQuestionListFragment userQuestionListFragment = new UserQuestionListFragment();
        userQuestionListFragment.action = action;
        return userQuestionListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        page = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (itemsContainer == null)
        {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            itemListAdapter = new ItemListAdapter<Question>(getActivity(), R.layout.question_snippet_layout,
                            new ArrayList<Question>(), this);
        }

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");
        
        super.onActivityCreated(savedInstanceState);
        
        if (savedInstanceState != null)
            action = savedInstanceState.getInt(StringConstants.ACTION);

        if (!activityCreated)
        {
            startIntentService();
            activityCreated = true;
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");
        
        outState.putInt(StringConstants.ACTION, action);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        Log.d(getLogTag(), "onCreateContextMenu");

        super.onCreateContextMenu(menu, v, menuInfo);

        menu.removeItem(R.id.q_ctx_comments);
        menu.removeItem(R.id.q_ctx_menu_user_profile);
    }

    @Override
    public void onStop()
    {
        Log.d(getLogTag(), "onStop");

        super.onStop();

        stopService(intent);
    }

    @Override
    protected void startIntentService()
    {
        Log.d(TAG, "startIntentService for action " + action);

        showProgressBar();

        intent = getIntentForService(UserIntentService.class, UserIntentAction.QUESTIONS_BY_USER.getAction());
        if (intent != null)
        {
            intent.putExtra(StringConstants.ACTION, action);
            intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
            intent.putExtra(StringConstants.USER_ID, getActivity().getIntent()
                            .getLongExtra(StringConstants.USER_ID, 0L));
            intent.putExtra(StringConstants.PAGE, ++page);
            intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

            startService(intent);
        }
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }
}

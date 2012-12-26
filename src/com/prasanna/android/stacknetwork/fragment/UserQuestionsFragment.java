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

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserQuestionsFragment extends AbstractQuestionsFragment
{
    private static final String TAG = UserQuestionsFragment.class.getSimpleName();
    private ScrollViewWithNotifier scroller;
    private User user;
    private Intent intent;
    private int page = 0;
    private LinearLayout parentLayout;
    private LinearLayout itemsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        page = 0;

        if (items == null || items.isEmpty() == true)
        {
            user = (User) getActivity().getIntent().getSerializableExtra(StringConstants.USER);

            registerReceiver();

            startIntentService();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "Creating question fragment");

        parentLayout = (LinearLayout) inflater.inflate(R.layout.scroll_linear_layout, null);

        scroller = (ScrollViewWithNotifier) parentLayout.findViewById(R.id.scroller_with_linear_layout);
        itemsLayout = (LinearLayout) scroller.findViewById(R.id.ll_in_scroller);
        scroller.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
        {
            @Override
            public void onScrollToBottom(View view)
            {
                UserQuestionsFragment.this.onScrollToBottom();

                startIntentService();
            }
        });

        showLoadingSpinningWheel();

        if (items != null && items.isEmpty() == false)
        {
            displayItems();
        }

        return parentLayout;
    }

    @Override
    protected void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void startIntentService()
    {
        intent = getIntentForService(UserQuestionsIntentService.class,
                IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        intent.putExtra(StringConstants.USER_ID, user.id);
        intent.putExtra(StringConstants.PAGE, ++page);
        intent.putExtra(StringConstants.ACCESS_TOKEN, user.accessToken);
        getActivity().startService(intent);
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }

    @Override
    protected LinearLayout getParentLayout()
    {
        return itemsLayout;
    }
}

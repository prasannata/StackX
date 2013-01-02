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

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserQuestionListFragment extends AbstractQuestionListFragment
{
    private static final String TAG = UserQuestionListFragment.class.getSimpleName();
    private Intent intent;
    private int page = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	page = 0;

	registerReceiver();

	startIntentService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	Log.d(TAG, "Creating question fragment");

	if (itemsContainer == null)
	{
	    itemsContainer = (LinearLayout) inflater.inflate(R.layout.items_fragment_container, null);
	    itemListAdapter = new ItemListAdapter<Question>(getActivity(), R.layout.question_snippet_layout,
		            new ArrayList<Question>(), this);
	    setListAdapter(itemListAdapter);

	    showLoadingSpinningWheel();
	}

	return itemsContainer;
    }

    @Override
    protected void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().registerReceiver(receiver, filter);
    }

    @Override
    protected void startIntentService()
    {
	intent = getIntentForService(UserIntentService.class,
	                IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
	intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_QUESTIONS);
	intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
	intent.putExtra(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
	intent.putExtra(StringConstants.PAGE, ++page);
	startService();
    }

    @Override
    public String getLogTag()
    {
	return TAG;
    }
}

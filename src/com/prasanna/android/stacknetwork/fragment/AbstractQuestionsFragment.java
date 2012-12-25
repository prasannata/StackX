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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractQuestionsFragment extends ItemDisplayFragment<Question>
{
    private int itemDisplayCursor = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	itemDisplayCursor = 0;

	return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void displayItems()
    {
	dismissLoadingDialog();

	if (loadingProgressView != null)
	{
	    loadingProgressView.setVisibility(View.GONE);
	    loadingProgressView = null;
	}

	Log.d(getLogTag(), "questions size: " + items.size() + ", lastDisplayQuestionIndex: " + itemDisplayCursor);

	for (; itemDisplayCursor < items.size(); itemDisplayCursor++)
	{
	    LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(
		            getActivity().getLayoutInflater(), getActivity(), false, items.get(itemDisplayCursor));
	    itemsContainer.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT));
	}

	serviceRunning = false;
    }

    @Override
    public String getReceiverExtraName()
    {
	return StringConstants.QUESTIONS;
    }
}

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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.SearchForQuestionsIntentService;
import com.prasanna.android.stacknetwork.intent.TagFaqIntentService;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsFragment extends AbstractQuestionsFragment
{
    private static final String TAG = QuestionsFragment.class.getSimpleName();

    private Intent intent;

    private int currentPage = 0;

    private IntentFilter filter;

    public enum QuestionAction
    {
	FRONT_PAGE,
	FAQS_TAG,
	SEARCH;
    }

    public interface OnGetQuestionsListener
    {
	void onGetQuestions(QuestionAction questionAction, String tag);
    }

    @Override
    public void onAttach(Activity activity)
    {
	super.onAttach(activity);

	if (!(activity instanceof OnGetQuestionsListener))
	    throw new ClassCastException(activity.toString() + " must implement OnGetQuestionsListener");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	itemsContainer = (LinearLayout) inflater.inflate(R.layout.items_fragment_container, container, false);

	return itemsContainer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	if (savedInstanceState != null)
	{
	    Log.d(TAG, "onCreate savedInstanceState");

	    items = (ArrayList<Question>) savedInstanceState.getSerializable(StringConstants.QUESTIONS);

	    if (items != null)
		displayItems();
	}
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
	Log.d(TAG, "Saving instance state");
	if (items != null && items.isEmpty() == false)
	{
	    outState.putSerializable(StringConstants.QUESTIONS, items);
	}

	super.onSaveInstanceState(outState);
    }

    @Override
    public void startIntentService()
    {
	intent.putExtra(StringConstants.PAGE, ++currentPage);
	getActivity().startService(intent);
	serviceRunning = true;
    }

    @Override
    protected void registerReceiver()
    {
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public String getLogTag()
    {
	return TAG;
    }

    private void cancelRunningServiceAndReceiver()
    {
	if (serviceRunning)
	{
	    getActivity().stopService(intent);
	    getActivity().unregisterReceiver(receiver);
	    serviceRunning = false;
	}
    }

    private void clean()
    {
	cancelRunningServiceAndReceiver();

	itemsContainer.removeAllViews();

	currentPage = 0;
    }

    public void loadFrontPage()
    {
	clean();

	intent = getIntentForService(UserQuestionsIntentService.class, QuestionIntentAction.QUESTIONS.name());
	intent.setAction(QuestionIntentAction.QUESTIONS.name());
	filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();
    }

    public void getFaqsForTag(String tag)
    {
	clean();

	intent = getIntentForService(TagFaqIntentService.class, QuestionIntentAction.TAGS_FAQ.name());
	intent.setAction(QuestionIntentAction.TAGS_FAQ.name());
	intent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), tag);
	filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();
    }

    public void search(String query)
    {
	clean();

	intent = getIntentForService(SearchForQuestionsIntentService.class, QuestionIntentAction.QUESTION_SEARCH.name());
	intent.putExtra(SearchManager.QUERY, query);

	filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();

    }

    @Override
    protected LinearLayout getParentLayout()
    {
	return itemsContainer;
    }
}

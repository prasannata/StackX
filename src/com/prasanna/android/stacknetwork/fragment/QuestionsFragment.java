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
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsFragment extends ItemDisplayFragment<Question>
{
    private static final String TAG = QuestionsFragment.class.getSimpleName();

    private Intent intent;
    private IntentFilter filter;
    private int itemDisplayCursor = 0;
    private int currentPage = 0;

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
	itemDisplayCursor = 0;
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
    protected void startIntentService()
    {
	intent.putExtra(StringConstants.PAGE, ++currentPage);
	startService();
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

    private void stopRunningServiceAndReceiver()
    {
	if (isServiceRunning())
	{
	    getActivity().stopService(intent);
	    getActivity().unregisterReceiver(receiver);
	}
    }

    private void clean()
    {
	stopRunningServiceAndReceiver();

	itemsContainer.removeAllViews();

	currentPage = 0;

	itemDisplayCursor = 0;

	items.clear();
    }

    public void loadFrontPage()
    {
	clean();

	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.name());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FRONT_PAGE);
	filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();
    }

    public void getFaqsForTag(String tag)
    {
	clean();

	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.TAGS_FAQ.name());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FAQ_FOR_TAG);
	intent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), tag);
	filter = new IntentFilter(QuestionIntentAction.TAGS_FAQ.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();
    }

    public void search(String query)
    {
	clean();

	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTION_SEARCH.name());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.SEARCH);
	intent.putExtra(SearchManager.QUERY, query);

	filter = new IntentFilter(QuestionIntentAction.QUESTION_SEARCH.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();

    }

    @Override
    protected void displayItems()
    {
	dismissLoadingSpinningWheel();

	Log.d(getLogTag(), "questions size: " + items.size() + ", lastDisplayQuestionIndex: " + itemDisplayCursor);

	for (; itemDisplayCursor < items.size(); itemDisplayCursor++)
	{
	    LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(
		            getActivity().getLayoutInflater(), getActivity(), false, items.get(itemDisplayCursor));
	    getParentLayout().addView(questionLayout,
		            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}
    }

    @Override
    public String getReceiverExtraName()
    {
	return QuestionIntentAction.QUESTIONS.getExtra();
    }

    @Override
    protected ViewGroup getParentLayout()
    {
	return itemsContainer;
    }

}

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsFragment extends AbstractListQuestionFragment
{
    private static final String TAG = QuestionsFragment.class.getSimpleName();

    private Intent intent;
    private IntentFilter filter;
    private int currentPage = 0;
    private boolean relatedQuestions = false;

    public enum QuestionAction
    {
	FRONT_PAGE,
	FAQS_TAG,
	SEARCH
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
	if (itemsContainer == null)
	{
	    itemsContainer = (LinearLayout) inflater.inflate(R.layout.items_fragment_container, container, false);
	    itemListAdapter = new ItemListAdapter<Question>(getActivity(), R.layout.question_snippet_layout,
		            new ArrayList<Question>(), this);
	    setListAdapter(itemListAdapter);
	}

	return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	super.onActivityCreated(savedInstanceState);

	if (relatedQuestions)
	    getRelatedQuestions(getActivity().getIntent().getLongExtra(StringConstants.QUESTION_ID, 0));
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
	Log.d(TAG, "Saving instance state");
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

    protected void clean()
    {
	stopRunningServiceAndReceiver();

	itemListAdapter.clear();

	currentPage = 0;
    }

    public void getFrontPage()
    {
	clean();

	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.name());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_FRONT_PAGE);
	filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());

	showLoadingSpinningWheel();

	registerReceiver();

	startIntentService();
    }

    public void getRelatedQuestions(long questionId)
    {
	clean();

	intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.name());
	intent.putExtra(StringConstants.ACTION, QuestionsIntentService.GET_RELATED);
	intent.putExtra(StringConstants.QUESTION_ID, questionId);
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
    public boolean onContextItemSelected(MenuItem item)
    {
	Log.d(getLogTag(), "onContextItemSelected");

	if (!super.onContextItemSelected(item))
	{
	    if (item.getGroupId() == R.id.qContextMenuGroup)
	    {
		Log.d(getLogTag(), "Context item selected: " + item.getItemId());

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId())
		{
		    case R.id.q_ctx_related:
			getRelatedQuestions(itemListAdapter.getItem(info.position).id);
			return true;
		    default:
			Log.d(TAG, "Unknown item " + item.getItemId());
			return false;
		}
	    }
	}

	return false;
    }

    @Override
    public void refresh()
    {
	clean();

	super.refresh();
    }

    public void setFetchRelatedQuestions(boolean relatedQuestions)
    {
	this.relatedQuestions = relatedQuestions;
    }
}

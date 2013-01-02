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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserAnswerListFragment extends ItemListFragment<Answer> implements ListItemView<Answer>
{
    private static final String TAG = UserAnswerListFragment.class.getSimpleName();
    private int page = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	Log.d(TAG, "onCreate");

	super.onCreate(savedInstanceState);

	registerReceiver();

	startIntentService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	Log.d(TAG, "Creating answer fragment");

	if (itemsContainer == null)
	{
	    itemsContainer = (LinearLayout) inflater.inflate(R.layout.items_fragment_container, null);
	    itemListAdapter = new ItemListAdapter<Answer>(getActivity(), R.layout.answer_snippet,
		            new ArrayList<Answer>(), this);
	    setListAdapter(itemListAdapter);

	    showLoadingSpinningWheel();
	}
	return itemsContainer;
    }

    @Override
    protected void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(UserIntentAction.ANSWERS_BY_USER.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().registerReceiver(receiver, filter);
    }

    @Override
    protected void startIntentService()
    {
	Intent intent = getIntentForService(UserIntentService.class,
	                IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
	intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_ANSWERS);
	intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
	intent.putExtra(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
	intent.putExtra(StringConstants.PAGE, page++);

	startService();
    }

    @Override
    public String getReceiverExtraName()
    {
	return UserIntentAction.ANSWERS_BY_USER.getExtra();
    }

    @Override
    protected void displayItems(ArrayList<Answer> items)
    {
	dismissLoadingSpinningWheel();

	Log.d(TAG, "Add items to adapter");

	itemListAdapter.addAll(items);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	super.onActivityCreated(savedInstanceState);

	registerForContextMenu(getListView());

	getListView().setOnScrollListener(this);
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    @Override
    protected ViewGroup getParentLayout()
    {
	return itemsContainer;
    }

    @Override
    public View getView(final Answer answer, View convertView, ViewGroup parent)
    {
	final LinearLayout answerRow = (LinearLayout) getActivity().getLayoutInflater().inflate(
	                R.layout.answer_snippet, null);

	if (answer.accepted)
	    answerRow.setBackgroundResource(R.drawable.rounded_lichen_color_border_grey);

	TextView textView = (TextView) answerRow.findViewById(R.id.questionTitle);
	textView.setText(Html.fromHtml(answer.title));

	textView = (TextView) answerRow.findViewById(R.id.answerScore);
	textView.setText("Answer Score: " + answer.score);

	textView = (TextView) answerRow.findViewById(R.id.answerTime);
	textView.setText(DateTimeUtils.getElapsedDurationSince(answer.creationDate));

	textView = (TextView) answerRow.findViewById(R.id.answerBodyPreview);
	if (answer.body != null && answer.body.length() > 200)
	    textView.setText(Html.fromHtml(answer.body.substring(0, 200) + "...."));
	else
	    textView.setText(Html.fromHtml(answer.body));
	return answerRow;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
	Intent intent = new Intent(getActivity(), QuestionActivity.class);
	intent.setAction(StringConstants.QUESTION_ID);
	intent.putExtra(StringConstants.QUESTION_ID, itemListAdapter.getItem(position).questionId);
	startActivity(intent);
    }
}

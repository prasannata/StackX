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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
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
    private static final int ANSWER_PREVIEW_LEN = 200;
    private static final String ANS_CONTNUES = "...";
    private static final String MULTIPLE_NEW_LINES_AT_END = "[\\n]+$";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	Log.d(TAG, "Creating answer fragment");

	if (itemsContainer == null)
	{
	    itemsContainer = (LinearLayout) inflater.inflate(R.layout.items_fragment_container, null);
	    itemListAdapter = new ItemListAdapter<Answer>(getActivity(), R.layout.answer_snippet,
		            new ArrayList<Answer>(), this);

	    showProgressBar();
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
	dismissProgressBar();

	Log.d(TAG, "Add items to adapter");

	itemListAdapter.addAll(items);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	super.onActivityCreated(savedInstanceState);

	registerForContextMenu(getListView());

	getListView().addFooterView(getProgressBar());
	setListAdapter(itemListAdapter);
	getListView().setOnScrollListener(this);

	registerReceiver();
	startIntentService();
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
	LinearLayout answerRow = (LinearLayout) getActivity().getLayoutInflater()
	                .inflate(R.layout.answer_snippet, null);

	TextView textView = (TextView) answerRow.findViewById(R.id.itemScore);
	textView.setVisibility(View.GONE);

	if (answer.accepted)
	    answerRow.findViewById(R.id.acceptedAnswer).setVisibility(View.VISIBLE);

	textView = (TextView) answerRow.findViewById(R.id.itemTitle);
	textView.setText(Html.fromHtml(answer.title));
	RelativeLayout.LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
	layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	textView.setLayoutParams(layoutParams);

	textView = (TextView) answerRow.findViewById(R.id.answerScore);
	textView.setText("Answer Score: " + answer.score);

	textView = (TextView) answerRow.findViewById(R.id.answerTime);
	textView.setText(DateTimeUtils.getElapsedDurationSince(answer.creationDate));

	textView = (TextView) answerRow.findViewById(R.id.answerBodyPreview);

	if (answer.body != null)
	{
	    String answerBody = answer.body.replaceAll(MULTIPLE_NEW_LINES_AT_END, "\n");

	    if (answerBody.length() > ANSWER_PREVIEW_LEN)
	    {
		answerBody = answerBody.substring(0, ANSWER_PREVIEW_LEN);
		textView.setText(Html.fromHtml(answerBody + ANS_CONTNUES));
	    }
	    else
		textView.setText(Html.fromHtml(answerBody));
	}
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

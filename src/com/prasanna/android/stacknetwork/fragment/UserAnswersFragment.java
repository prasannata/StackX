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
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserAnswersFragment extends ItemDisplayFragment<Answer>
{
    private static final String TAG = UserAnswersFragment.class.getSimpleName();
    private int answerDisplayCursor = 0;
    private ScrollViewWithNotifier scroller;
    private LinearLayout loadingProgressView;
    private Intent intent;
    private int page = 0;
    private LinearLayout itemsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	if (items == null || items.isEmpty() == true)
	{
	    registerReceiver();

	    startIntentService();
	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	answerDisplayCursor = 0;

	Log.d(TAG, "Creating answer fragment");

	scroller = (ScrollViewWithNotifier) inflater.inflate(R.layout.scroll_linear_layout, null);
	itemsLayout = (LinearLayout) scroller.findViewById(R.id.ll_in_scroller);
	scroller.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
	{
	    @Override
	    public void onScrollToBottom(View view)
	    {
		UserAnswersFragment.this.onScrollToBottom();

		startIntentService();
	    }
	});

	showLoadingSpinningWheel();

	if (items != null && items.isEmpty() == false)
	{
	    displayItems();
	}

	return scroller;
    }

    private void addAnswersToView()
    {
	for (; answerDisplayCursor < items.size(); answerDisplayCursor++)
	{
	    final RelativeLayout answerRow = (RelativeLayout) getActivity().getLayoutInflater().inflate(
		            R.layout.user_item_row, null);
	    final Answer answer = items.get(answerDisplayCursor);
	    TextView textView = (TextView) answerRow.findViewById(R.id.userItemTitle);
	    textView.setText(Html.fromHtml(answer.title));

	    textView = (TextView) answerRow.findViewById(R.id.viewItem);
	    textView.setClickable(true);
	    textView.setOnClickListener(new View.OnClickListener()
	    {
		@Override
		public void onClick(View v)
		{
		    Point size = new Point();
		    getActivity().getWindowManager().getDefaultDisplay().getSize(size);

		    PopupBuilder.build(getActivity().getLayoutInflater(), answerRow, answer, size);
		}
	    });

	    textView = (TextView) answerRow.findViewById(R.id.viewQuestion);
	    textView.setClickable(true);
	    textView.setOnClickListener(new View.OnClickListener()
	    {
		@Override
		public void onClick(View v)
		{
		    Intent intent = new Intent(getActivity(), QuestionActivity.class);
		    Question question = new Question();
		    question.id = answer.questionId;
		    question.title = answer.title;
		    intent.putExtra(StringConstants.QUESTION, question);
		    intent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name(), true);
		    startActivity(intent);
		}
	    });
	    itemsLayout.addView(answerRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT));
	}
    }

    @Override
    protected void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(UserIntentAction.ANSWERS_BY_USER.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public String getReceiverExtraName()
    {
	return UserIntentAction.ANSWERS_BY_USER.getExtra();
    }

    @Override
    public void startIntentService()
    {
	intent = getIntentForService(UserIntentService.class,
	                IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
	intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_ANSWERS);
	intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
	intent.putExtra(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
	intent.putExtra(StringConstants.PAGE, ++page);

	getActivity().startService(intent);
    }

    @Override
    protected void displayItems()
    {
	dismissLoadingSpinningWheel();

	if (loadingProgressView != null)
	{
	    loadingProgressView.setVisibility(View.GONE);
	    loadingProgressView = null;
	}

	if (items != null && scroller != null)
	{
	    addAnswersToView();
	}
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    @Override
    protected ViewGroup getParentLayout()
    {
	return itemsLayout;
    }
}

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

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.intent.UserInboxIntentService;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserInboxActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = UserInboxActivity.class.getSimpleName();

    private Intent fetchInboxIntent = null;

    private LinearLayout questionsLayout;

    private LinearLayout questionsDisplayList;

    private ScrollViewWithNotifier questionsScroll;

    private ArrayList<InboxItem> inboxItems = new ArrayList<InboxItem>();

    private int itemCursor = 0;

    private LinearLayout loadingProgressView;

    private int page = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    if (loadingProgressView != null)
	    {
		loadingProgressView.setVisibility(View.GONE);
		loadingProgressView = null;
	    }

	    if (intent.getSerializableExtra(UserIntentAction.INBOX.getExtra()) != null)
	    {
		inboxItems.addAll((ArrayList<InboxItem>) intent.getSerializableExtra(UserIntentAction.INBOX.getExtra()));

		displayInbox();
	    }
	}
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	questionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.scroll_linear_layout, null);
	questionsScroll = (ScrollViewWithNotifier) questionsLayout.findViewById(R.id.scroller_with_linear_layout);
	questionsDisplayList = (LinearLayout) questionsScroll.findViewById(R.id.ll_in_scroller);

	questionsScroll.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
	{
	    @Override
	    public void onScrollToBottom(View view)
	    {
		if (loadingProgressView == null)
		{
		    loadingProgressView = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress, null);
		    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
			            LayoutParams.WRAP_CONTENT);
		    layoutParams.setMargins(0, 15, 0, 15);

		    questionsLayout.addView(loadingProgressView, layoutParams);
		}

		startIntentService();
	    }
	});

	setContentView(questionsLayout);

	registerReceiver();

	startIntentService();
    }

    @Override
    protected void onDestroy()
    {
	super.onDestroy();
	stopServiceAndUnregisterReceiver();
    }

    @Override
    public void onStop()
    {
	super.onStop();

	stopServiceAndUnregisterReceiver();
    }

    private void startIntentService()
    {
	fetchInboxIntent = new Intent(this, UserInboxIntentService.class);

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
	{
	    fetchInboxIntent.putExtra(StringConstants.ACCESS_TOKEN,
		            sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null));

	    fetchInboxIntent.putExtra(StringConstants.PAGE, ++page);

	    startService(fetchInboxIntent);
	}
    }

    private void registerReceiver()
    {
	IntentFilter filter = new IntentFilter(UserIntentAction.INBOX.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(receiver, filter);
    }

    private void stopServiceAndUnregisterReceiver()
    {
	if (fetchInboxIntent != null)
	{
	    stopService(fetchInboxIntent);
	}

	try
	{
	    unregisterReceiver(receiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    private void displayInbox()
    {
	for (; itemCursor < inboxItems.size(); itemCursor++)
	{
	    final RelativeLayout itemRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.user_item_row, null);
	    final InboxItem inboxItem = inboxItems.get(itemCursor);

	    TextView textView = (TextView) itemRow.findViewById(R.id.userItemTitle);
	    textView.setText(Html.fromHtml(inboxItem.title));

	    textView = (TextView) itemRow.findViewById(R.id.viewItem);
	    textView.setClickable(true);
	    textView.setText(R.string.viewMessage);
	    textView.setOnClickListener(new View.OnClickListener()
	    {
		@Override
		public void onClick(View v)
		{
		    Point size = new Point();
		    getWindowManager().getDefaultDisplay().getSize(size);

		    PopupBuilder.build(getLayoutInflater(), itemRow, inboxItem, size);
		}
	    });

	    textView = (TextView) itemRow.findViewById(R.id.viewQuestion);
	    textView.setClickable(true);
	    textView.setOnClickListener(new View.OnClickListener()
	    {
		@Override
		public void onClick(View v)
		{
		    Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
		    Question question = new Question();
		    question.id = inboxItem.questionId;
		    question.title = inboxItem.title;
		    intent.putExtra(StringConstants.QUESTION, question);
		    intent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name(), true);
		    startActivity(intent);
		}
	    });
	    questionsDisplayList.addView(itemRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT));
	}

    }

    @Override
    protected void onCreateOptionsMenuPostProcess(Menu menu)
    {
	menu.removeItem(R.id.menu_my_inbox);
    }

    @Override
    public void refresh()
    {
	questionsDisplayList.removeAllViews();
	inboxItems.clear();
	page = 0;
	itemCursor = 0;
	startIntentService();
    }

    @Override
    public Context getCurrentContext()
    {
	return UserInboxActivity.this;
    }
}

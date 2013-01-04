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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserInboxActivity extends AbstractUserActionBarActivity implements
        StackXRestQueryResultReceiver
{
    private static final String TAG = UserInboxActivity.class.getSimpleName();

    private Intent intent = null;

    private LinearLayout questionsDisplayList;

    private ScrollViewWithNotifier questionsScroll;

    private ArrayList<InboxItem> inboxItems = new ArrayList<InboxItem>();

    private int itemCursor = 0;

    private LinearLayout loadingProgressView;

    private int page = 0;

    private RestQueryResultReceiver receiver;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        questionsScroll = (ScrollViewWithNotifier) getLayoutInflater().inflate(
                R.layout.scroll_linear_layout, null);
        questionsDisplayList = (LinearLayout) questionsScroll.findViewById(R.id.ll_in_scroller);

        questionsScroll.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
        {
            @Override
            public void onScrollToBottom(View view)
            {
                if (loadingProgressView == null)
                {
                    loadingProgressView = (LinearLayout) getLayoutInflater().inflate(
                            R.layout.loading_progress, null);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 15, 0, 15);

                    questionsDisplayList.addView(loadingProgressView, layoutParams);
                }

                startIntentService();
            }
        });

        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);

        setContentView(questionsScroll);

        startIntentService();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (intent != null)
            stopService(intent);
    }

    private void startIntentService()
    {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
        {
            intent = new Intent(this, UserIntentService.class);
            intent.setAction(StringConstants.INBOX_ITEMS);
            intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_INBOX);
            intent.putExtra(StringConstants.PAGE, ++page);
            intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);

            startService(intent);
        }
    }

    private void displayInbox()
    {
        for (; itemCursor < inboxItems.size(); itemCursor++)
        {
            final RelativeLayout itemRow = (RelativeLayout) getLayoutInflater().inflate(
                    R.layout.user_item_row, null);
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
                    Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                    Question question = new Question();
                    question.id = inboxItem.questionId;
                    question.title = inboxItem.title;
                    intent.putExtra(StringConstants.QUESTION, question);
                    intent.putExtra(QuestionIntentAction.QUESTION_FULL_DETAILS.getAction(), true);
                    startActivity(intent);
                }
            });
            questionsDisplayList.addView(itemRow, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        menu.removeItem(R.id.menu_my_inbox);

        return ret & true;
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

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        if (loadingProgressView != null)
        {
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }

        if (resultData.getSerializable(StringConstants.INBOX_ITEMS) != null)
        {
            inboxItems.addAll((ArrayList<InboxItem>) resultData
                    .getSerializable(StringConstants.INBOX_ITEMS));

            displayInbox();
        }
    }
}

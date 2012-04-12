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
                inboxItems
                        .addAll((ArrayList<InboxItem>) intent.getSerializableExtra(UserIntentAction.INBOX.getExtra()));

                displayInbox();
            }
        }
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        questionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.questions_layout, null);
        questionsScroll = (ScrollViewWithNotifier) questionsLayout.findViewById(R.id.questionsScroll);
        questionsDisplayList = (LinearLayout) getLayoutInflater().inflate(R.layout.items_fragment_container, null);
        questionsScroll.addView(questionsDisplayList);
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
                    questionsDisplayList.addView(loadingProgressView, layoutParams);
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
            textView.setText("View message");
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
    public void refresh()
    {
        questionsDisplayList.removeAllViews();
        inboxItems.clear();
        page = 0;
        itemCursor = 0;
        startIntentService();
    }

    @Override
    public Context getCurrentAppContext()
    {
        return questionsDisplayList.getContext();
    }
}

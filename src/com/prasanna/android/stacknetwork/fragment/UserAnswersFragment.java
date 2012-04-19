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

import com.prasanna.android.stacknetwork.QuestionDetailActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserAnswersIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserAnswersFragment extends ItemDisplayFragment<Answer>
{
    private static final String TAG = UserAnswersFragment.class.getSimpleName();
    private int answerDisplayCursor = 0;
    private LinearLayout layoutContainer;
    private ScrollViewWithNotifier scroller;
    private LinearLayout loadingProgressView;
    private User user;
    private Intent intent;
    private int page = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (items == null || items.isEmpty() == true)
        {
            user = (User) getActivity().getIntent().getSerializableExtra(StringConstants.USER);

            registerReceiver();

            startIntentService();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        answerDisplayCursor = 0;

        Log.d(TAG, "Creating answer fragment");

        showLoadingDialog();

        layoutContainer = (LinearLayout) inflater.inflate(R.layout.items_scroll_layout, null);
        scroller = (ScrollViewWithNotifier) layoutContainer.findViewById(R.id.itemScroller);
        scroller.addView(itemsContainer);
        scroller.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
        {
            @Override
            public void onScrollToBottom(View view)
            {
                if (loadingProgressView == null)
                {
                    loadingProgressView = (LinearLayout) getActivity().getLayoutInflater().inflate(
                            R.layout.loading_progress, null);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 15, 0, 15);
                    itemsContainer.addView(loadingProgressView, layoutParams);
                }

                startIntentService();
            }
        });

        if (items != null && items.isEmpty() == false)
        {
            displayItems();
        }

        return layoutContainer;
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
                    Intent intent = new Intent(getActivity(), QuestionDetailActivity.class);
                    Question question = new Question();
                    question.id = answer.questionId;
                    question.title = answer.title;
                    intent.putExtra(StringConstants.QUESTION, question);
                    intent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name(), true);
                    startActivity(intent);
                }
            });
            itemsContainer.addView(answerRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
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
        intent = getIntentForService(UserAnswersIntentService.class,
                IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
        intent.setAction(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
        intent.putExtra(StringConstants.USER_ID, user.id);
        intent.putExtra(StringConstants.PAGE, ++page);
        intent.putExtra(StringConstants.ACCESS_TOKEN, user.accessToken);
        getActivity().startService(intent);
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

        if (items != null && layoutContainer != null && itemsContainer != null)
        {
            Log.d(TAG, "Displaying already existing items: " + items.isEmpty());
            if (items.isEmpty())
            {
                TextView textView = (TextView) getActivity().getLayoutInflater().inflate(
                        R.layout.textview_black_textcolor, null);
                textView.setText("No answers by " + user.displayName);
                itemsContainer.addView(textView);
            }
            else
            {
                addAnswersToView();
            }
        }
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }
}

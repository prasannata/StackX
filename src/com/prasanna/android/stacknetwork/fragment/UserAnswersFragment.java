package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.QuestionDetailActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserAnswersIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserAnswersFragment extends Fragment
{
    private static final String TAG = UserAnswersFragment.class.getSimpleName();
    private int answerDisplayCursor = 0;
    private ProgressDialog progress;
    private LinearLayout questionsLayout;
    private ScrollViewWithNotifier questionsScroll;
    private ArrayList<Answer> answers = new ArrayList<Answer>();
    private LinearLayout loadingProgressView;
    private LinearLayout questionsDisplayList;
    private User user;
    private Intent intent;
    private int page = 0;

    private BroadcastReceiver answersByUserReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            answers.addAll((ArrayList<Answer>) intent
                    .getSerializableExtra(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.getExtra()));

            displayAnswers();

            Log.d(TAG, "Number of answers by user: " + answers.size());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        user = (User) getActivity().getIntent().getSerializableExtra(StringConstants.USER);

        registerForAnwersByUserReceiver();

        startUserAnswersService(user.id, user.accessToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        answerDisplayCursor = 0;

        Log.d(TAG, "Creating answer fragment");

        progress = ProgressDialog.show(getActivity(), "", "Loading answers");

        questionsLayout = (LinearLayout) inflater.inflate(R.layout.questions_layout, null);

        questionsScroll = (ScrollViewWithNotifier) questionsLayout.findViewById(R.id.questionsScroll);
        questionsDisplayList = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.fragment_questions,
                null);
        questionsScroll.addView(questionsDisplayList);

        questionsScroll.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
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
                    questionsDisplayList.addView(loadingProgressView, layoutParams);
                }

                startUserAnswersService(user.id, user.accessToken);
            }
        });

        return questionsLayout;
    }

    @Override
    public void onStop()
    {
        super.onStop();

        stopServiceAndUnregsiterReceivers();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        stopServiceAndUnregsiterReceivers();
    }

    private void stopServiceAndUnregsiterReceivers()
    {
        if (intent != null)
        {
            getActivity().stopService(intent);
        }

        try
        {
            getActivity().unregisterReceiver(answersByUserReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    private void displayAnswers()
    {
        if (progress != null)
        {
            progress.dismiss();
            progress = null;
        }

        if (loadingProgressView != null)
        {
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }

        if (answers != null && questionsLayout != null && questionsDisplayList != null)
        {
            if (answers.isEmpty())
            {
                TextView textView = (TextView) getActivity().getLayoutInflater().inflate(
                        R.layout.textview_black_textcolor, null);
                textView.setText("No answers by " + user.displayName);
                questionsDisplayList.addView(textView);
            }
            else
            {
                addAnswersToView();
            }
        }
    }

    private void addAnswersToView()
    {
        for (; answerDisplayCursor < answers.size(); answerDisplayCursor++)
        {
            final RelativeLayout answerRow = (RelativeLayout) getActivity().getLayoutInflater().inflate(
                    R.layout.user_item_row, null);
            final Answer answer = answers.get(answerDisplayCursor);
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
            questionsDisplayList.addView(answerRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }
    }

    private void startUserAnswersService(long userId, String accessToken)
    {
        intent = new Intent(getActivity(), UserAnswersIntentService.class);
        intent.setAction(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
        intent.putExtra(StringConstants.USER_ID, userId);
        intent.putExtra(StringConstants.PAGE, ++page);
        intent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
        getActivity().startService(intent);
    }

    private void registerForAnwersByUserReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(answersByUserReceiver, filter);
    }
}

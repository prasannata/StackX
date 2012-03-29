package com.prasanna.android.stacknetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.listener.FlingActionListener;
import com.prasanna.android.stacknetwork.intent.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.HtmlTagFragmenter;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.FlingScrollView;

public class QuestionDetailActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = QuestionDetailActivity.class.getSimpleName();
    private Intent questionIntent;
    private LinearLayout detailLinearLayout;
    private TextView answersOrQuestion;
    private TextView currentAnswerOfTotalTextView;
    private ImageView acceptedAnswerLogo;
    private Question question;
    private List<Answer> answers;
    int currentAnswerCount = -1;
    private FlingScrollView flingScrollView;
    private boolean viewingAnswer = false;
    private TextView currentAnswerAuthor;
    private RelativeLayout answerHeader;

    private class QuestionDetailActivityFlingActionListenerImpl implements FlingActionListener
    {
        public void flingedToLeft()
        {
            Log.d(TAG, "Flinged to left");
            if (viewingAnswer && currentAnswerCount < answers.size() - 1)
            {
                ++currentAnswerCount;
                updateViewForAnswer(question.getAnswers().get(currentAnswerCount).getBody(), (currentAnswerCount + 1)
                        + " of " + question.getAnswerCount(), question.getAnswers().get(currentAnswerCount).getOwner()
                        .getDisplayName(), answers.get(currentAnswerCount).isAccepted());
            }
        }

        public void flingedToRight()
        {
            Log.d(TAG, "Fling to right: " + currentAnswerCount);
            if (currentAnswerCount > 0)
            {
                --currentAnswerCount;
                updateViewForAnswer(question.getAnswers().get(currentAnswerCount).getBody(), (currentAnswerCount + 1)
                        + " of " + question.getAnswerCount(), question.getAnswers().get(currentAnswerCount).getOwner()
                        .getDisplayName(), answers.get(currentAnswerCount).isAccepted());
            }
        }

        private void updateViewForAnswer(String body, String textLabel, String author, boolean isAccepted)
        {
            detailLinearLayout.removeAllViews();
            displayQuestionBody(body);
            currentAnswerOfTotalTextView.setText(textLabel);
            currentAnswerAuthor.setText(author);

            if (isAccepted)
            {
                acceptedAnswerLogo.setVisibility(View.VISIBLE);
            }
            else
            {
                acceptedAnswerLogo.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.question_detail_layout);

        flingScrollView = (FlingScrollView) findViewById(R.id.questionDisplayFlingScrollView);
        flingScrollView.flingActionListener = new QuestionDetailActivityFlingActionListenerImpl();
        detailLinearLayout = (LinearLayout) findViewById(R.id.questionAnswerDetail);
        answerHeader = (RelativeLayout) findViewById(R.id.answerHeader);
        acceptedAnswerLogo = (ImageView) findViewById(R.id.acceptedAnswerLogo);
        currentAnswerOfTotalTextView = (TextView) findViewById(R.id.currentAnswerOfTotal);
        currentAnswerAuthor = (TextView) findViewById(R.id.currentAnswerAuthor);
        currentAnswerAuthor.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                startActivity(IntentUtils.createUserProfileIntent(view, answers.get(currentAnswerCount).getOwner()
                        .getId()));
            }
        });

        displayQuestionMetaData((Question) getIntent().getSerializableExtra("question"));
        registerReceiverAndStartService();
    }

    private void registerReceiverAndStartService()
    {
        registerForQuestionByIdReceiver();

        startQuestionService();
    }

    private void startQuestionService()
    {
        question = (Question) getIntent().getSerializableExtra("question");
        questionIntent = new Intent(this, QuestionDetailsIntentService.class);
        questionIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_DETAILS.name());
        questionIntent.putExtra(StringConstants.QUESTION, question);
        startService(questionIntent);
    }

    private void registerForQuestionByIdReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_DETAILS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(getReceiver(), filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopServiceAndUnregsiterReceiver();
    }

    private void stopServiceAndUnregsiterReceiver()
    {
        if (questionIntent != null)
        {
            stopService(questionIntent);
        }

        // If I do not unregister, it is leaked. Good, that is right. If I
        // unregister, it throws
        // IllegalArgumentException saying receiver not registered. WTF!
        try
        {
            unregisterReceiver(getReceiver());
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        stopServiceAndUnregsiterReceiver();
    }

    private void displayQuestionMetaData(final Question question)
    {
        updateResponseCounts(question);

        TextView textView = (TextView) findViewById(R.id.questionScore);
        textView.setText(String.valueOf(question.getScore()));

        textView = (TextView) findViewById(R.id.questionTitle);
        textView.setText(Html.fromHtml(question.getTitle()));

        textView = (TextView) findViewById(R.id.questionOwner);
        textView.setText(getOwnerString(question.getOwner()));

        textView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                startActivity(IntentUtils.createUserProfileIntent(view, question.getOwner().getId()));
            }
        });

        textView = (TextView) findViewById(R.id.questionTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(question.getCreateDate()));

        textView = (TextView) findViewById(R.id.questionViews);
        textView.append(String.valueOf(question.getViewCount()));
    }

    private static String getOwnerString(User user)
    {
        String userDetails = " by " + Html.fromHtml(user.getDisplayName());
        userDetails += AppUtils.formatUserReputation(user.getReputation());

        if (user.getAcceptRate() != -1)
        {
            userDetails += " Accept%: " + user.getAcceptRate();
        }
        return userDetails;
    }

    private void updateResponseCounts(final Question question)
    {
        if (question.getAnswerCount() > 0)
        {
            answersOrQuestion = (TextView) findViewById(R.id.answers);
            answersOrQuestion.append(" (" + question.getAnswerCount() + ")");
            answersOrQuestion.setClickable(true);
        }
    }

    private void displayQuestionBody(String text)
    {
        try
        {
            ArrayList<TextView> detailFragments = HtmlTagFragmenter.parse(getApplicationContext(), text);
            for (TextView detailFragment : detailFragments)
            {
                detailLinearLayout.addView(detailFragment);
            }
        }
        catch (XmlPullParserException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void scrollViewToBottomNotifier()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh()
    {
        stopServiceAndUnregsiterReceiver();
        registerReceiverAndStartService();
    }

    @Override
    public View getActiveParentView()
    {
        return null;
    }

    @Override
    public void processReceiverIntent(Context context, Intent intent)
    {
        question = (Question) intent.getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_DETAILS
                .getExtra());

        answers = question.getAnswers();

        displayQuestionBody(question.getBody());

        setupAnswersOnClick();
    }

    private void setupAnswersOnClick()
    {
        if (answersOrQuestion != null)
        {
            answersOrQuestion.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    String label = null;
                    String body = null;

                    if (viewingAnswer == false)
                    {
                        label = getString(R.string.question);

                        if (currentAnswerCount == -1)
                        {
                            currentAnswerCount = 0;
                        }

                        Log.d(TAG, "Accepted: " + answers.get(currentAnswerCount).isAccepted());
                        viewingAnswer = true;
                        currentAnswerOfTotalTextView.setText((currentAnswerCount + 1) + " of "
                                + question.getAnswerCount());
                        currentAnswerAuthor.setText(getString(R.string.by)
                                + answers.get(currentAnswerCount).getOwner().getDisplayName()
                                + AppUtils.formatUserReputation(answers.get(currentAnswerCount).getOwner()
                                        .getReputation()));
                        answerHeader.setVisibility(View.VISIBLE);
                        body = answers.get(currentAnswerCount).getBody();

                        if (answers.get(currentAnswerCount).isAccepted())
                        {
                            acceptedAnswerLogo.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            acceptedAnswerLogo.setVisibility(View.GONE);
                        }
                    }
                    else
                    {
                        label = getString(R.string.answers) + " (" + question.getAnswerCount() + ")";
                        body = question.getBody();
                        viewingAnswer = false;

                        if (currentAnswerOfTotalTextView != null)
                        {
                            answerHeader.setVisibility(View.GONE);
                        }
                    }

                    detailLinearLayout.removeAllViews();
                    displayQuestionBody(body);
                    answersOrQuestion.setText(label);
                }
            });
        }
    }
}

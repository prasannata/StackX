package com.prasanna.android.stacknetwork;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.prasanna.android.listener.FlingActionListener;
import com.prasanna.android.stacknetwork.intent.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
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
    private TextView commentsCickableTextView;
    private TextView currentAnswerOfTotalTextView;
    private TextView currentAnswerScore;
    private ImageView acceptedAnswerLogo;
    private Question question;
    private FlingScrollView flingScrollView;
    private TextView currentAnswerAuthor;
    private RelativeLayout answerHeader;
    private PopupWindow pw;
    private View hrInQuestionTitle;
    private int currentAnswerCount = -1;
    private boolean viewingAnswer = false;
    private boolean fetchFullDetails;

    private BroadcastReceiver questionBodyReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            question = (Question) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.getExtra());

            if (fetchFullDetails == true)
            {
                displayQuestionMetaData(question);
            }

            displayBody(question.body);

            setupAnswersOnClick();
        }
    };

    private BroadcastReceiver questionCommentsReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int numComments = 0;

            question.comments = (ArrayList<Comment>) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.getExtra());

            if (question.comments != null)
            {
                numComments = question.comments.size();
            }

            commentsCickableTextView.setText("Comments (" + numComments + ")");

        }
    };

    private BroadcastReceiver questionAnswersReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            question.answers = (ArrayList<Answer>) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra());

            answersOrQuestion.setBackgroundResource(R.drawable.square_bottom_edges);
            answersOrQuestion.setTextColor(Color.WHITE);

            if (question.answers != null)
            {
                answersOrQuestion.setClickable(true);
                answersOrQuestion.setEnabled(true);
            }
        }
    };

    private class QuestionDetailActivityFlingActionListenerImpl implements FlingActionListener
    {
        public void flingedToLeft()
        {
            Log.d(TAG, "Flinged to left");

            if (viewingAnswer && question.answers != null && currentAnswerCount < question.answers.size() - 1)
            {
                ++currentAnswerCount;
                updateViewForAnswer();
            }
        }

        public void flingedToRight()
        {
            Log.d(TAG, "Flinged to right");

            if (currentAnswerCount > 0)
            {
                --currentAnswerCount;
                updateViewForAnswer();
            }
        }

        private void updateViewForAnswer()
        {
            detailLinearLayout.removeAllViews();
            displayBody(question.answers.get(currentAnswerCount).body);

            if (question.answers.get(currentAnswerCount).comments == null)
            {
                commentsCickableTextView.setText("Comments (0)");
            }
            else
            {
                commentsCickableTextView.setText("Comments ("
                        + question.answers.get(currentAnswerCount).comments.size() + ")");
            }

            currentAnswerOfTotalTextView.setText((currentAnswerCount + 1) + " of " + question.answerCount);
            currentAnswerAuthor.setText(Html.fromHtml(question.answers.get(currentAnswerCount).owner.displayName));
            currentAnswerScore.setText(getString(R.string.score) + ": "
                    + question.answers.get(currentAnswerCount).score);
            if (question.answers.get(currentAnswerCount).accepted == true)
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
        currentAnswerScore = (TextView) findViewById(R.id.currentAnswerScore);
        hrInQuestionTitle = findViewById(R.id.hrInQuestionTitle);
        currentAnswerAuthor.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                startActivity(IntentUtils.createUserProfileIntent(view.getContext(),
                        question.answers.get(currentAnswerCount).owner.id));
            }
        });

        setupCommentsPopup();

        registerReceivers();

        fetchQuestionDetail();
    }

    private void fetchQuestionDetail()
    {
        fetchFullDetails = getIntent().getBooleanExtra(
                IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name(), false);

        if (fetchFullDetails == false)
        {
            displayQuestionMetaData((Question) getIntent().getSerializableExtra(StringConstants.QUESTION));
            startQuestionService(IntentActionEnum.QuestionIntentAction.QUESTION_BODY.name());
        }
        else
        {
            startQuestionService(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name());
        }
    }

    private void setupCommentsPopup()
    {
        final RelativeLayout questionTitleLayout = (RelativeLayout) findViewById(R.id.questionTitleLayout);
        commentsCickableTextView = (TextView) findViewById(R.id.comments);
        commentsCickableTextView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (question != null && question.comments != null && question.comments.isEmpty() == false)
                {
                    final ScrollView commentsView = (ScrollView) getLayoutInflater().inflate(R.layout.popup_layout,
                            null);

                    LinearLayout commentsLayout = (LinearLayout) commentsView.findViewById(R.id.popupItemList);
                    ImageView closeCommentsPopup = (ImageView) commentsLayout.findViewById(R.id.closePopup);
                    closeCommentsPopup.setOnClickListener(new View.OnClickListener()
                    {

                        @Override
                        public void onClick(View v)
                        {
                            if (pw != null)
                            {
                                pw.dismiss();
                            }
                        }
                    });

                    List<Comment> comments = (viewingAnswer && currentAnswerCount != -1) ? question.answers
                            .get(currentAnswerCount).comments : question.comments;

                    if (comments != null)
                    {
                        for (Comment comment : comments)
                        {
                            RelativeLayout commentLayout = (RelativeLayout) getLayoutInflater().inflate(
                                    R.layout.comment_layout, null);
                            TextView textView = (TextView) commentLayout.findViewById(R.id.commentScore);
                            textView.setText(String.valueOf(comment.score));

                            textView = (TextView) commentLayout.findViewById(R.id.commentContent);
                            textView.setText(Html.fromHtml(comment.body));

                            textView = (TextView) commentLayout.findViewById(R.id.commentAuthor);
                            textView.setText(Html.fromHtml(comment.owner.displayName));

                            commentsLayout.addView(commentLayout, LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                        }

                        Point size = new Point();
                        getWindowManager().getDefaultDisplay().getSize(size);

                        pw = new PopupWindow(commentsView, size.x - 50, size.y - questionTitleLayout.getHeight() - 150,
                                true);
                        pw.showAsDropDown(hrInQuestionTitle, 10, 10);
                    }
                }
            }
        });
    }

    private void registerReceivers()
    {
        registerForQuestionBodyReceiver();

        registerForQuestionCommentsReceiver();

        registerForQuestionAnswersReceiver();
    }

    private void startQuestionService(String intentAction)
    {
        questionIntent = new Intent(this, QuestionDetailsIntentService.class);
        questionIntent.setAction(intentAction);
        questionIntent.putExtra(StringConstants.QUESTION,
                (Question) getIntent().getSerializableExtra(StringConstants.QUESTION));
        startService(questionIntent);
    }

    private void registerForQuestionBodyReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(questionBodyReceiver, filter);
    }

    private void registerForQuestionCommentsReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(questionCommentsReceiver, filter);
    }

    private void registerForQuestionAnswersReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(questionAnswersReceiver, filter);
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
            if (questionBodyReceiver != null)
            {
                unregisterReceiver(questionBodyReceiver);
            }

            if (questionAnswersReceiver != null)
            {
                unregisterReceiver(questionAnswersReceiver);
            }

            if (questionCommentsReceiver != null)
            {
                unregisterReceiver(questionCommentsReceiver);
            }

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
        updateAnswerCount(question);

        TextView textView = (TextView) findViewById(R.id.questionScore);
        textView.setText("Score: " + String.valueOf(question.score));

        textView = (TextView) findViewById(R.id.questionTitle);
        textView.setText(Html.fromHtml(question.title));

        textView = (TextView) findViewById(R.id.questionOwner);
        textView.setText(getOwnerString(question.owner));

        textView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                startActivity(IntentUtils.createUserProfileIntent(view.getContext(), question.owner.id));
            }
        });

        textView = (TextView) findViewById(R.id.questionTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(question.creationDate));

        textView = (TextView) findViewById(R.id.questionViews);
        textView.append(String.valueOf(question.viewCount));
    }

    private static String getOwnerString(User user)
    {
        String userDetails = " by " + Html.fromHtml(user.displayName);
        userDetails += AppUtils.formatUserReputation(user.reputation);

        if (user.acceptRate != -1)
        {
            userDetails += " Accept%: " + user.acceptRate;
        }
        return userDetails;
    }

    private void updateAnswerCount(final Question question)
    {
        answersOrQuestion = (TextView) findViewById(R.id.answers);
        answersOrQuestion.append(" (" + question.answerCount + ")");
        answersOrQuestion.setClickable(false);
        answersOrQuestion.setEnabled(false);
    }

    private void displayBody(String text)
    {
        ArrayList<TextView> detailFragments = HtmlTagFragmenter.parse(getApplicationContext(), text);
        for (TextView detailFragment : detailFragments)
        {
            detailLinearLayout.addView(detailFragment);
        }
    }

    @Override
    public void refresh()
    {
        stopServiceAndUnregsiterReceiver();
        registerReceivers();
    }

    @Override
    public Context getCurrentAppContext()
    {
        return getApplicationContext();
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

                        Log.d(TAG, "Accepted: " + question.answers.get(currentAnswerCount).accepted);
                        viewingAnswer = true;
                        currentAnswerOfTotalTextView.setText((currentAnswerCount + 1) + " of " + question.answerCount);
                        currentAnswerAuthor.setText(getString(R.string.by)
                                + question.answers.get(currentAnswerCount).owner.displayName
                                + AppUtils.formatUserReputation(question.answers.get(currentAnswerCount).owner.reputation));
                        currentAnswerScore.setText(String.valueOf(question.answers.get(currentAnswerCount).score));

                        if (question.answers.get(currentAnswerCount).comments == null)
                        {
                            commentsCickableTextView.setText("Comments (0)");
                        }
                        else
                        {
                            commentsCickableTextView.setText("Comments ("
                                    + question.answers.get(currentAnswerCount).comments.size() + ")");
                        }

                        answerHeader.setVisibility(View.VISIBLE);
                        body = question.answers.get(currentAnswerCount).body;

                        if (question.answers.get(currentAnswerCount).accepted)
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
                        label = getString(R.string.answers) + " (" + question.answerCount + ")";
                        body = question.body;
                        viewingAnswer = false;

                        if (currentAnswerOfTotalTextView != null)
                        {
                            answerHeader.setVisibility(View.GONE);
                        }

                        if (question.comments == null)
                        {
                            commentsCickableTextView.setText("Comments (0)");
                        }
                        else
                        {
                            commentsCickableTextView.setText("Comments (" + question.comments.size() + ")");
                        }
                    }

                    detailLinearLayout.removeAllViews();
                    displayBody(body);
                    answersOrQuestion.setText(label);
                }
            });
        }
    }
}

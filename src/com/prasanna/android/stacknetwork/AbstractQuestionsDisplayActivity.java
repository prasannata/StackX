package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.views.ScrollViewWithNotifier;

public abstract class AbstractQuestionsDisplayActivity extends AbstractUserActionBarActivity
{
    protected boolean serviceRunning = false;

    protected Intent questionsIntent;

    protected ProgressDialog fetchingQuestionsDialog;

    protected ScrollViewWithNotifier scrollView;

    protected LinearLayout questionsLinearLayout;

    protected LinearLayout loadingProgressView;

    protected ArrayList<Question> questions = new ArrayList<Question>();

    protected int lastDisplayQuestionIndex = 0;

    protected abstract void startQuestionsService();

    protected abstract void registerQuestionsReceiver();

    protected abstract void onScrollToBottom();

    protected abstract String getLogTag();

    protected abstract QuestionIntentAction getReceiverIntentAction();

    protected int page = 0;

    protected ArrayList<String> tags = new ArrayList<String>();

    protected BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            questions.addAll((ArrayList<Question>) intent.getSerializableExtra(getReceiverIntentAction().getExtra()));

            Log.d(getLogTag(), "Questions received: " + questions.size());

            processQuestions();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.items_scroll_layout);

        scrollView = (ScrollViewWithNotifier) findViewById(R.id.itemScroller);
        scrollView.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
        {
            @Override
            public void onScrollToBottom(View view)
            {
                AbstractQuestionsDisplayActivity.this.onScrollToBottom();
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void loadIfLastInstanceWasSaved(Object lastSavedObject)
    {
        if (lastSavedObject == null)
        {
            startQuestionsService();
        }
        else
        {
            questions = (ArrayList<Question>) lastSavedObject;
            page = questions.size() / Integer.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE);
            processQuestions();
        }
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

    protected void stopServiceAndUnregisterReceiver()
    {
        if (questionsIntent != null)
        {
            stopService(questionsIntent);
        }

        try
        {
            unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(getLogTag(), e.getMessage());
        }
    }

    protected void processQuestions()
    {
        if (fetchingQuestionsDialog != null)
        {
            fetchingQuestionsDialog.dismiss();
            fetchingQuestionsDialog = null;
        }

        if (loadingProgressView != null)
        {
            questionsLinearLayout.removeView(loadingProgressView);
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }

        for (; lastDisplayQuestionIndex < questions.size(); lastDisplayQuestionIndex++)
        {
            LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(getLayoutInflater(), this,
                    questions.get(lastDisplayQuestionIndex));
            questionsLinearLayout.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }

        serviceRunning = false;
    }
}

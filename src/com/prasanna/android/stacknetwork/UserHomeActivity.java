package com.prasanna.android.stacknetwork;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.LayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserHomeActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = UserHomeActivity.class.getSimpleName();

    private Boolean serviceRunning = Boolean.FALSE;

    private int page = 0;

    static class GzipDecompressingEntity extends HttpEntityWrapper
    {
        public GzipDecompressingEntity(final HttpEntity entity)
        {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException
        {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength()
        {
            return -1;
        }
    }

    private Intent questionsIntent;

    private ProgressDialog progressDialog;

    private LinearLayout masterLinearLayout;

    private ScrollViewWithNotifier scrollView;

    private LinearLayout questionsLinearLayout;

    private LinearLayout loadingProgressView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.questions_layout);

        masterLinearLayout = (LinearLayout) findViewById(R.id.homeDisplay);
        scrollView = (ScrollViewWithNotifier) masterLinearLayout.findViewById(R.id.questionsScroll);
        questionsLinearLayout = (LinearLayout) scrollView.findViewById(R.id.questionsDisplay);
        scrollView.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
        {
            @Override
            public void onScrollToBottom(View view)
            {
                if (serviceRunning.equals(Boolean.FALSE))
                {
                    loadingProgressView = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress, null);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 15, 0, 15);
                    questionsLinearLayout.addView(loadingProgressView, layoutParams);
                    startQuestionsService();
                }
            }
        });

        // setSiteHeader();

        startReceiverAndService();
    }

    private void startReceiverAndService()
    {
        progressDialog = ProgressDialog.show(UserHomeActivity.this, "", getString(R.string.loading));

        registerQuestionsReceiver();

        startQuestionsService();
    }

    private void startQuestionsService()
    {
        questionsIntent = new Intent(this, UserQuestionsIntentService.class);
        questionsIntent.setAction(IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.name());
        questionsIntent.putExtra(StringConstants.PAGE, ++page);
        startService(questionsIntent);
        serviceRunning = Boolean.TRUE;
    }

    private void registerQuestionsReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(getReceiver(), filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopServiceAndUnregisterReceiver();
    }

    private void stopServiceAndUnregisterReceiver()
    {
        if (questionsIntent != null)
        {
            stopService(questionsIntent);
        }

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
    public void onStop()
    {
        super.onStop();

        stopServiceAndUnregisterReceiver();
    }

    private void processQuestions(ArrayList<Question> questions)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (loadingProgressView != null)
        {
            questionsLinearLayout.removeView(loadingProgressView);
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }

        for (final Question question : questions)
        {
            LinearLayout questionLayout = LayoutBuilder.getInstance().buildQuestionSnippet(this, question);
            questionsLinearLayout.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }

        serviceRunning = Boolean.FALSE;
    }

    @Override
    public void refresh()
    {
        stopServiceAndUnregisterReceiver();
        questionsLinearLayout.removeAllViews();
        startReceiverAndService();
    }

    @Override
    public Context getCurrentAppContext()
    {
        return getApplicationContext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processReceiverIntent(Context context, Intent intent)
    {
        ArrayList<Question> questions = (ArrayList<Question>) intent
                .getSerializableExtra(IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.getExtra());
        processQuestions(questions);
    }
}

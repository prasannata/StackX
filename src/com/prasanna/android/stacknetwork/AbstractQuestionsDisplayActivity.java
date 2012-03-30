package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.LayoutBuilder;
import com.prasanna.android.views.ScrollViewWithNotifier;

public abstract class AbstractQuestionsDisplayActivity extends AbstractUserActionBarActivity
{
    protected boolean serviceRunning = false;

    protected Intent questionsIntent;

    protected ProgressDialog fetchingQuestionsDialog;

    protected LinearLayout masterLinearLayout;

    protected ScrollViewWithNotifier scrollView;

    protected LinearLayout questionsLinearLayout;

    protected LinearLayout loadingProgressView;

    protected abstract void startQuestionsService();

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
                if (serviceRunning == false)
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
    }

    protected void processQuestions(ArrayList<Question> questions)
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

        for (final Question question : questions)
        {
            LinearLayout questionLayout = LayoutBuilder.getInstance().buildQuestionSnippet(this, question);
            questionsLinearLayout.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }

        serviceRunning = false;
    }

}

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
    protected abstract void startQuestionsService();

    protected abstract void registerQuestionsSearchReceiver();

    protected abstract void onScrollToBottom();

    protected abstract String getLogTag();

    protected abstract QuestionIntentAction getReceiverIntentAction();

    protected boolean serviceRunning = false;

    protected Intent questionsIntent;

    protected ProgressDialog fetchingQuestionsDialog;

    protected ScrollViewWithNotifier scrollView;

    protected LinearLayout questionsLinearLayout;

    protected LinearLayout loadingProgressView;

    protected ArrayList<Question> questions = new ArrayList<Question>();

    protected int lastDisplayQuestionIndex = 0;

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
    protected void onStop()
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
	dismissLoadingProgressDialog();

	dismissLoadingQuestionsProgressView();

	for (; lastDisplayQuestionIndex < questions.size(); lastDisplayQuestionIndex++)
	{
	    LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(getLayoutInflater(), this,
		            false, questions.get(lastDisplayQuestionIndex));
	    questionsLinearLayout.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT));
	}

	serviceRunning = false;
    }

    protected void dismissLoadingProgressDialog()
    {
	if (fetchingQuestionsDialog != null)
	{
	    fetchingQuestionsDialog.dismiss();
	    fetchingQuestionsDialog = null;
	}
    }

    protected void dismissLoadingQuestionsProgressView()
    {
	if (loadingProgressView != null)
	{
	    questionsLinearLayout.removeView(loadingProgressView);
	    loadingProgressView.setVisibility(View.GONE);
	    loadingProgressView = null;
	}
    }
}

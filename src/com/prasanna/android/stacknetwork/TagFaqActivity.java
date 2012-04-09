package com.prasanna.android.stacknetwork;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.prasanna.android.stacknetwork.intent.TagFaqIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class TagFaqActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = TagFaqActivity.class.getSimpleName();
    private String tag;
    private int page = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	Object lastSavedObject = null;
	if (savedInstanceState != null)
	{
	    lastSavedObject = savedInstanceState.getSerializable(StringConstants.QUESTIONS);
	}

	registerQuestionsReceiver();

	loadIfLastInstanceWasSaved(lastSavedObject);
    }

    @Override
    protected void startQuestionsService()
    {
	tag = getIntent().getStringExtra(QuestionIntentAction.TAGS_FAQ.getExtra());

	if (tag == null)
	{
	    finish();
	}

	questionsIntent = new Intent(this, TagFaqIntentService.class);
	questionsIntent.setAction(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
	questionsIntent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), tag);
	questionsIntent.putExtra(StringConstants.PAGE, ++page);
	startService(questionsIntent);
	serviceRunning = true;
    }

    @Override
    public void refresh()
    {
	// TODO Auto-generated method stub

    }

    @Override
    public Context getCurrentAppContext()
    {
	return TagFaqActivity.this;
    }

    @Override
    protected void registerQuestionsReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(receiver, filter);
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    @Override
    protected QuestionIntentAction getIntentAction()
    {
	return QuestionIntentAction.TAGS_FAQ;
    }
}

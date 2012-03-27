package com.prasanna.android.stacknetwork.intent;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionDetailsIntentService extends IntentService
{
    private QuestionService questionService = QuestionService.getInstance();

    public QuestionDetailsIntentService()
    {
	this("QuestionService");
    }

    public QuestionDetailsIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	Question question = (Question) intent.getSerializableExtra(StringConstants.QUESTION);
	if (question != null && question.getId() > 0)
	{
	    question.setBody(questionService.getQuestionBodyForId(question.getId()));
	    if (question.getAnswerCount() > 0)
	    {
		question.setAnswers(questionService.getAnswersForQuestion(question.getId()));
	    }
	}

	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_DETAILS.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_DETAILS.getExtra(),
	        question);
	sendBroadcast(broadcastIntent);
    }
}

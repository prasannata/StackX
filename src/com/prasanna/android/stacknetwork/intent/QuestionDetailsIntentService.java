package com.prasanna.android.stacknetwork.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
	Log.d("QuestionDetailsIntentService", "Action: " + intent.getAction());

	Question question = (Question) intent.getSerializableExtra(StringConstants.QUESTION);

	Intent broadcastIntent = null;

	if (question != null && question.id > 0)
	{
	    String action = intent.getAction();
	    if (action != null)
	    {
		if (action.equals(IntentActionEnum.QuestionIntentAction.QUESTION_BODY.name()))
		{
		    question.body = questionService.getQuestionBodyForId(question.id);
		}
		else
		{
		    question = questionService.getQuestionFullDetails(question.id);
		}

		broadcastIntent = new Intent();
		broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name());
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.getExtra(),
		                question);
		sendBroadcast(broadcastIntent);

		broadcastIntent = new Intent();
		broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.name());
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.getExtra(),
		                questionService.getComments(StringConstants.QUESTIONS, question.id));
		sendBroadcast(broadcastIntent);

		if (question.answerCount > 0)
		{
		    question.answers = questionService.getAnswersForQuestion(question.id);

		    broadcastIntent = new Intent();
		    broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.name());
		    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		    broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra(),
			            question.answers);
		    sendBroadcast(broadcastIntent);
		}
	    }
	}
    }
}

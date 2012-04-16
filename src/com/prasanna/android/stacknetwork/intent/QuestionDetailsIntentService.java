package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.service.QuestionsLRUService;
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

        if (question != null && question.id > 0)
        {
            String action = intent.getAction();
            if (action != null)
            {
                Question cachedQuestion = QuestionsLRUService.get(question.id);
                if (cachedQuestion == null)
                {
                    getFromServer(question, action);
                }
                else
                {
                    broadcastQuestionBody(cachedQuestion);

                    broadcoastQuestionComments(cachedQuestion.comments);

                    broadcastQuestionAnswers(cachedQuestion);
                }
            }
        }
    }

    private void getFromServer(Question question, String action)
    {
        if (action.equals(IntentActionEnum.QuestionIntentAction.QUESTION_BODY.name()))
        {
            question.body = questionService.getQuestionBodyForId(question.id);
        }
        else
        {
            question = questionService.getQuestionFullDetails(question.id);
        }

        broadcastQuestionBody(question);

        broadcoastQuestionComments(questionService.getComments(StringConstants.QUESTIONS, question.id));

        if (question.answerCount > 0)
        {
            question.answers = questionService.getAnswersForQuestion(question.id);
        }

        broadcastQuestionAnswers(question);

        QuestionsLRUService.add(question);
    }

    private void broadcastQuestionAnswers(Question question)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra(), question.answers);
        sendBroadcast(broadcastIntent);
    }

    private void broadcoastQuestionComments(ArrayList<Comment> comments)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.getExtra(), comments);
        sendBroadcast(broadcastIntent);
    }

    private void broadcastQuestionBody(Question question)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.getExtra(), question);
        sendBroadcast(broadcastIntent);
    }
}

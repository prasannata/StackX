/*
    Copyright 2012 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.service.QuestionsLRUService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StackUri;
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

                    broadcoastComments(cachedQuestion.comments);

                    broadcastAnswers(cachedQuestion.answers);
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

        broadcoastComments(questionService.getComments(StringConstants.QUESTIONS, question.id));

        if (question.answers == null)
        {
            question.answers = new ArrayList<Answer>();
        }

        if (question.answerCount > 0)
        {
            int numPages = (question.answerCount / StackUri.QueryParamDefaultValues.ANSWERS_PAGE_SIZE) + 1;
            for (int page = 1; page <= numPages; page++)
            {
                ArrayList<Answer> answers = questionService.getAnswersForQuestion(question.id, page);
                question.answers.addAll(answers);
                broadcastAnswers(answers);
            }
        }
        else
        {
            broadcastAnswers(question.answers);
        }

        QuestionsLRUService.add(question);
    }

    private void broadcastAnswers(ArrayList<Answer> answers)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra(), answers);
        sendBroadcast(broadcastIntent);
    }

    private void broadcoastComments(ArrayList<Comment> comments)
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

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class TagFaqIntentService extends IntentService
{
    private QuestionService questionService = QuestionService.getInstance();

    public TagFaqIntentService()
    {
        this(TagFaqIntentService.class.getSimpleName());
    }

    public TagFaqIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String tag = intent.getStringExtra(QuestionIntentAction.TAGS_FAQ.getExtra());
        int page = intent.getIntExtra(StringConstants.PAGE, 1);

        ArrayList<Question> questions = questionService.getFaqForTag(tag, page);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(StringConstants.QUESTIONS, questions);
        sendBroadcast(broadcastIntent);
    }

}

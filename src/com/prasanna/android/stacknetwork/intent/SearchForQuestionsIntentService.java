package com.prasanna.android.stacknetwork.intent;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class SearchForQuestionsIntentService extends IntentService
{
    private static final String TAG = SearchForQuestionsIntentService.class.getSimpleName();

    public SearchForQuestionsIntentService()
    {
        this("SearchForQuestionsIntentService");
    }

    public SearchForQuestionsIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String query = intent.getStringExtra(SearchManager.QUERY);
        int page = intent.getIntExtra(StringConstants.PAGE, 1);

        Log.d(TAG, "Received search query: " + query);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.getExtra(), QuestionService
                .getInstance().search(query, page));
        sendBroadcast(broadcastIntent);
    }
}

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserQuestionsIntentService extends IntentService
{
    private static final String TAG = UserQuestionsIntentService.class.getSimpleName();

    private UserService userService = UserService.getInstance();

    public UserQuestionsIntentService()
    {
        this("UserQuestionsService");
    }

    public UserQuestionsIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        int page = intent.getIntExtra(StringConstants.PAGE, 1);
        String action = intent.getAction();
        String accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);

        if (accessToken == null)
        {
            long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
            if (userId == -1)
            {
                getFrontPageQuestionsAndBroadcast(null, page);
            }
            else
            {
                broadcastIntent(userService.getQuestionsByUser(userId, page));
            }
        }
        else
        {
            if (action.equals(UserIntentAction.QUESTIONS_BY_USER.name()))
            {
                broadcastIntent(userService.getMyQuestions(page));
            }
            else
            {
                getFrontPageQuestionsAndBroadcast(accessToken, page);
            }
        }
    }

    private void getFrontPageQuestionsAndBroadcast(String accessToken, int page)
    {
        ArrayList<Question> questions = userService.getAllQuestions(page);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTIONS.getExtra(), questions);
        sendBroadcast(broadcastIntent);

        Log.d(TAG, "Questions fetched and broadcasted");
    }

    private void broadcastIntent(ArrayList<Question> questions)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.getExtra(), questions);
        sendBroadcast(broadcastIntent);
    }
}

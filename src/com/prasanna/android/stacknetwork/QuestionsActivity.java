package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionsActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = QuestionsActivity.class.getSimpleName();

    private int page = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            questions.addAll((ArrayList<Question>) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTIONS.getExtra()));

            processQuestions();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Object lastSavedObject = null;
        if (savedInstanceState != null)
        {
            lastSavedObject = savedInstanceState.getSerializable(StringConstants.QUESTIONS);
        }

        startReceiverAndService(lastSavedObject);
    }

    @SuppressWarnings("unchecked")
    private void startReceiverAndService(Object lastSavedObject)
    {
        registerQuestionsReceiver();

        if (lastSavedObject == null)
        {
            fetchingQuestionsDialog = ProgressDialog.show(QuestionsActivity.this, "", getString(R.string.loading));

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
    protected void startQuestionsService()
    {
        questionsIntent = new Intent(this, UserQuestionsIntentService.class);
        questionsIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        questionsIntent.putExtra(StringConstants.PAGE, ++page);
        startService(questionsIntent);
        serviceRunning = true;
    }

    private void registerQuestionsReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopServiceAndUnregisterReceiver();
    }

    private void stopServiceAndUnregisterReceiver()
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
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        stopServiceAndUnregisterReceiver();
    }

    @Override
    public void refresh()
    {
        stopServiceAndUnregisterReceiver();
        questionsLinearLayout.removeAllViews();
        startReceiverAndService(null);
    }

    @Override
    public Context getCurrentAppContext()
    {
        return getApplicationContext();
    }
}

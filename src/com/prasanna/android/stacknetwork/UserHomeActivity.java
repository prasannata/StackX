package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserHomeActivity extends AbstractQuestionsDisplayActivity
{
    private static final String TAG = UserHomeActivity.class.getSimpleName();

    private int page = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ArrayList<Question> questions = (ArrayList<Question>) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTIONS.getExtra());
            processQuestions(questions);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        startReceiverAndService();
    }

    private void startReceiverAndService()
    {
        fetchingQuestionsDialog = ProgressDialog.show(UserHomeActivity.this, "", getString(R.string.loading));

        registerQuestionsReceiver();

        startQuestionsService();
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
        startReceiverAndService();
    }

    @Override
    public Context getCurrentAppContext()
    {
        return getApplicationContext();
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        if (serviceRunning == false)
        {
            Intent intent = new Intent(this, QuestionSearchResultsActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String paramString)
    {
        // TODO Auto-generated method stub
        return false;
    }
}

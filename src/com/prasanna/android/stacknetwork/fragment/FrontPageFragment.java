package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class FrontPageFragment extends QuestionsFragment
{
    private static final String TAG = FrontPageFragment.class.getSimpleName();

    private Intent frontPageQuestionsIntent;

    private int currentPage = 0;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        registerReceiver();

        if (savedInstanceState == null)
        {
            Log.d(TAG, "onCreate not savedInstanceState");
            loadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

            startIntentService();
        }
        else
        {
            Log.d(TAG, "onCreate savedInstanceState");
            questions = (ArrayList<Question>) savedInstanceState.getSerializable(StringConstants.QUESTIONS);
            displayQuestions();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "Saving instance state");
        if (questions != null && questions.isEmpty() == false)
        {
            outState.putSerializable(StringConstants.QUESTIONS, questions);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void startIntentService()
    {
        frontPageQuestionsIntent = new Intent(getActivity().getApplicationContext(), UserQuestionsIntentService.class);
        frontPageQuestionsIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        frontPageQuestionsIntent.putExtra(StringConstants.PAGE, ++currentPage);
        getActivity().startService(frontPageQuestionsIntent);
        serviceRunning = true;
    }

    private void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public QuestionIntentAction getReceiverExtraName()
    {
        return QuestionIntentAction.QUESTIONS;
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }
}

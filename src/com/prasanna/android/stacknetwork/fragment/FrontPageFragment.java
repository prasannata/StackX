package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class FrontPageFragment extends AbstractQuestionsFragment
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

            showLoadingDialog();

            startIntentService();
        }
        else
        {
            Log.d(TAG, "onCreate savedInstanceState");
            items = (ArrayList<Question>) savedInstanceState.getSerializable(StringConstants.QUESTIONS);
            displayItems();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "Saving instance state");
        if (items != null && items.isEmpty() == false)
        {
            outState.putSerializable(StringConstants.QUESTIONS, items);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void startIntentService()
    {
        frontPageQuestionsIntent = getIntentForService(UserQuestionsIntentService.class,
                IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        frontPageQuestionsIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        frontPageQuestionsIntent.putExtra(StringConstants.PAGE, ++currentPage);
        getActivity().startService(frontPageQuestionsIntent);
        serviceRunning = true;
    }

    @Override
    protected void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTIONS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }
}

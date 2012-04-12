package com.prasanna.android.stacknetwork.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.TagFaqIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class TagFaqFragment extends QuestionsFragment
{
    private static final String TAG = TagFaqFragment.class.getSimpleName();

    private int currentPage = 0;

    private String qTag;

    private Intent tagFaqIntent;

    public TagFaqFragment()
    {
        currentPage = 0;
        questions.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (qTag != null)
        {
            registerTagFaqReceiver();

            loadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
            
            startIntentService();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (questions != null && questions.isEmpty() == false)
        {
            outState.putSerializable(StringConstants.QUESTIONS, questions);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void startIntentService()
    {
        tagFaqIntent = new Intent(getActivity().getApplicationContext(), TagFaqIntentService.class);
        tagFaqIntent.setAction(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
        tagFaqIntent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), qTag);
        tagFaqIntent.putExtra(StringConstants.PAGE, ++currentPage);
        getActivity().startService(tagFaqIntent);
        serviceRunning = true;
    }

    protected void registerTagFaqReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(receiver, filter);
    }

    public String getqTag()
    {
        return qTag;
    }

    public void setqTag(String qTag)
    {
        this.qTag = qTag;
    }

    @Override
    public QuestionIntentAction getReceiverExtraName()
    {
        return QuestionIntentAction.TAGS_FAQ;
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }
}

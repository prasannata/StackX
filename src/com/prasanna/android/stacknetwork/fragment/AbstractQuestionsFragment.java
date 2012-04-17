package com.prasanna.android.stacknetwork.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;

public abstract class AbstractQuestionsFragment extends ItemDisplayFragment<Question>
{
    private int itemDisplayCursor = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        itemDisplayCursor = 0;

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void displayItems()
    {
        if (loadingDialog != null)
        {
            loadingDialog.dismiss();
            loadingDialog = null;
        }

        if (loadingProgressView != null)
        {
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }
        
        Log.d(getLogTag(), "questions size: " + items.size() + ", lastDisplayQuestionIndex: " + itemDisplayCursor);

        for (; itemDisplayCursor < items.size(); itemDisplayCursor++)
        {
            LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(
                    getActivity().getLayoutInflater(), getActivity(), false,items.get(itemDisplayCursor));
            itemsContainer.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }

        serviceRunning = false;
    }
}

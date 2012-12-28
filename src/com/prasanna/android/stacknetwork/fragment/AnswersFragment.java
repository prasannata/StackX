package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.listener.FlingActionListener;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.utils.HtmlTagFragmenter;
import com.prasanna.android.views.FlingScrollView;

public class AnswersFragment extends Fragment
{
    private static final String TAG = QuestionFragment.class.getSimpleName();
    private FlingScrollView parentLayout;
    private ArrayList<Answer> answers;
    private int currentAnswerCount = 0;
    private LinearLayout answerBodyLayout;;

    private class AnswerFlingActionListenerImpl implements FlingActionListener
    {
        public void flingedToLeft()
        {
            Log.d(TAG, "Flinged to left");

            if (answers != null && currentAnswerCount < answers.size() - 1)
            {
                ++currentAnswerCount;
                displayBody();
            }
        }

        public void flingedToRight()
        {
            Log.d(TAG, "Flinged to right");

            if (currentAnswerCount > 0)
            {
                --currentAnswerCount;
                displayBody();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (parentLayout == null)
        {
            parentLayout = (FlingScrollView) inflater.inflate(R.layout.answers, null);
            parentLayout.flingActionListener = new AnswerFlingActionListenerImpl();

            answerBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.answerBody);
        }

        displayAnswers();

        return parentLayout;
    }

    public void displayAnswers()
    {
        parentLayout.flingActionListener = new AnswerFlingActionListenerImpl();
        answerBodyLayout.removeAllViews();

        if (answers != null && !answers.isEmpty())
        {
            displayBody();
        }
    }

    private void displayBody()
    {
        ArrayList<TextView> questionBodyTextViews = HtmlTagFragmenter.parse(getActivity(),
                answers.get(currentAnswerCount).body);
        for (TextView questionBodyTextView : questionBodyTextViews)
        {
            answerBodyLayout.addView(questionBodyTextView);
        }
    }

    public void setAnswers(ArrayList<Answer> answers)
    {
        Log.d(TAG, "answers set");
        this.answers = answers;
    }

}

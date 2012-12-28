/*
    Copyright (C) 2012 Prasanna Thirumalai
    
    This file is part of StackX.

    StackX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    StackX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with StackX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;

public class AnswerFragment extends Fragment
{
    private static final String TAG = QuestionFragment.class.getSimpleName();
    private LinearLayout parentLayout;
    private LinearLayout answerBodyLayout;

    private Answer answer;;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (savedInstanceState == null)
        {
            parentLayout = (LinearLayout) inflater.inflate(R.layout.answers, null);
            answerBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.answerBody);

            if (answer != null)
                displayAnswer();
        }
        return parentLayout;
    }

    private void displayAnswer()
    {
        RelativeLayout answerMetaInfoLayout = (RelativeLayout) parentLayout.findViewById(R.id.answerMetaInfo);

        TextView textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerScore);
        textView.setText(AppUtils.formatNumber(answer.score));

        textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerAuthor);
        textView.setText(DateTimeUtils.getElapsedDurationSince(answer.creationDate) + " by " + answer.owner.displayName);

        ArrayList<TextView> answerBodyTextViews = MarkdownFormatter.format(getActivity(), answer.body);
        for (TextView answer : answerBodyTextViews)
        {
            answerBodyLayout.addView(answer);
        }
    }

    public void setAnswer(Answer answer)
    {
        Log.d(TAG, "answer set");

        this.answer = answer;
    }

}

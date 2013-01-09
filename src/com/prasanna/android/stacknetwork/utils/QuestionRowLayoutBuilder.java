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

package com.prasanna.android.stacknetwork.utils;

import android.content.Context;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Question;

public class QuestionRowLayoutBuilder
{
    private static final String TAG = QuestionRowLayoutBuilder.class.getSimpleName();

    private static QuestionRowLayoutBuilder builder = new QuestionRowLayoutBuilder();

    private QuestionRowLayoutBuilder()
    {
    }

    public static QuestionRowLayoutBuilder getInstance()
    {
        return builder;
    }

    public LinearLayout build(final LayoutInflater layoutInflater, final Context context, final Question question)
    {
        LinearLayout questionRowLayout = (LinearLayout) layoutInflater.inflate(R.layout.question_snippet_layout, null);

        questionRowLayout.setId((int) question.id);
        setupViewForQuestionMetadata(context, questionRowLayout, question);
        setupViewForTags(questionRowLayout, context, question);
        return questionRowLayout;
    }

    private void setupViewForTags(final LinearLayout parentLayout, final Context context, final Question question)
    {
        LinearLayout tagsParentLayout = (LinearLayout) parentLayout.findViewById(R.id.questionSnippetTags);
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        int maxWidth = display.widthPixels - 10;

        Log.d(TAG, "Max width for tag row: " + maxWidth);

        LinearLayout rowLayout = createNewRowForTags(context, 0);

        if (question.tags != null && question.tags.length > 0)
        {
            for (int i = 0; i < question.tags.length; i++)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(3, 0, 3, 0);
                TextView tagTextView = ((TextView) getInflater(context).inflate(R.layout.tags_layout, null));
                tagTextView.setText(question.tags[i]);

                tagTextView.measure(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                rowLayout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                Log.d(TAG, "Current textview width: " + rowLayout.getMeasuredWidth());

                if ((tagTextView.getMeasuredWidth() + rowLayout.getMeasuredWidth()) > maxWidth)
                {
                    tagsParentLayout.addView(rowLayout);
                    rowLayout = createNewRowForTags(context, 3);
                }

                rowLayout.addView(tagTextView, params);
            }

            tagsParentLayout.addView(rowLayout);
        }
    }

    private LinearLayout createNewRowForTags(final Context context, int topMargin)
    {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = topMargin;
        rowLayout.setLayoutParams(layoutParams);
        return rowLayout;
    }

    private void setupViewForQuestionMetadata(Context context, LinearLayout parentLayout, Question question)
    {
        TextView textView = (TextView) parentLayout.findViewById(R.id.score);
        textView.setText(AppUtils.formatNumber(question.score));

        textView = (TextView) parentLayout.findViewById(R.id.answerCount);
        textView.setText(AppUtils.formatNumber(question.answerCount));

        if (question.hasAcceptedAnswer)
            textView.setBackgroundColor(context.getResources().getColor(R.color.lichen));

        textView = (TextView) parentLayout.findViewById(R.id.itemTitle);
        textView.setText(Html.fromHtml(question.title));

        textView = (TextView) parentLayout.findViewById(R.id.questionViewsValue);
        textView.append(":" + AppUtils.formatNumber(question.viewCount));

        textView = (TextView) parentLayout.findViewById(R.id.questionOwner);
        textView.setText(DateTimeUtils.getElapsedDurationSince(question.creationDate) + " by "
                        + Html.fromHtml(question.owner.displayName));
    }

    private LayoutInflater getInflater(Context context)
    {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}

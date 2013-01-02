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
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Question;

public class QuestionRowLayoutBuilder
{
    private static QuestionRowLayoutBuilder builder = new QuestionRowLayoutBuilder();

    private QuestionRowLayoutBuilder()
    {
    }

    public static QuestionRowLayoutBuilder getInstance()
    {
	return builder;
    }

    public LinearLayout build(final LayoutInflater layoutInflater, final Context context, final boolean cached,
	            final Question question)
    {
	LinearLayout questionRowLayout = (LinearLayout) layoutInflater.inflate(R.layout.question_snippet_layout, null);

	questionRowLayout.setId((int) question.id);
	setupViewForQuestionMetadata(questionRowLayout, question);
	setupViewForTags(questionRowLayout, context, question);
	return questionRowLayout;
    }

    private void setupViewForTags(final LinearLayout parentLayout, final Context context, final Question question)
    {
	LinearLayout tagsParentLayout = (LinearLayout) parentLayout.findViewById(R.id.questionSnippetTags);

	if (question.tags != null && question.tags.length > 0)
	{
	    for (int i = 0; i < question.tags.length; i++)
	    {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		                LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(3, 0, 3, 0);
		TextView tagTextView = ((TextView) getInflater(context).inflate(R.layout.tags_layout, null));
		tagTextView.setText(question.tags[i]);
		tagsParentLayout.addView(tagTextView, params);
	    }
	}
    }

    private void setupViewForQuestionMetadata(LinearLayout parentLayout, Question question)
    {
	TextView textView = (TextView) parentLayout.findViewById(R.id.questionScore);
	textView.setText(AppUtils.formatNumber(question.score));

	if (question.hasAcceptedAnswer)
	    textView.setBackgroundResource(R.drawable.rounded_border_delft_bg_lichen);

	textView = (TextView) parentLayout.findViewById(R.id.questionTitle);
	textView.setText(Html.fromHtml(question.title));

	textView = (TextView) parentLayout.findViewById(R.id.questionViewsValue);
	textView.append(":" + AppUtils.formatNumber(question.viewCount));

	textView = (TextView) parentLayout.findViewById(R.id.questionAnswersValue);
	textView.append(":" + AppUtils.formatNumber(question.answerCount));

	textView = (TextView) parentLayout.findViewById(R.id.questionOwner);
	textView.setText(DateTimeUtils.getElapsedDurationSince(question.creationDate) + " by "
	                + Html.fromHtml(question.owner.displayName));
    }

    private LayoutInflater getInflater(Context context)
    {
	return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}

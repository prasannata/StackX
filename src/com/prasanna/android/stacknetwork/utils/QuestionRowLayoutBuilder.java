package com.prasanna.android.stacknetwork.utils;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionDetailActivity;
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

    public LinearLayout build(final LayoutInflater layoutInflater, final Context context, boolean cached,
	            Question question)
    {
	LinearLayout questionRowLayout = (LinearLayout) layoutInflater.inflate(R.layout.question_snippet_layout, null);
	if (question.hasAcceptedAnswer == true)
	{
	    questionRowLayout.setBackgroundResource(R.drawable.question_answered_shape);
	}

	createCountsView(questionRowLayout, question);
	createQuestionSnippetView(questionRowLayout, context, cached, question);
	setupViewForTags(questionRowLayout, context, question);

	return questionRowLayout;
    }

    private void createQuestionSnippetView(final LinearLayout parentLayout, final Context context,
	            final boolean cached, final Question question)
    {
	TextView textView = (TextView) parentLayout.findViewById(R.id.questionSnippetTitle);
	textView.setText(Html.fromHtml(question.title));
	textView.setOnClickListener(new View.OnClickListener()
	{
	    public void onClick(View view)
	    {
		Intent displayQuestionIntent = new Intent(view.getContext(), QuestionDetailActivity.class);
		displayQuestionIntent.putExtra(StringConstants.QUESTION, question);
		displayQuestionIntent.putExtra(StringConstants.CACHED, cached);
		if (cached == true)
		{
		    displayQuestionIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		}
		context.startActivity(displayQuestionIntent);
	    }
	});
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

    private void createCountsView(final LinearLayout parentLayout, Question question)
    {
	TextView textView = (TextView) parentLayout.findViewById(R.id.questionViewsValue);
	textView.append(":" + AppUtils.formatNumber(question.viewCount));

	textView = (TextView) parentLayout.findViewById(R.id.questionScoreValue);
	textView.append(":" + AppUtils.formatNumber(question.score));

	textView = (TextView) parentLayout.findViewById(R.id.questionAnswersValue);
	textView.append(":" + AppUtils.formatNumber(question.answerCount));
    }

    private LayoutInflater getInflater(Context context)
    {
	return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}

package com.prasanna.android.stacknetwork.utils;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionDetailActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Question;

public class LayoutBuilder
{
    private static LayoutBuilder builder = new LayoutBuilder();
    private static int id = 0;

    private LayoutBuilder()
    {
    }

    public static LayoutBuilder getInstance()
    {
	return builder;
    }

    public LinearLayout buildQuestionSnippet(final Context context, Question question)
    {
	LinearLayout topLayout = new LinearLayout(context);
	topLayout.setWeightSum(1f);
	if (question.getHasAcceptedAnswer())
	{
	    topLayout.setBackgroundResource(R.drawable.question_answered_shape);
	}
	else
	{
	    topLayout.setBackgroundResource(R.drawable.question_unanswered_shape);
	}

	topLayout.addView(createCountsView(context, question));
	topLayout.addView(createQuestionSnippetView(context, question));
	return topLayout;
    }

    private LinearLayout createQuestionSnippetView(final Context context, final Question question)
    {
	LinearLayout questionLinearLayout = new LinearLayout(context);
	questionLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
	questionLinearLayout.setClickable(true);
	questionLinearLayout.setOnClickListener(new View.OnClickListener()
	{
	    public void onClick(View view)
	    {
		Intent displayQuestionIntent = new Intent(view.getContext(),
		        QuestionDetailActivity.class);
		displayQuestionIntent.putExtra("question", question);
		context.startActivity(displayQuestionIntent);
	    }
	});

	RelativeLayout relativeLayout = new RelativeLayout(context);
	TextView textView = getTextViewForQuestion(context, question);
	textView.setId(++id);
	relativeLayout.addView(textView);

	setupViewForTags(context, question, relativeLayout);
	questionLinearLayout.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.86f));
	questionLinearLayout.addView(relativeLayout);
	return questionLinearLayout;
    }

    private void setupViewForTags(final Context context, final Question question,
	    RelativeLayout relativeLayout)
    {
	RelativeLayout.LayoutParams params = getTagLayoutParams();

	TextView tagTextView = inflateTagsTextView(context);
	tagTextView.setId(++id);
	tagTextView.setText(question.getTags()[0]);
	params.addRule(RelativeLayout.BELOW, id - 1);
	tagTextView.setLayoutParams(params);
	relativeLayout.addView(tagTextView);

	for (int i = 1; i < question.getTags().length; i++)
	{
	    RelativeLayout.LayoutParams layoutParams = getTagLayoutParams();
	    tagTextView = inflateTagsTextView(context);
	    tagTextView.setId(++id);
	    tagTextView.setText(question.getTags()[i]);
	    tagTextView.setLayoutParams(layoutParams);
	    layoutParams.addRule(RelativeLayout.ALIGN_TOP, id - 1);
	    layoutParams.addRule(RelativeLayout.RIGHT_OF, id - 1);
	    relativeLayout.addView(tagTextView);
	}
    }

    private RelativeLayout.LayoutParams getTagLayoutParams()
    {
	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
	        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	params.setMargins(0, 0, 7, 0);
	return params;
    }

    private LinearLayout createCountsView(Context context, Question question)
    {
	LinearLayout countsLayout = new LinearLayout(context);
	countsLayout.setOrientation(LinearLayout.VERTICAL);
	TextView textView = inflateCountsTextView(context);
	textView.setText(context.getString(R.string.QuestionViews)
	        + String.valueOf(question.getViewCount()));
	countsLayout.addView(textView);

	textView = inflateCountsTextView(context);
	textView.setText(context.getString(R.string.QuestionScore)
	        + String.valueOf(question.getScore()));
	countsLayout.addView(textView);

	textView = inflateCountsTextView(context);
	textView.setText(context.getString(R.string.QuestionAnswers)
	        + String.valueOf(question.getAnswerCount()));
	countsLayout.addView(textView);
	LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.14f);
	countsLayout.setLayoutParams(params);
	return countsLayout;
    }

    private LayoutInflater getInflater(Context context)
    {
	return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private TextView inflateQuestionSnippetView(Context context)
    {
	return ((TextView) getInflater(context).inflate(R.layout.question_snippet_layout, null));
    }

    private TextView inflateCountsTextView(Context context)
    {
	return ((TextView) getInflater(context).inflate(R.layout.question_counts_layout, null));
    }

    private TextView inflateTagsTextView(Context context)
    {
	return ((TextView) getInflater(context).inflate(R.layout.tags_layout, null));
    }

    private TextView getTextViewForQuestion(Context context, final Question question)
    {
	TextView textView = inflateQuestionSnippetView(context);
	textView.setText(Html.fromHtml(question.getTitle()));
	return textView;
    }
}

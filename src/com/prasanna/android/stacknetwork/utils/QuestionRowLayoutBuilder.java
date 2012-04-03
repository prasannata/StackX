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

public class QuestionRowLayoutBuilder
{
    private static QuestionRowLayoutBuilder builder = new QuestionRowLayoutBuilder();
    private static int id = 0;

    private QuestionRowLayoutBuilder()
    {
    }

    public static QuestionRowLayoutBuilder getInstance()
    {
        return builder;
    }

    public LinearLayout build(final Context context, Question question)
    {
        LinearLayout topLayout = new LinearLayout(context);
        topLayout.setWeightSum(1f);
        if (question.hasAcceptedAnswer == true)
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
                Intent displayQuestionIntent = new Intent(view.getContext(), QuestionDetailActivity.class);
                displayQuestionIntent.putExtra("question", question);
                context.startActivity(displayQuestionIntent);
            }
        });

        RelativeLayout relativeLayout = new RelativeLayout(context);
        TextView textView = ((TextView) getInflater(context).inflate(R.layout.question_snippet_layout, null));
        textView.setText(Html.fromHtml(question.title));
        textView.setId(++id);
        relativeLayout.addView(textView);

        setupViewForTags(context, question, relativeLayout);
        questionLinearLayout.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.86f));
        questionLinearLayout.addView(relativeLayout);
        return questionLinearLayout;
    }

    private void setupViewForTags(final Context context, final Question question, RelativeLayout relativeLayout)
    {
        RelativeLayout.LayoutParams params = getTagLayoutParams();

        if (question.tags != null && question.tags.length > 0)
        {
            TextView tagTextView = ((TextView) getInflater(context).inflate(R.layout.tags_layout, null));
            tagTextView.setId(++id);
            tagTextView.setText(question.tags[0]);
            params.addRule(RelativeLayout.BELOW, id - 1);
            tagTextView.setLayoutParams(params);
            relativeLayout.addView(tagTextView);

            for (int i = 1; i < question.tags.length; i++)
            {
                RelativeLayout.LayoutParams layoutParams = getTagLayoutParams();
                tagTextView = ((TextView) getInflater(context).inflate(R.layout.tags_layout, null));
                tagTextView.setId(++id);
                tagTextView.setText(question.tags[i]);
                tagTextView.setLayoutParams(layoutParams);
                layoutParams.addRule(RelativeLayout.ALIGN_TOP, id - 1);
                layoutParams.addRule(RelativeLayout.RIGHT_OF, id - 1);
                relativeLayout.addView(tagTextView);
            }
        }
    }

    private RelativeLayout.LayoutParams getTagLayoutParams()
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 7, 0);
        return params;
    }

    private LinearLayout createCountsView(Context context, Question question)
    {
        LinearLayout countsLayout = new LinearLayout(context);
        countsLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = ((TextView) getInflater(context).inflate(R.layout.question_counts_layout, null));
        textView.setText(context.getString(R.string.QuestionViews) + String.valueOf(question.viewCount));
        countsLayout.addView(textView);

        textView = ((TextView) getInflater(context).inflate(R.layout.question_counts_layout, null));
        textView.setText(context.getString(R.string.QuestionScore) + String.valueOf(question.score));
        countsLayout.addView(textView);

        textView = ((TextView) getInflater(context).inflate(R.layout.question_counts_layout, null));
        textView.setText(context.getString(R.string.QuestionAnswers) + String.valueOf(question.answerCount));
        countsLayout.addView(textView);
        LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.14f);
        countsLayout.setLayoutParams(params);
        return countsLayout;
    }

    private LayoutInflater getInflater(Context context)
    {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}

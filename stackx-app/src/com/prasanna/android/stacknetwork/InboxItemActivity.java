/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.Post;
import com.prasanna.android.stacknetwork.model.Post.PostType;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.AnswersIntentService;
import com.prasanna.android.stacknetwork.service.PostIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class InboxItemActivity extends AbstractUserActionBarActivity implements StackXRestQueryResultReceiver
{
    private RestQueryResultReceiver receiver;
    private InboxItem item;
    private Comment comment;
    private TextView viewQuestion;
    private String VIEW_ANSWER;
    private String HIDE_ANSWER;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.inbox_item_detail);

        VIEW_ANSWER = getString(R.string.viewAnswer);
        HIDE_ANSWER = getString(R.string.hideAnswer);
        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);
        viewQuestion = (TextView) findViewById(R.id.viewQuestion);

        showInboxItem();
        getPostDetail();
    }

    private void getPostDetail()
    {
        setProgressBarIndeterminateVisibility(true);

        Intent intent = new Intent(this, PostIntentService.class);
        intent.putExtra(StringConstants.SITE, item.site.apiSiteParameter);
        if (item.itemType.equals(ItemType.NEW_ANSWER))
        {
            intent.putExtra(StringConstants.ACTION, PostIntentService.GET_POST);
            intent.putExtra(StringConstants.POST_ID, item.answerId);
        }
        else
        {
            intent.putExtra(StringConstants.ACTION, PostIntentService.GET_POST_COMMENT);
            intent.putExtra(StringConstants.COMMENT_ID, item.commentId);
        }

        intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);

        startService(intent);
    }

    private void showInboxItem()
    {
        item = (InboxItem) getIntent().getSerializableExtra(StringConstants.INBOX_ITEM);

        getActionBar().setTitle(Html.fromHtml(item.title));

        TextView textView = (TextView) findViewById(R.id.postTitle);
        textView.setText(Html.fromHtml(item.title));

        textView = (TextView) findViewById(R.id.postType);
        textView.setText(item.itemType.getRepr());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        if (menu != null)
            menu.removeItem(R.id.menu_refresh);

        return ret & true;
    }

    @Override
    protected void refresh()
    {
        throw new UnsupportedOperationException("Refersh not supported");
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        setProgressBarIndeterminateVisibility(false);

        switch (resultCode)
        {
            case PostIntentService.GET_POST:
                showPostDetail((Post) resultData.getSerializable(StringConstants.POST), false);
                break;
            case PostIntentService.GET_POST_COMMENT:
                comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
                if (comment != null)
                {
                    boolean commentOnAnswer = isCommentOnAnswer();
                    showPostDetail(comment, commentOnAnswer);
                    if (commentOnAnswer)
                        getAnswer();
                }
                break;
            case AnswersIntentService.GET_ANSWER:
                setupOnClickForViewMyPost((Answer) resultData.getSerializable(StringConstants.ANSWER));
                break;
            case AnswersIntentService.ERROR:
                break;

        }
    }

    private void getAnswer()
    {
        Intent getAnswerIntent = new Intent(this, AnswersIntentService.class);
        getAnswerIntent.putExtra(StringConstants.ACTION, AnswersIntentService.GET_ANSWER);
        getAnswerIntent.putExtra(StringConstants.ANSWER_ID, item.answerId);
        getAnswerIntent.putExtra(StringConstants.RESULT_RECEIVER, receiver);
        startService(getAnswerIntent);
        setProgressBarIndeterminateVisibility(true);
    }

    private boolean isCommentOnAnswer()
    {
        return comment.type != null && comment.type.equals(PostType.ANSWER);
    }

    private void showPostDetail(Post stackXItem, boolean disableViewQuestion)
    {
        TextView textView = (TextView) findViewById(R.id.responseUserAndTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(stackXItem.creationDate) + " by "
                        + Html.fromHtml(stackXItem.owner.displayName));

        if (item.site != null)
        {
            textView = (TextView) findViewById(R.id.postSite);
            textView.setText("Asked in " + item.site.name);
        }

        textView = (TextView) findViewById(R.id.responseItemScore);
        textView.setVisibility(View.VISIBLE);
        textView.setText(String.valueOf(stackXItem.score));

        setupOnClickForViewQuestion(item.questionId, disableViewQuestion);

        ArrayList<View> views = MarkdownFormatter.parse(this, stackXItem.body);
        if (views != null)
        {
            LinearLayout postBodyLayout = (LinearLayout) findViewById(R.id.postBody);
            for (final View view : views)
                postBodyLayout.addView(view);
        }

    }

    private void setupOnClickForViewMyPost(Answer answer)
    {
        final LinearLayout postContextLayout = (LinearLayout) findViewById(R.id.postContext);
        ArrayList<View> views = MarkdownFormatter.parse(this, answer.body);

        if (views != null)
        {
            for (final View view : views)
                postContextLayout.addView(view);
        }

        final TextView textView = (TextView) findViewById(R.id.viewAnswer);
        textView.setVisibility(View.VISIBLE);
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (postContextLayout.getVisibility() == View.VISIBLE)
                {
                    postContextLayout.startAnimation(AnimationUtils.loadAnimation(InboxItemActivity.this,
                                    android.R.anim.slide_out_right));
                    postContextLayout.setVisibility(View.GONE);
                    textView.setText(VIEW_ANSWER);
                }
                else
                {
                    postContextLayout.startAnimation(AnimationUtils.loadAnimation(InboxItemActivity.this,
                                    android.R.anim.slide_in_left));
                    postContextLayout.setVisibility(View.VISIBLE);
                    textView.setText(HIDE_ANSWER);
                }
            }
        });

        setupOnClickForViewQuestion(answer.questionId, false);
    }

    private void setupOnClickForViewQuestion(final long questionId, boolean disableViewQuestion)
    {
        if (disableViewQuestion)
        {
            viewQuestion.setClickable(false);
            viewQuestion.setEnabled(false);
            viewQuestion.setTextColor(getResources().getColor(R.color.lightGrey));
        }
        else
        {
            viewQuestion.setClickable(true);
            viewQuestion.setEnabled(true);
            viewQuestion.setTextColor(getResources().getColor(R.color.delft));
            viewQuestion.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startQuestionDetailActivity(questionId);
                }
            });
        }
    }

    private void startQuestionDetailActivity(long questionId)
    {
        Intent displayQuestionIntent = new Intent(InboxItemActivity.this, QuestionActivity.class);
        displayQuestionIntent.setAction(StringConstants.QUESTION_ID);
        displayQuestionIntent.putExtra(StringConstants.QUESTION_ID, questionId);
        displayQuestionIntent.putExtra(StringConstants.SITE, item.site.apiSiteParameter);
        startActivity(displayQuestionIntent);
    }
}

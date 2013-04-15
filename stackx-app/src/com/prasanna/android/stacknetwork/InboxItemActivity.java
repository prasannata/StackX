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
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
import com.prasanna.android.stacknetwork.utils.StackXQuickActionMenu;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class InboxItemActivity extends AbstractUserActionBarActivity implements StackXRestQueryResultReceiver
{
    private RestQueryResultReceiver receiver;
    private InboxItem item;
    private Comment comment;
    private View postTitleLayout;
    private StackXQuickActionMenu stackXQuickActionMenu;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        item = (InboxItem) getIntent().getSerializableExtra(StringConstants.INBOX_ITEM);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.inbox_item_detail);

        stackXQuickActionMenu = new StackXQuickActionMenu(this);
        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);
        postTitleLayout = findViewById(R.id.postTitleLayout);

        setupQuickActionMenuClick();
        showInboxItem();
        getPostDetail();
    }

    private void setupQuickActionMenuClick()
    {
        final ImageView imageView = (ImageView) findViewById(R.id.quickActionMenu);
        imageView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stackXQuickActionMenu.build().show(v);
            }
        });
    }

    private void showInboxItem()
    {
        TextView textView = (TextView) findViewById(R.id.postTitle);
        textView.setText(Html.fromHtml(item.title));

        textView = (TextView) findViewById(R.id.postType);
        if (item.questionId != -1 && ItemType.NEW_ANSWER.equals(item.itemType))
            textView.setText(item.itemType.getRepr() + " to your question");
        else if (item.questionId != -1)
            textView.setText(item.itemType.getRepr() + " on your question");
        else if (item.answerId != -1)
            textView.setText(item.itemType.getRepr() + " on your answer");
        else
            textView.setText(item.itemType.getRepr());

        if (item.site != null)
        {
            textView = (TextView) findViewById(R.id.postSite);
            textView.setText("Asked in " + item.site.name);
        }
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

    @Override
    protected void setActionBarTitleAndIcon()
    {
        if (item == null || item.site == null)
            super.setActionBarTitleAndIcon();

        getActionBar().setTitle(Html.fromHtml(item.title));
        setActionBarHomeIcon(item.site.name, item.site.iconUrl);
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
                showPostDetail((Post) resultData.getSerializable(StringConstants.POST));
                break;
            case PostIntentService.GET_POST_COMMENT:
                comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
                if (comment != null)
                {
                    boolean commentOnAnswer = isCommentOnAnswer();
                    showPostDetail(comment);
                    if (commentOnAnswer)
                        startGetAnswerService();
                }
                else
                    showPostBody(item.body);
                break;
            case AnswersIntentService.GET_ANSWER:
                setupOnClickForViewMyAnswer((Answer) resultData.getSerializable(StringConstants.ANSWER));
                break;
            case AnswersIntentService.ERROR:
                break;

        }
    }

    private void startGetAnswerService()
    {
        Intent getAnswerIntent = new Intent(this, AnswersIntentService.class);
        getAnswerIntent.putExtra(StringConstants.SITE, item.site.apiSiteParameter);
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

    private void showPostDetail(Post stackXItem)
    {
        TextView textView = (TextView) findViewById(R.id.responseUserAndTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(stackXItem.creationDate) + " by "
                        + Html.fromHtml(stackXItem.owner.displayName));

        stackXQuickActionMenu.addUserProfileItem(stackXItem.owner.id, Html.fromHtml(stackXItem.owner.displayName)
                        .toString(), item.site);

        setupOnClickForViewQuestion(item.questionId);
        showPostBody(stackXItem.body);
    }

    protected void showPostBody(String body)
    {
        ArrayList<View> views = MarkdownFormatter.parse(this, body);
        if (views != null)
        {
            LinearLayout postBodyLayout = (LinearLayout) findViewById(R.id.postBody);
            for (final View view : views)
                postBodyLayout.addView(view);
        }
    }

    private void setupOnClickForViewMyAnswer(Answer answer)
    {
        final LinearLayout postContextLayout = (LinearLayout) findViewById(R.id.postContext);
        ArrayList<View> views = MarkdownFormatter.parse(this, answer.body);

        if (views != null)
        {
            for (final View view : views)
                postContextLayout.addView(view);
        }

        addViewAnswerToQuickActionMenu(postContextLayout);
        setupOnClickForViewQuestion(answer.questionId);
    }

    private void addViewAnswerToQuickActionMenu(final LinearLayout postContextLayout)
    {
        stackXQuickActionMenu.addItem(R.string.viewAnswer, new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (postContextLayout.getVisibility() == View.VISIBLE)
                {
                    postContextLayout.startAnimation(AnimationUtils.loadAnimation(InboxItemActivity.this,
                                    android.R.anim.slide_out_right));
                    postContextLayout.setVisibility(View.GONE);
                }
                else
                {
                    postContextLayout.startAnimation(AnimationUtils.loadAnimation(InboxItemActivity.this,
                                    android.R.anim.slide_in_left));
                    postContextLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupOnClickForViewQuestion(final long questionId)
    {
        postTitleLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startQuestionDetailActivity(questionId);
            }
        });
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

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

package com.prasanna.android.stacknetwork;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AnimationUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.Post;
import com.prasanna.android.stacknetwork.model.Post.PostType;
import com.prasanna.android.stacknetwork.model.StackXItem;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.AnswersIntentService;
import com.prasanna.android.stacknetwork.service.PostIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class InboxItemActivity extends AbstractUserActionBarActivity implements StackXRestQueryResultReceiver
{
    private static final String TAG = InboxItemActivity.class.getSimpleName();
    private Post post;
    private Intent intent;
    private RestQueryResultReceiver receiver;
    private InboxItem item;
    private Comment comment;
    private ContextMenu menu;
    private TextView viewQuestion;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.inbox_item_detail);

        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);
        viewQuestion = (TextView) findViewById(R.id.viewQuestion);

        showInboxItem();
        getPostDetail();
    }

    private void getPostDetail()
    {
        setProgressBarIndeterminateVisibility(true);

        intent = new Intent(this, PostIntentService.class);
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.question_context_menu, menu);

        menu.removeItem(R.id.q_ctx_comments);
        menu.removeItem(R.id.q_ctx_menu_email);
        menu.removeItem(R.id.q_ctx_menu_tags);
        menu.removeItem(R.id.q_ctx_similar);
        menu.removeItem(R.id.q_ctx_related);

        this.menu = menu;

        setupUserProfileInContextMenu();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.q_ctx_menu_user_profile:
                viewUserProfile();
                return true;

        }

        return false;
    }

    private void viewUserProfile()
    {
        long userId = post != null ? post.owner.id : comment.owner.id;
        Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        startActivity(userProfileIntent);
    }

    private void setupUserProfileInContextMenu()
    {
        MenuItem userProfileMenuItem = menu.findItem(R.id.q_ctx_menu_user_profile);
        String userName = post == null ? comment.owner.displayName : post.owner.displayName;
        if (userProfileMenuItem != null && userName != null)
            userProfileMenuItem.setTitle(userName + "'s profile");
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
        LogWrapper.d(TAG, "ResultReceiver invoked for " + resultCode);
        setProgressBarIndeterminateVisibility(false);

        switch (resultCode)
        {
            case PostIntentService.GET_POST:
                post = (Post) resultData.getSerializable(StringConstants.POST);
                if (post != null)
                    showPostDetail(post, false);
                break;
            case PostIntentService.GET_POST_COMMENT:
                comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
                if (comment != null)
                {
                    boolean commentOnAnswer = isCommentOnAnswer();
                    showPostDetail(comment, commentOnAnswer);
                    if (commentOnAnswer)
                    {
                        Intent getAnswerIntent = new Intent(this, AnswersIntentService.class);
                        getAnswerIntent.putExtra(StringConstants.ACTION, AnswersIntentService.GET_ANSWER);
                        getAnswerIntent.putExtra(StringConstants.ANSWER_ID, item.answerId);
                        getAnswerIntent.putExtra(StringConstants.RESULT_RECEIVER, receiver);
                        startService(getAnswerIntent);
                        setProgressBarIndeterminateVisibility(true);
                    }
                }
                break;
            case AnswersIntentService.GET_ANSWER:
                Answer answer = (Answer) resultData.getSerializable(StringConstants.ANSWER);
                setupOnClickForViewMyPost(answer);
                break;

        }
    }

    private boolean isCommentOnAnswer()
    {
        return comment.type != null && comment.type.equals(PostType.ANSWER);
    }

    private void showPostDetail(StackXItem stackXItem, boolean disableViewQuestion)
    {
        LogWrapper.d(TAG, "Showing post " + stackXItem.id);

        TextView textView = (TextView) findViewById(R.id.responseUserAndTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(stackXItem.creationDate) + " by "
                        + Html.fromHtml(stackXItem.owner.displayName));

        textView = (TextView) findViewById(R.id.postSite);
        textView.setText("Asked in " + item.site.name);

        textView = (TextView) findViewById(R.id.responseItemScore);
        textView.setVisibility(View.VISIBLE);
        textView.setText(String.valueOf(stackXItem.score));

        LogWrapper.d(TAG, "Setting on click view question");
        setupOnClickForViewQuestion(item.questionId, disableViewQuestion);
        setuOnClickForContextMenu();

        LinearLayout postBodyLayout = (LinearLayout) findViewById(R.id.postBody);
        for (View view : MarkdownFormatter.parse(this, stackXItem.body))
            postBodyLayout.addView(view);
    }

    private void setuOnClickForContextMenu()
    {
        ImageView imageView = (ImageView) findViewById(R.id.responseContextMenu);
        registerForContextMenu(imageView);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                InboxItemActivity.this.openContextMenu(v);
            }
        });
    }

    private void setupOnClickForViewMyPost(Answer answer)
    {
        final LinearLayout postContextLayout = (LinearLayout) findViewById(R.id.postContext);
        for (View view : MarkdownFormatter.parse(this, answer.body))
            postContextLayout.addView(view);

        TextView textView = (TextView) findViewById(R.id.viewAnswer);
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
                }
                else
                {
                    postContextLayout.startAnimation(AnimationUtils.loadAnimation(InboxItemActivity.this,
                                    android.R.anim.slide_in_left));
                    postContextLayout.setVisibility(View.VISIBLE);
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

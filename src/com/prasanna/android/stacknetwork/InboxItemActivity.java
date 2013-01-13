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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.Post;
import com.prasanna.android.stacknetwork.model.StackXItem;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.PostIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class InboxItemActivity extends AbstractUserActionBarActivity implements StackXRestQueryResultReceiver
{
    private static final String TAG = InboxItemActivity.class.getSimpleName();
    private Post post;
    private Intent intent;
    private RestQueryResultReceiver receiver;
    private InboxItem item;
    private Comment comment;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.inbox_item_detail);

        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);

        showInboxItem();

        if (post == null && comment == null)
            getPostDetail();
        else
            showPostDetail(post == null ? comment : post);

    }

    private void getPostDetail()
    {
        setProgressBarIndeterminateVisibility(true);

        intent = new Intent(this, PostIntentService.class);
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

        TextView textView = (TextView) findViewById(R.id.itemTitle);
        textView.setText(Html.fromHtml(item.title));

        if (item.body != null)
        {
            textView = (TextView) findViewById(R.id.itemBodyPreview);
            textView.setVisibility(View.GONE);
        }

        textView = (TextView) findViewById(R.id.itemCreationTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(item.creationDate));

        textView = (TextView) findViewById(R.id.itemType);
        textView.setText(item.itemType.getRepr());

        if (item.site != null)
        {
            textView = (TextView) findViewById(R.id.itemSite);
            textView.setText(item.site.name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

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
        Log.d(TAG, "ResultReceiver invoked for " + resultCode);

        if (resultCode == PostIntentService.GET_POST)
        {
            post = (Post) resultData.getSerializable(StringConstants.POST);
            if (post != null)
                showPostDetail(post);
        }
        else
        {
            comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
            if (comment != null)
                showPostDetail(comment);

        }

    }

    private void showPostDetail(StackXItem stackXItem)
    {
        Log.d(TAG, "Showing post " + stackXItem.id);

        setProgressBarIndeterminateVisibility(false);

        TextView textView = (TextView) findViewById(R.id.responseUserAndTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(stackXItem.creationDate) + " ago by "
                        + Html.fromHtml(stackXItem.owner.displayName));

        textView = (TextView) findViewById(R.id.responseItemScore);
        textView.setText(String.valueOf(stackXItem.score));

        LinearLayout postBodyLayout = (LinearLayout) findViewById(R.id.postBody);
        for (View view : MarkdownFormatter.parse(getApplicationContext(), stackXItem.body))
            postBodyLayout.addView(view);
    }
}

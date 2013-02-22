/*
    Copyright 2012 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class PostIntentService extends IntentService
{
    private static final String TAG = PostIntentService.class.getSimpleName();

    public static final int GET_POST = 0x01;
    public static final int GET_POST_COMMENT = 0x02;

    public PostIntentService()
    {
        this(TAG);
    }

    public PostIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
        int action = intent.getIntExtra(StringConstants.ACTION, 0);
        long postId = intent.getLongExtra(StringConstants.POST_ID, 0);
        final String site = intent.getStringExtra(StringConstants.SITE);

        switch (action)
        {
            case GET_POST:
                getPost(postId, site, receiver);
                break;
            case GET_POST_COMMENT:
                getComment(intent, site, receiver);
                break;
            default:
                LogWrapper.d(TAG, "Unknown action:" + action);
                break;
        }
    }

    private void getComment(Intent intent, String site, final ResultReceiver receiver)
    {
        long commentId = intent.getLongExtra(StringConstants.COMMENT_ID, 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable(StringConstants.COMMENT, PostServiceHelper.getInstance().getComment(commentId, site));
        receiver.send(GET_POST_COMMENT, bundle);
    }

    private void getPost(long postId, String site, ResultReceiver receiver)
    {
        if (postId > 0)
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable(StringConstants.POST, PostServiceHelper.getInstance().getPost(postId, site));
            receiver.send(GET_POST, bundle);
        }
    }
}

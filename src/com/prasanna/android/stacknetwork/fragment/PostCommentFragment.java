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

package com.prasanna.android.stacknetwork.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.fragment.PostCommentView.OnSendCommentListener;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class PostCommentFragment extends Fragment
{
    private static final String TAG = PostCommentFragment.class.getSimpleName();

    private static final String TEXT = "text";
    private RestQueryResultReceiver resultReceiver;
    private PostCommentView postCommentView;

    private class OnSendCommentListenerImpl implements OnSendCommentListener
    {
        @Override
        public boolean sendComment(long postId, String body)
        {
            if (isAdded())
            {
                if (AppUtils.allowedToWrite(getActivity()))
                {
                    Intent intent = new Intent(getActivity(), WriteIntentService.class);
                    intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
                    intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_ADD_COMMENT);
                    intent.putExtra(StringConstants.POST_ID, postId);
                    intent.putExtra(StringConstants.BODY, body);
                    getActivity().startService(intent);
                    return true;
                }
                else
                {
                    long minSecondsBetweenWrite = SharedPreferencesUtil.getLong(getActivity(),
                                    WritePermission.PREF_SECS_BETWEEN_COMMENT_WRITE, 0);
                    Toast.makeText(getActivity(),
                                    "You have to wait a minium of " + minSecondsBetweenWrite + " between writes",
                                    Toast.LENGTH_LONG).show();
                }
            }

            return false;
        }
    }

    public PostCommentFragment()
    {
        postCommentView = new PostCommentView(this);
    }

    public void setPostId(long id)
    {
        postCommentView.setPostId(id);
    }

    public void setTitle(String title)
    {
        postCommentView.setTitle(title);
    }

    public void setDraftText(String draftText)
    {
        postCommentView.setDraftText(draftText);
    }

    public void setSendError(String errorResponse)
    {
        postCommentView.setSendError(errorResponse);
    }

    public void setResultReceiver(RestQueryResultReceiver resultReceiver)
    {
        this.resultReceiver = resultReceiver;
    }

    public void hideSoftKeyboard()
    {
        postCommentView.hideSoftKeyboard();
    }

    public String getCurrentText()
    {
        return postCommentView.getCurrentText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return postCommentView.getView(inflater, R.layout.post_comment, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        postCommentView.prepare(new OnSendCommentListenerImpl());
        postCommentView.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");

        if (postCommentView.getCurrentText() != null)
            outState.putString(TEXT, postCommentView.getCurrentText().toString());

        super.onSaveInstanceState(outState);
    }

    public void refreshView()
    {
        postCommentView.show();
    }
}

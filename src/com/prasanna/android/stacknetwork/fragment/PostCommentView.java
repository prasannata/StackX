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
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.utils.AppUtils;

public class PostCommentView
{
    private static final int COMMENT_MIN_LEN = 15;

    private ScrollView parentLayout;
    private EditText editText;
    private TextView commentContext;
    private TextView sendComment;
    private String title;
    private long postId;
    private String draftText;
    private TextView charCount;
    private TextView sendStatus;
    private ProgressBar sendProgressBar;
    private final Fragment fragment;

    private class CommentTextWatcher implements TextWatcher
    {
        @Override
        public void afterTextChanged(Editable s)
        {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (!fragment.isRemoving() || !fragment.isDetached())
            {
                charCount.setText(String.valueOf(s.length()));

                if (s.length() >= COMMENT_MIN_LEN)
                {
                    sendComment.setTextColor(fragment.getResources().getColor(R.color.delft));
                    sendComment.setClickable(true);
                }
                else
                {
                    if (s.length() < COMMENT_MIN_LEN && sendComment.isClickable())
                    {
                        sendComment.setTextColor(fragment.getResources().getColor(R.color.lightGrey));
                        sendComment.setClickable(false);
                    }
                }
            }
        }
    }

    public interface OnSendCommentListener
    {
        boolean sendComment(long postId, String body);
    }

    public PostCommentView(Fragment fragment)
    {
        this.fragment = fragment;
    }

    public void setPostId(long id)
    {
        this.postId = id;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCurrentText()
    {
        if (editText != null && editText.getText() != null)
            return editText.getText().toString();

        return null;
    }

    public void setDraftText(String draftText)
    {
        this.draftText = draftText;
    }

    public void setSendError(String errorResponse)
    {
        sendProgressBar.setVisibility(View.GONE);

        if (sendStatus != null)
        {
            String failureText = "Failed";
            StackXError error = StackXError.deserialize(errorResponse);
            sendStatus.setText(error != null ? error.name : failureText);
            sendStatus.setTextColor(Color.RED);
            sendStatus.setVisibility(View.VISIBLE);
        }

        editText.setFocusableInTouchMode(true);
        sendComment.setClickable(true);
        sendComment.setTextColor(fragment.getResources().getColor(R.color.delft));
    }

    public View getView(LayoutInflater inflater, int postCommentLayoutResId, ViewGroup container,
                    Bundle savedInstanceState)
    {
        parentLayout = (ScrollView) inflater.inflate(postCommentLayoutResId, null);
        charCount = (TextView) parentLayout.findViewById(R.id.charCount);
        sendStatus = (TextView) parentLayout.findViewById(R.id.sendStatus);
        sendProgressBar = (ProgressBar) parentLayout.findViewById(R.id.sendProgress);

        return parentLayout;
    }

    public void prepare(OnSendCommentListener onSendCommentListener)
    {
        prepareSendComment(onSendCommentListener);
        prepareEditText();

        if (title != null)
        {
            commentContext = (TextView) parentLayout.findViewById(R.id.commentContext);
            commentContext.setText(Html.fromHtml(title));
        }
    }

    private void prepareEditText()
    {
        editText = (EditText) parentLayout.findViewById(R.id.textInput);
        editText.addTextChangedListener(new CommentTextWatcher());
        if (draftText != null)
            editText.setText(draftText);
    }

    private void prepareSendComment(final OnSendCommentListener onSendCommentListener)
    {
        sendComment = (TextView) parentLayout.findViewById(R.id.sendComment);
        sendComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (editText.getText() != null && editText.getText().toString().length() > 0)
                {
                    if (onSendCommentListener.sendComment(postId, editText.getText().toString()))
                        updateUIElements();
                }
            }
        });
    }

    private void updateUIElements()
    {
        editText.setFocusable(false);
        sendComment.setClickable(false);
        sendProgressBar.setVisibility(View.VISIBLE);
        sendStatus.setVisibility(View.GONE);
    }

    public void hideSoftKeyboard()
    {
        if (fragment.isVisible() && editText != null)
            AppUtils.hideSoftInput(fragment.getActivity(), editText);
    }

    public void show()
    {
        if (View.GONE == parentLayout.getVisibility())
            parentLayout.setVisibility(View.VISIBLE);

        if (editText.getText() != null)
            editText.setSelection(editText.getText().length());

        editText.requestFocus();
        AppUtils.showSoftInput(fragment.getActivity(), editText);
    }

    public void hide()
    {
        if (View.VISIBLE == parentLayout.getVisibility())
            parentLayout.setVisibility(View.GONE);

        editText.clearFocus();
        AppUtils.hideSoftInput(fragment.getActivity(), editText);
    }

    public boolean isVisible()
    {
        return parentLayout != null && View.VISIBLE == parentLayout.getVisibility();
    }

}

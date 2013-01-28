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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class PostCommentFragment extends Fragment
{
    private static final String TAG = PostCommentFragment.class.getSimpleName();
    private static final String TEXT = "text";
    private static final int COMMENT_MIN_LEN = 15;

    private RelativeLayout parentLayout;
    private EditText editText;
    private TextView commentContext;
    private TextView sendComment;
    private String title;
    private long postId;
    private String draftText;
    private RestQueryResultReceiver resultReceiver;
    private TextView charCount;
    private TextView sendStatus;
    private ProgressBar sendProgressBar;

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
            charCount.setText(String.valueOf(s.length()));

            if (s.length() >= COMMENT_MIN_LEN && !sendComment.isClickable())
            {
                sendComment.setTextColor(getResources().getColor(R.color.delft));
                sendComment.setClickable(true);
            }
            else
            {
                if (s.length() < COMMENT_MIN_LEN && sendComment.isClickable())
                {
                    sendComment.setTextColor(getResources().getColor(R.color.lightGrey));
                    sendComment.setClickable(false);
                }
            }
        }
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
    }

    public void setResultReceiver(RestQueryResultReceiver resultReceiver)
    {
        this.resultReceiver = resultReceiver;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        parentLayout = (RelativeLayout) inflater.inflate(R.layout.post_comment, null);
        charCount = (TextView) parentLayout.findViewById(R.id.charCount);
        sendStatus = (TextView) parentLayout.findViewById(R.id.sendStatus);
        sendProgressBar = (ProgressBar) parentLayout.findViewById(R.id.sendProgress);

        prepareSendComment();
        prepareEditText(savedInstanceState);

        if (title != null)
        {
            commentContext = (TextView) parentLayout.findViewById(R.id.commentContext);
            commentContext.setText(Html.fromHtml(title));
        }

        return parentLayout;
    }

    private void prepareEditText(Bundle savedInstanceState)
    {
        editText = (EditText) parentLayout.findViewById(R.id.textInput);
        editText.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(final View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    editText.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            }
        });

        editText.addTextChangedListener(new CommentTextWatcher());

        if (draftText != null)
            editText.setText(draftText);

        if (savedInstanceState != null && savedInstanceState.getString(TEXT) != null)
            editText.setText(savedInstanceState.getString(TEXT));

        editText.requestFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");

        if (editText != null && editText.getText() != null)
            outState.putString(TEXT, editText.getText().toString());

        super.onSaveInstanceState(outState);
    }

    private void prepareSendComment()
    {
        sendComment = (TextView) parentLayout.findViewById(R.id.sendComment);
        sendComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (editText.getText() != null && editText.getText().toString().length() > 0)
                {
                    if (isAdded())
                        sendComment();
                }
            }
        });
    }

    private void sendComment()
    {
        Intent intent = new Intent(getActivity(), WriteIntentService.class);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
        intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_ADD_COMMENT);
        intent.putExtra(StringConstants.POST_ID, postId);
        intent.putExtra(StringConstants.BODY, editText.getText().toString());
        updateUIElements();
        getActivity().startService(intent);
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
        if (isVisible() && editText != null)
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
}

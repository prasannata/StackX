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

package com.prasanna.android.stacknetwork.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;

public class PostCommentFragment extends Fragment
{
    private static final String TAG = PostCommentFragment.class.getSimpleName();
    private static final String TEXT = "text";

    private RelativeLayout parentLayout;
    private EditText editText;
    private TextView commentContext;
    private TextView sendComment;
    private String title;
    private long postId;
    private String draftText;

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        parentLayout = (RelativeLayout) inflater.inflate(R.layout.post_comment, null);
        prepareEditText();

        if (savedInstanceState != null && savedInstanceState.getString(TEXT) != null)
            editText.setText(savedInstanceState.getString(TEXT));

        prepareSendComment();

        if (title != null)
        {
            commentContext = (TextView) parentLayout.findViewById(R.id.commentContext);
            commentContext.setText(title);
        }

        return parentLayout;
    }

    private void prepareEditText()
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

        if(draftText != null)
            editText.setText(draftText);
        
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
                    Toast.makeText(getActivity(), editText.getText().toString() + " for " + postId, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setPostId(long id)
    {
        this.postId = id;
    }

    public void hideSoftKeyboard()
    {
        if (isVisible() && editText != null)
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
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
}

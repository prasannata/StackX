/*
    Copyright (C) 2014 Prasanna Thirumalai
    
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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DialogBuilder;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class PostCommentFragment extends Fragment {
  private static final String TAG = PostCommentFragment.class.getSimpleName();
  private static final int COMMENT_MIN_LEN = 15;
  private static final int COMMENT_MAX_LEN = 600;
  private static final String TEXT = "text";

  private RestQueryResultReceiver resultReceiver;
  private RelativeLayout parentLayout;
  private EditText editText;
  private TextView commentContext;
  private TextView sendComment;
  private String title;
  private long postId;
  private String draftText;
  private TextView charCount;
  private TextView sendStatus;
  private ProgressBar sendProgressBar;
  private boolean myEdit = false;
  private long commentId;
  private int viewPagerPosition;

  private class CommentTextWatcher implements TextWatcher {
    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (!isRemoving() || !isDetached()) {
        charCount.setText(String.valueOf(s.length()));

        if (s.length() >= COMMENT_MIN_LEN && s.length() <= COMMENT_MAX_LEN) {
          charCount.setText(String.valueOf(COMMENT_MAX_LEN - s.length()));
          sendComment.setTextColor(getResources().getColor(R.color.delft));
          sendComment.setClickable(true);
        } else {
          if ((s.length() < COMMENT_MIN_LEN || s.length() > COMMENT_MAX_LEN) && sendComment.isClickable()) {
            sendComment.setTextColor(getResources().getColor(R.color.lightGrey));
            sendComment.setClickable(false);
          }

          if (s.length() > COMMENT_MAX_LEN) charCount.setText(String.valueOf(s.length() - COMMENT_MAX_LEN));
        }
      }
    }
  }

  public void setPostId(long id) {
    this.postId = id;
  }

  public void setViewPagerPosition(int viewPagerPosition) {
    this.viewPagerPosition = viewPagerPosition;
  }

  public void setCommentId(long commentId) {
    this.commentId = commentId;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDraftText(String draftText) {
    this.draftText = draftText;
  }

  public void setMyEdit(boolean myEdit) {
    this.myEdit = myEdit;
  }

  public void setSendError(String errorResponse) {
    sendProgressBar.setVisibility(View.GONE);

    if (sendStatus != null) {
      StackXError error = StackXError.deserialize(errorResponse);
      sendStatus.setText(error.name);
      sendStatus.setTextColor(Color.RED);
      sendStatus.setVisibility(View.VISIBLE);
      DialogBuilder.okDialog(getActivity(), error.msg, new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      }).show();
    }

    editText.setFocusableInTouchMode(true);
    sendComment.setClickable(true);
    sendComment.setTextColor(getResources().getColor(R.color.delft));
  }

  public void setResultReceiver(RestQueryResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;
  }

  public String getCurrentText() {
    if (editText != null && editText.getText() != null) return editText.getText().toString();

    return null;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    parentLayout = (RelativeLayout) inflater.inflate(R.layout.post_comment, null);
    charCount = (TextView) parentLayout.findViewById(R.id.charCount);
    sendStatus = (TextView) parentLayout.findViewById(R.id.sendStatus);
    sendProgressBar = (ProgressBar) parentLayout.findViewById(R.id.sendProgress);

    return parentLayout;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    prepare();
    show();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    LogWrapper.d(TAG, "onSaveInstanceState");

    if (getCurrentText() != null) outState.putString(TEXT, getCurrentText().toString());

    super.onSaveInstanceState(outState);
  }

  public void refreshView() {
    show();
  }

  public void prepare() {
    prepareSendComment();
    prepareEditText();

    if (title != null) {
      commentContext = (TextView) parentLayout.findViewById(R.id.commentContext);
      commentContext.setText(Html.fromHtml(title));
    }
  }

  private void prepareEditText() {
    editText = (EditText) parentLayout.findViewById(R.id.textInput);
    editText.addTextChangedListener(new CommentTextWatcher());
  }

  private void prepareSendComment() {
    sendComment = (TextView) parentLayout.findViewById(R.id.sendComment);
    sendComment.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (editText.getText() != null && editText.getText().toString().length() > 0) {
          if (sendComment(postId, editText.getText().toString())) updateUIElements();
        }
      }
    });
  }

  private void updateUIElements() {
    editText.setFocusable(false);
    sendComment.setClickable(false);
    sendProgressBar.setVisibility(View.VISIBLE);
    sendStatus.setVisibility(View.GONE);
  }

  public void hideSoftKeyboard() {
    if (isVisible() && editText != null) AppUtils.hideSoftInput(getActivity(), editText);
  }

  public void show() {
    if (View.GONE == parentLayout.getVisibility()) parentLayout.setVisibility(View.VISIBLE);

    if (draftText != null) editText.setText(Html.fromHtml(draftText));

    if (editText.getText() != null) editText.setSelection(editText.getText().length());

    editText.requestFocus();
    AppUtils.showSoftInput(getActivity(), editText);
  }

  public void hide() {
    if (View.VISIBLE == parentLayout.getVisibility()) parentLayout.setVisibility(View.GONE);

    editText.clearFocus();
    AppUtils.hideSoftInput(getActivity(), editText);
  }

  private boolean sendComment(long postId, String body) {
    if (isAdded()) {
      if (AppUtils.allowedToWrite(getActivity())) {
        startCommentWriteService(postId, body);
        return true;
      } else {
        long minSecondsBetweenWrite =
            SharedPreferencesUtil.getLong(getActivity(), WritePermission.PREF_SECS_BETWEEN_COMMENT_WRITE, 0);
        Toast.makeText(getActivity(), "You have to wait a minium of " + minSecondsBetweenWrite + " between writes",
            Toast.LENGTH_LONG).show();
      }
    }

    return false;
  }

  private void startCommentWriteService(long postId, String body) {
    Intent intent = new Intent(getActivity(), WriteIntentService.class);
    intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
    if (myEdit) {
      intent.putExtra(StringConstants.COMMENT_ID, commentId);
      intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_EDIT_COMMENT);
    } else intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_ADD_COMMENT);
    intent.putExtra(StringConstants.VIEW_PAGER_POSITION, viewPagerPosition);
    intent.putExtra(StringConstants.POST_ID, postId);
    intent.putExtra(StringConstants.BODY, body);
    getActivity().startService(intent);
  }
}

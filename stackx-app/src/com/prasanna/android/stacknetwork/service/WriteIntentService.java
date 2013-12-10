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

package com.prasanna.android.stacknetwork.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class WriteIntentService extends AbstractIntentService {
  private static final String TAG = WriteIntentService.class.getSimpleName();

  public static final int ACTION_ADD_COMMENT = 0x901;
  public static final int ACTION_EDIT_COMMENT = 0x902;
  public static final int ACTION_DEL_COMMENT = 0x903;

  public WriteIntentService() {
    this(TAG);
  }

  public WriteIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final int action = intent.getIntExtra(StringConstants.ACTION, -1);
    final long postId = intent.getLongExtra(StringConstants.POST_ID, -1);
    final int viewPagerPosition = intent.getIntExtra(StringConstants.VIEW_PAGER_POSITION, -1);
    final long commentId = intent.getLongExtra(StringConstants.COMMENT_ID, -1);
    final String body = intent.getStringExtra(StringConstants.BODY);
    final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);

    try {
      super.onHandleIntent(intent);

      switch (action) {
        case ACTION_ADD_COMMENT:
          sendComment(postId, viewPagerPosition, body, receiver);
          break;
        case ACTION_EDIT_COMMENT:
          editComment(commentId, body, receiver);
          break;
        case ACTION_DEL_COMMENT:
          deleteComment(commentId, receiver);
          break;
        default:
          LogWrapper.d(TAG, "Unknown action: " + action);
          break;
      }
    }
    catch (AbstractHttpException e) {
      Bundle bundle = new Bundle();
      bundle.putInt(StringConstants.REQUEST_CODE, action);
      bundle.putSerializable(StringConstants.EXCEPTION, e);
      receiver.send(ERROR, bundle);
    }
  }

  private void sendComment(final long postId, final int viewPagerPosition, final String body,
      final ResultReceiver receiver) {
    Bundle resultData = new Bundle();
    Comment comment = WriteServiceHelper.getInstance().addComment(postId, body);
    resultData.putLong(StringConstants.POST_ID, postId);
    resultData.putInt(StringConstants.VIEW_PAGER_POSITION, viewPagerPosition);
    resultData.putSerializable(StringConstants.COMMENT, comment);
    receiver.send(ACTION_ADD_COMMENT, resultData);
  }

  private void editComment(long commentId, String editedText, ResultReceiver receiver) {
    Bundle resultData = new Bundle();
    Comment comment = WriteServiceHelper.getInstance().editComment(commentId, editedText);
    resultData.putSerializable(StringConstants.COMMENT, comment);
    receiver.send(ACTION_EDIT_COMMENT, resultData);
  }

  private void deleteComment(long commentId, ResultReceiver receiver) {
    WriteServiceHelper.getInstance().deleteComment(commentId);
    Bundle resultData = new Bundle();
    resultData.putSerializable(StringConstants.COMMENT_ID, commentId);
    receiver.send(ACTION_DEL_COMMENT, resultData);
  }
}

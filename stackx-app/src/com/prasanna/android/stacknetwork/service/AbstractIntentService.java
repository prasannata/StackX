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

import java.io.Serializable;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.http.ClientException;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.ErrorIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractIntentService extends IntentService {
  public static final int ERROR = -1;

  public static final class VotingActions {
    public static final int UPVOTE = 0x702;
    public static final int UPVOTE_UNDO = 0x703;
    public static final int DOWNVOTE = 0x704;
    public static final int DOWNVOTE_UNDO = 0x705;
    public static final int UPVOTE_UNDO_DOWNVOTE = 0x706;
    public static final int DOWNVOTE_UNDO_UPVOTE = 0x707;

    public static final int RESULT_CODE_UPVOTE_SUCCESS = 0x708;
    public static final int RESULT_CODE_UPVOTE_UNDO_SUCCESS = 0x709;
    public static final int RESULT_CODE_DOWNVOTE_SUCCESS = 0x710;
    public static final int RESULT_CODE_DOWNVOTE_UNDO_SUCCESS = 0x711;
    public static final int RESULT_CODE_DOWNVOTE_UNDO_UPVOTE_SUCCESS = 0x712;
    public static final int RESULT_CODE_UPVOTE_UNDO_DOWNVOTE_SUCCESS = 0x713;
  }

  public AbstractIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (!AppUtils.isNetworkAvailable(getApplicationContext()))
      throw new ClientException(ClientException.ClientErrorCode.NO_NETWORK);
  }

  protected void broadcastHttpErrorIntent(StackXError error) {
    Intent broadcastIntent = new Intent();
    broadcastIntent.setAction(ErrorIntentAction.HTTP_ERROR.getAction());
    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    broadcastIntent.putExtra(StringConstants.ERROR, error);
    sendBroadcast(broadcastIntent);
  }

  protected void broadcastSerializableExtra(String action, String extraName, Serializable extra) {
    Intent broadcastIntent = new Intent();
    broadcastIntent.setAction(action);
    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    broadcastIntent.putExtra(extraName, extra);
    sendBroadcast(broadcastIntent);
  }
}

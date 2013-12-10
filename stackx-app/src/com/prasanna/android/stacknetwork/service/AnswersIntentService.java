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
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class AnswersIntentService extends AbstractIntentService {
    private static final String TAG = AnswersIntentService.class.getSimpleName();
    public static final int GET_ANSWER = 0x601;
    public static final int GET_ANSWERS = 0x502;

    private QuestionServiceHelper questionService = QuestionServiceHelper.getInstance();

    public AnswersIntentService() {
        this("UserQuestionsService");
    }

    public AnswersIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
        final int action = intent.getIntExtra(StringConstants.ACTION, -1);
        final long id = intent.getLongExtra(StringConstants.ANSWER_ID, -1);
        final String site = intent.getStringExtra(StringConstants.SITE);
        Bundle bundle = new Bundle();

        try {
            super.onHandleIntent(intent);

            switch (action) {
                case GET_ANSWER:
                    bundle.putSerializable(StringConstants.ANSWER, questionService.getAnswer(id, site));
                    receiver.send(GET_ANSWER, bundle);
                    break;

                default:
                    LogWrapper.d(TAG, "Unknown action: " + action);
                    break;
            }

        }
        catch (AbstractHttpException e) {
            bundle.putSerializable(StringConstants.EXCEPTION, e);
            receiver.send(ERROR, bundle);
        }
    }
}

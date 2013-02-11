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

import android.content.Context;
import android.database.SQLException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.sqlite.ProfileDAO;
import com.prasanna.android.stacknetwork.utils.IntegerConstants;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class MyProfileService extends AbstractStackxService
{
    private static final String TAG = TagsService.class.getSimpleName();

    private final static class ServiceHandler extends Handler
    {
        private OnHandlerComplete onHandlerComplete;
        private Context context;

        public ServiceHandler(Looper looper, Context context, OnHandlerComplete onHandlerComplete)
        {
            super(looper);
            this.context = context;
            this.onHandlerComplete = onHandlerComplete;
        }

        @Override
        public void handleMessage(Message msg)
        {
            ProfileDAO profileDAO = new ProfileDAO(context);

            try
            {
                profileDAO.open();
                User me = profileDAO.getMe(OperatingSite.getSite().apiSiteParameter);

                if (me == null || System.currentTimeMillis() - me.lastUpdateTime > IntegerConstants.MS_IN_AN_HOUR)
                {
                    Log.d(TAG, "Get my profile");
                    StackXPage<User> userPage = UserServiceHelper.getInstance().getMe();
                    if (userPage != null && userPage.items != null && !userPage.items.isEmpty())
                    {
                        profileDAO.deleteMe(OperatingSite.getSite().apiSiteParameter);
                        profileDAO.insert(OperatingSite.getSite().apiSiteParameter, userPage.items.get(0), true);
                        SharedPreferencesUtil.setLong(context, StringConstants.USER_ID, userPage.items.get(0).id);
                    }
                }
                else
                {
                    Log.d(TAG, "Profile fetched less than an hour ago. Using it");
                }
            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                profileDAO.close();
            }

            onHandlerComplete.onHandleMessageFinish(msg);
        }
    }

    @Override
    protected Handler getServiceHandler(Looper looper)
    {
        return new ServiceHandler(looper, getApplicationContext(), new OnHandlerComplete()
        {
            @Override
            public void onHandleMessageFinish(Message message, Object... args)
            {
                setRunning(false);
                MyProfileService.this.stopSelf(message.arg1);
            }
        });
    }
}

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

import java.util.LinkedHashSet;

import android.content.Context;
import android.database.SQLException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.Tag;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.utils.LogWrapper;

public class TagsService extends AbstractStackxService
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
            LinkedHashSet<Tag> tags = null;
            try
            {
                if (tagsOlderThanDay())
                {
                    boolean registeredUser = AppUtils.inAuthenticatedRealm(context);
                    tags = UserServiceHelper.getInstance().getTags(OperatingSite.getSite().apiSiteParameter, 1, 100,
                                    registeredUser);

                    if (tags == null || tags.isEmpty())
                        tags = UserServiceHelper.getInstance().getTags(OperatingSite.getSite().apiSiteParameter, 1,
                                        100, !registeredUser);

                    persistTags(tags);
                }
            }
            catch (AbstractHttpException e)
            {
                LogWrapper.e(TAG, "Error fetching tags: " + e.getMessage());
            }

            onHandlerComplete.onHandleMessageFinish(msg);
        }

        private boolean tagsOlderThanDay()
        {
            long MILLISECONDS_IN_DAY = 86400000L;

            long lastUpdateTime = 0L;

            TagDAO tagDAO = new TagDAO(context);
            try
            {
                tagDAO.open();
                lastUpdateTime = tagDAO.getLastUpdateTime(OperatingSite.getSite().apiSiteParameter);
            }
            catch (SQLException e)
            {
                LogWrapper.d(TAG, e.getMessage());
            }
            finally
            {
                tagDAO.close();
            }

            LogWrapper.d(TAG, "Tags last updated: " + lastUpdateTime);

            return (System.currentTimeMillis() - lastUpdateTime >= MILLISECONDS_IN_DAY);
        }

        private void persistTags(LinkedHashSet<Tag> result)
        {
            TagDAO tagDao = new TagDAO(context);

            try
            {
                tagDao.open();
                tagDao.deleteTagsFromServerForSite(OperatingSite.getSite().apiSiteParameter);
                tagDao.insert(OperatingSite.getSite().apiSiteParameter, result);
                LogWrapper.d(TAG, "Inserting tags into db for " + OperatingSite.getSite().apiSiteParameter);
            }
            catch (SQLException e)
            {
                LogWrapper.e(TAG, e.getMessage());
            }
            finally
            {
                tagDao.close();
            }
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
                TagsService.this.stopSelf(message.arg1);
                
                notifyWaitingObjectsOnComplete();
                
                LogWrapper.d(TAG, "Finished executing TagsService");
                setRunning(false);
            }
        });
    }
}

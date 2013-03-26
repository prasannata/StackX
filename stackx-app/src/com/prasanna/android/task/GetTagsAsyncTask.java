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

package com.prasanna.android.task;

import java.util.LinkedHashSet;

import android.database.SQLException;
import android.os.AsyncTask;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.Tag;
import com.prasanna.android.stacknetwork.service.TagsService;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.utils.LogWrapper;

public class GetTagsAsyncTask extends AsyncTask<Void, Void, LinkedHashSet<Tag>>
{
    private final String TAG = GetTagsAsyncTask.class.getSimpleName();

    private final AsyncTaskCompletionNotifier<LinkedHashSet<Tag>> taskCompletionNotifier;
    private final TagDAO tagDao;

    public GetTagsAsyncTask(AsyncTaskCompletionNotifier<LinkedHashSet<Tag>> taskCompletionNotifier, TagDAO tagsDbAdapter)
    {
        super();

        this.taskCompletionNotifier = taskCompletionNotifier;
        this.tagDao = tagsDbAdapter;
    }

    @Override
    protected LinkedHashSet<Tag> doInBackground(Void... params)
    {
        if (TagsService.isRunning())
            waitForServiceToComplete();

        try
        {
            return getTagsFromDb(OperatingSite.getSite().apiSiteParameter);
        }
        catch (AbstractHttpException e)
        {
            LogWrapper.e(TAG, "Error fetching tags: " + e.getMessage());
        }

        return null;
    }

    private void waitForServiceToComplete()
    {
        TagsService.registerForCompleteNotification(this);

        try
        {
            synchronized (this)
            {
                wait();
            }
        }
        catch (InterruptedException e)
        {
            LogWrapper.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(LinkedHashSet<Tag> result)
    {
        if (taskCompletionNotifier != null)
            taskCompletionNotifier.notifyOnCompletion(result);
    }

    private LinkedHashSet<Tag> getTagsFromDb(String site)
    {
        try
        {
            tagDao.open();
            return tagDao.getTagSet(site);

        }
        catch (SQLException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        finally
        {
            tagDao.close();
        }

        return null;
    }
}

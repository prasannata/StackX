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

package com.prasanna.android.task;

import java.util.LinkedHashSet;

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import com.prasanna.android.http.ServerException;
import com.prasanna.android.stacknetwork.service.UserServiceHelper;
import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.OperatingSite;

public class GetTagsAsyncTask extends AsyncTask<Void, Void, LinkedHashSet<String>>
{
    private final String TAG = GetTagsAsyncTask.class.getSimpleName();

    private final AsyncTaskCompletionNotifier<LinkedHashSet<String>> taskCompletionNotifier;
    private final boolean registeredUser;
    private final TagDAO tagsDbAdapter;
    private boolean persistTags = false;

    public GetTagsAsyncTask(AsyncTaskCompletionNotifier<LinkedHashSet<String>> taskCompletionNotifier,
                    TagDAO tagsDbAdapter, boolean registeredUser)
    {
        super();

        this.taskCompletionNotifier = taskCompletionNotifier;
        this.tagsDbAdapter = tagsDbAdapter;
        this.registeredUser = registeredUser;
    }

    @Override
    protected LinkedHashSet<String> doInBackground(Void... params)
    {
        LinkedHashSet<String> tags = null;
        try
        {
            tags = getTagsFromDb(OperatingSite.getSite().name);
            
            if (tags == null || tags.isEmpty())
            {
                tags = UserServiceHelper.getInstance().getTags(OperatingSite.getSite().apiSiteParameter, 1, 100,
                                registeredUser);

                if (tags == null || tags.isEmpty())
                    tags = UserServiceHelper.getInstance().getTags(OperatingSite.getSite().apiSiteParameter, 1, 100,
                                    !registeredUser);

                persistTags = true;
            }
        }
        catch (ServerException e)
        {
            Log.e(TAG, "Error fetching tags: " + e.getMessage());
        }
        return tags;
    }

    @Override
    protected void onPostExecute(LinkedHashSet<String> result)
    {
        if (taskCompletionNotifier != null)
        {
            taskCompletionNotifier.notifyOnCompletion(result);
        }

        if (persistTags)
            persistTags(result);

        super.onPostExecute(result);
    }

    private LinkedHashSet<String> getTagsFromDb(String site)
    {
        try
        {
            tagsDbAdapter.open();
            return tagsDbAdapter.getTags(OperatingSite.getSite().apiSiteParameter);
        }
        catch (SQLException e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            tagsDbAdapter.close();
        }
        
        return null;
    }
    
    private void persistTags(LinkedHashSet<String> result)
    {
        try
        {
            tagsDbAdapter.open();
            tagsDbAdapter.insert(OperatingSite.getSite().apiSiteParameter, result);
        }
        catch (SQLException e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            tagsDbAdapter.close();
        }
    }
}

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

import java.util.ArrayList;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.service.UserService;

public class FetchTagsAsyncTask extends AsyncTask<Integer, Void, ArrayList<String>>
{
    private final AsyncTaskCompletionNotifier<ArrayList<String>> taskCompletionNotifier;

    public FetchTagsAsyncTask(AsyncTaskCompletionNotifier<ArrayList<String>> taskCompletionNotifier)
    {
	this.taskCompletionNotifier = taskCompletionNotifier;
    }

    @Override
    protected ArrayList<String> doInBackground(Integer... params)
    {
	return UserService.getInstance().getTags(1);
    }

    @Override
    protected void onPostExecute(ArrayList<String> result)
    {
	if (taskCompletionNotifier != null)
	{
	    taskCompletionNotifier.notifyOnCompletion(result);
	}
	
	super.onPostExecute(result);
    }
}

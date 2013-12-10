/*
    Copyright 2012 Prasanna Thirumalai
    
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

import java.io.File;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

public class ReadObjectAsyncTask extends AsyncTask<Void, Void, ArrayList<Object>> {
    private final File directory;
    private final String fileName;
    private final AsyncTaskCompletionNotifier<ArrayList<Object>> notifier;

    public ReadObjectAsyncTask(File directory, String fileName, AsyncTaskCompletionNotifier<ArrayList<Object>> notifier) {
        this.directory = directory;
        this.fileName = fileName;
        this.notifier = notifier;
    }

    @Override
    protected ArrayList<Object> doInBackground(Void... params) {
        if (fileName == null) {
            return SharedPreferencesUtil.readObjects(directory);
        }
        else {
            ArrayList<Object> objects = new ArrayList<Object>();
            objects.add(SharedPreferencesUtil.readObject(new File(directory, fileName)));
            return objects;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Object> objects) {
        if (notifier != null) {
            notifier.notifyOnCompletion(objects);
        }
    }

}

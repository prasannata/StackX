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

import java.io.File;

import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

public class WriteObjectAsyncTask extends AsyncTask<Object, Void, Void> {
    private final File directory;
    private final String fileName;

    public WriteObjectAsyncTask(File directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (params != null && params.length == 1) {
            SharedPreferencesUtil.writeObject(params[0], directory, fileName);
        }

        return null;
    }
}

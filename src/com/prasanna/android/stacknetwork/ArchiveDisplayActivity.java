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

package com.prasanna.android.stacknetwork;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.ReadObjectAsyncTask;

public class ArchiveDisplayActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = ArchiveDisplayActivity.class.getSimpleName();

    private ArrayList<Object> questions;
    private LinearLayout container;

    private class CacheReadCompletionNotifier implements AsyncTaskCompletionNotifier<ArrayList<Object>>
    {
        @Override
        public void notifyOnCompletion(ArrayList<Object> result)
        {
            Log.d(TAG, "Read cache task complete");
            questions = result;
            displayQuestions();
        }

        private void displayQuestions()
        {
            if (questions != null && questions.isEmpty() == false)
            {
                Log.d(TAG, "displaying saved questions");

                for (Object question : questions)
                {
                    LinearLayout row = QuestionRowLayoutBuilder.getInstance().build(getLayoutInflater(),
                            ArchiveDisplayActivity.this, true, (Question) question);
                    container.addView(row);
                }
            }
            else
            {
                FrameLayout emptyDisplayLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.empty_items, null);
                TextView textView = (TextView) emptyDisplayLayout.findViewById(R.id.emptyStatus);
                textView.setText("Empty. Long press on title while viewing a question.");
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER;
                container.addView(emptyDisplayLayout, params);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ll_whitebg_vertical);

        container = (LinearLayout) findViewById(R.id.fragmentContainer);

        refresh();
    }

    @Override
    public void refresh()
    {
        File directory = new File(getCacheDir(), StringConstants.QUESTIONS);
        ReadObjectAsyncTask asyncTask = new ReadObjectAsyncTask(directory, null, new CacheReadCompletionNotifier());
        asyncTask.execute((Void) null);
    }

    @Override
    public Context getCurrentContext()
    {
        return ArchiveDisplayActivity.this;
    }
}

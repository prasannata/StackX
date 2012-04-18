package com.prasanna.android.stacknetwork;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
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

public class ArchiveDisplayActivity extends Activity
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
        File directory = new File(getCacheDir(), StringConstants.QUESTIONS);
        ReadObjectAsyncTask asyncTask = new ReadObjectAsyncTask(directory, null, new CacheReadCompletionNotifier());
        asyncTask.execute((Void) null);
    }
}

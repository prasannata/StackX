package com.prasanna.android.stacknetwork;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.CacheReadObjectAsyncTask;

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
	    if (questions != null)
	    {
		Log.d(TAG, "displaying saved questions");

		for (Object question : questions)
		{
		    LinearLayout row = QuestionRowLayoutBuilder.getInstance().build(getLayoutInflater(),
			            ArchiveDisplayActivity.this, true, (Question) question);
		    container.addView(row);
		}
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
	CacheReadObjectAsyncTask asyncTask = new CacheReadObjectAsyncTask(directory, null,
	                new CacheReadCompletionNotifier());
	asyncTask.execute((Void) null);
    }
}

package com.prasanna.android.task;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.os.AsyncTask;

import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.service.UserService;

public class FetchUserProfileInfoAndWriteToFileTask extends AsyncTask<String, Void, User>
{
    private static final String FILE_NAME = "";

    private final Context context;

    public FetchUserProfileInfoAndWriteToFileTask(Context context)
    {
	this.context = context;
    }

    @Override
    protected User doInBackground(String... params)
    {
	if (params != null && params.length == 1)
	{
	    return UserService.getInstance().getLoggedInUser(params[0]);
	}

	return null;
    }

    @Override
    protected void onPostExecute(User user)
    {
	try
	{
	    FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
	    if (fos != null)
	    {
		ObjectOutputStream objectOuputStream = new ObjectOutputStream(fos);
		objectOuputStream.writeObject(user);
		objectOuputStream.close();
	    }
	}
	catch (FileNotFoundException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}

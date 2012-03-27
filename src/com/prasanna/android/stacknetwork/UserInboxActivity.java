package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

public class UserInboxActivity extends Activity
{
    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}

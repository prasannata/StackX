package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.os.Bundle;

import com.prasanna.android.stacknetwork.fragment.SettingsFragment;

public class SettingsActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}

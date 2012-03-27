package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	handleLogin();
	handleNoLogin();
    }

    private void handleLogin()
    {
	Button loginButton = (Button) findViewById(R.id.login_button);
	loginButton.setOnClickListener(new View.OnClickListener()
	{
	    public void onClick(View view)
	    {
		Toast.makeText(LoginActivity.this, "Not supported at this time. Please skip login",
		        Toast.LENGTH_SHORT).show();
	    }
	});
    }

    private void handleNoLogin()
    {
	TextView skipLoginTextView = (TextView) findViewById(R.id.noLogin);
	skipLoginTextView.setOnClickListener(new View.OnClickListener()
	{

	    public void onClick(View view)
	    {
		Intent listStackNetworkIntent = new Intent(view.getContext(),
		        StackNetworkListActivity.class);
		listStackNetworkIntent.putExtra("allSites", true);
		startActivity(listStackNetworkIntent);

	    }
	});
    }
}

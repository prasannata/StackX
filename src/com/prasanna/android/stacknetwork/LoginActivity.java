package com.prasanna.android.stacknetwork;

import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	if (sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null) == null)
	{
	    setContentView(R.layout.main);
	    handleLogin();
	    handleNoLogin();
	}
	else
	{
	    startActivity(IntentUtils.createSiteListIntent(getApplicationContext()));
	}
    }

    private void handleLogin()
    {
	Button loginButton = (Button) findViewById(R.id.login_button);
	loginButton.setOnClickListener(new View.OnClickListener()
	{
	    public void onClick(View view)
	    {
		Intent oAuthIntent = new Intent(view.getContext(), OAuthActivity.class);
		startActivity(oAuthIntent);
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
		startActivity(IntentUtils.createSiteListIntent(view.getContext()));
	    }
	});
    }
}

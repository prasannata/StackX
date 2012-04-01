package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractUserActionBarActivity extends Activity implements SearchView.OnQueryTextListener
{
    private SearchView searchView;
    private SharedPreferences sharedPreferences;
    private String accessToken;

    public abstract void refresh();

    public abstract Context getCurrentAppContext();

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	getActionBar().setTitle(OperatingSite.getSite().name);
	MenuInflater menuInflater = getMenuInflater();
	menuInflater.inflate(R.menu.action_menu, menu);
	MenuItem searchItem = menu.findItem(R.id.menu_search);
	searchView = (SearchView) searchItem.getActionView();
	searchView.setOnQueryTextListener(this);
	searchView.setQueryHint("Search in title");

	if (isAuthenticatedRealm() == false)
	{
	    Log.d("AbstractUserActionBarActivity", "Not in authenticated realm");

	    MenuItem menuOptions = menu.findItem(R.id.menu_options);
	    SubMenu subMenu = menuOptions.getSubMenu();

	    subMenu.removeItem(R.id.menu_profile);
	    subMenu.removeItem(R.id.menu_option_inbox);
	}

	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	switch (item.getItemId())
	{
	    case android.R.id.home:
		Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
		break;

	    case R.id.menu_refresh:
		refresh();
		break;

	    case R.id.menu_search:
		break;

	    case R.id.menu_profile:
		Intent userProfileIntent = IntentUtils
		                .createUserProfileIntent(getCurrentAppContext(), getAccessToken());
		startActivity(userProfileIntent);
		break;
	}
	return super.onOptionsItemSelected(item);
    }

    public boolean isAuthenticatedRealm()
    {
	if (sharedPreferences == null)
	{
	    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    accessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);
	}

	return (accessToken != null);
    }

    public String getAccessToken()
    {
	return accessToken;
    }

}

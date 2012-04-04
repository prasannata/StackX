package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.app.SearchManager;
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

import com.prasanna.android.stacknetwork.model.User.UserType;
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
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(OperatingSite.getSite().name);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search in title");

        if (isAuthenticatedRealm() == false || OperatingSite.getSite().userType == null
                || OperatingSite.getSite().userType.equals(UserType.REGISTERED) == false)
        {
            Log.d("AbstractUserActionBarActivity", "Not in authenticated realm");

            MenuItem menuOptions = menu.findItem(R.id.menu_options);
            SubMenu subMenu = menuOptions.getSubMenu();

            subMenu.removeItem(R.id.menu_profile);
            subMenu.removeItem(R.id.menu_option_inbox);
            subMenu.removeItem(R.id.menu_option_logout);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            Intent intent = new Intent(this, QuestionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;

        case R.id.menu_refresh:
            refresh();
            break;

        case R.id.menu_search:
            break;

        case R.id.menu_profile:
            Intent userProfileIntent = IntentUtils.createUserProfileIntent(getCurrentAppContext(), getAccessToken());
            startActivity(userProfileIntent);
            break;

        case R.id.menu_option_inbox:
            Intent userInboxIntent = new Intent(getCurrentAppContext(), UserInboxActivity.class);
            userInboxIntent.putExtra(StringConstants.ACCESS_TOKEN, getAccessToken());
            startActivity(userInboxIntent);
            break;
        case R.id.menu_option_change_site:
            Intent siteListIntent = new Intent(this, StackNetworkListActivity.class);
            startActivity(siteListIntent);
            break;

        case R.id.menu_option_logout:
            Intent logoutIntent = new Intent(this, LogoutActivity.class);
            startActivity(logoutIntent);
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

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Intent intent = new Intent(this, QuestionSearchResultsActivity.class);
        intent.putExtra(SearchManager.QUERY, query);
        startActivity(intent);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String paramString)
    {
        return false;
    }
}

/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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

import java.util.HashMap;

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
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractUserActionBarActivity extends Activity implements SearchView.OnQueryTextListener
{
    protected SearchView searchView;
    private String accessToken;

    public abstract void refresh();

    public abstract Context getCurrentContext();

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

        MenuItem menuOptions = menu.findItem(R.id.menu_options);
        SubMenu subMenu = menuOptions.getSubMenu();

        if (isAuthenticatedRealm() == false || OperatingSite.getSite().userType == null
                || OperatingSite.getSite().userType.equals(UserType.REGISTERED) == false)
        {
            Log.d("AbstractUserActionBarActivity", "Not in authenticated realm");

            subMenu.removeItem(R.id.menu_profile);
            subMenu.removeItem(R.id.menu_option_inbox);
            subMenu.removeItem(R.id.menu_option_logout);
        }
        else
        {
            subMenu.removeItem(R.id.menu_option_login);
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
                Intent userProfileIntent = IntentUtils.createUserProfileIntent(getCurrentContext(), getAccessToken());
                startActivity(userProfileIntent);
                break;
            case R.id.menu_option_archive:
                Intent archiveIntent = new Intent(this, ArchiveDisplayActivity.class);
                startActivity(archiveIntent);
                break;
            case R.id.menu_option_inbox:
                Intent userInboxIntent = new Intent(getCurrentContext(), UserInboxActivity.class);
                userInboxIntent.putExtra(StringConstants.ACCESS_TOKEN, getAccessToken());
                startActivity(userInboxIntent);
                break;
            case R.id.menu_option_change_site:
                Intent siteListIntent = new Intent(this, StackNetworkListActivity.class);
                startActivity(siteListIntent);
                break;
            case R.id.menu_option_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_option_login:
                Intent oAuthIntent = new Intent(this, OAuthActivity.class);
                CacheUtils.clear(getApplicationContext());
                startActivity(oAuthIntent);
                break;
            case R.id.menu_option_logout:
                Intent logoutIntent = new Intent(this, LogoutActivity.class);
                startActivity(logoutIntent);
                break;
            case R.id.menu_option_test_gen_notify:
                Intent notifyIntent = new Intent(UserIntentAction.NEW_MSG.name());
                HashMap<String, Integer> newMsgCount = new HashMap<String, Integer>();
                newMsgCount.put("stackoverflow", 2);
                newMsgCount.put("serverfault", 2);
                notifyIntent.putExtra(UserIntentAction.NEW_MSG.getExtra(), newMsgCount);
                notifyIntent.putExtra(UserIntentAction.TOTAL_NEW_MSGS.getExtra(), 4);
                sendBroadcast(notifyIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isAuthenticatedRealm()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        accessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);

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
        return true;
    }

    @Override
    public boolean onQueryTextChange(String paramString)
    {
        return false;
    }
}

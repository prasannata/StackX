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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.prasanna.android.cache.LRU;
import com.prasanna.android.listener.OnDiscardOptionListener;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.FetchImageAsyncTask;

public abstract class AbstractUserActionBarActivity extends Activity implements SearchView.OnQueryTextListener
{
    private String accessToken;
    protected SearchView searchView;
    private static LRU<String, Bitmap> iconCache = new LRU<String, Bitmap>(5);
    private OnDiscardOptionListener discardOptionListener;

    protected abstract void refresh();

    protected abstract Context getCurrentContext();

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	getActionBar().setTitle(OperatingSite.getSite().name);

	if (iconCache.containsKey(OperatingSite.getSite().name))
	{
	    setActionBarIcon(iconCache.get(OperatingSite.getSite().name));
	}
	else
	{
	    loadIcon();
	}

	getMenuInflater().inflate(R.menu.action_menu, menu);

	searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	searchView.setOnQueryTextListener(this);
	searchView.setQueryHint(getString(R.string.searchInTitle));

	if (isAuthenticatedRealm())
	{
	    Log.d("AbstractUserActionBarActivity", "In authenticated realm");
	    menu.removeItem(R.id.menu_option_login);

	    if (OperatingSite.getSite().userType == null
		            || !OperatingSite.getSite().userType.equals(UserType.REGISTERED))
	    {
		menu.removeItem(R.id.menu_my_profile);
		menu.removeItem(R.id.menu_my_inbox);
	    }

	}
	else
	{
	    menu.removeItem(R.id.menu_my_profile);
	    menu.removeItem(R.id.menu_my_inbox);
	    menu.removeItem(R.id.menu_option_logout);
	}

	onCreateOptionsMenuPostProcess(menu);

	return super.onCreateOptionsMenu(menu);
    }

    private void loadIcon()
    {
	FetchImageAsyncTask fetchImageAsyncTask = new FetchImageAsyncTask(new AsyncTaskCompletionNotifier<Bitmap>()
	{
	    @Override
	    public void notifyOnCompletion(Bitmap result)
	    {
		setActionBarIcon(result);
		iconCache.put(OperatingSite.getSite().name, result);
	    }

	});

	fetchImageAsyncTask.execute(OperatingSite.getSite().iconUrl);
    }

    private void setActionBarIcon(Bitmap result)
    {
	getActionBar().setIcon(new BitmapDrawable(getResources(), result));
	getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	switch (item.getItemId())
	{
	    case android.R.id.home:
		if (!getClass().getSimpleName().equals("QuestionsActivity"))
		{
		    Intent intent = new Intent(this, QuestionsActivity.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);
		}
		break;
	    case R.id.menu_refresh:
		refresh();
		break;
	    case R.id.menu_search:
		break;
	    case R.id.menu_my_profile:
		Intent userProfileIntent = IntentUtils.createUserProfileIntent(getCurrentContext(), getAccessToken());
		startActivity(userProfileIntent);
		break;
	    case R.id.menu_option_archive:
		Intent archiveIntent = new Intent(this, ArchiveDisplayActivity.class);
		startActivity(archiveIntent);
		break;
	    case R.id.menu_my_inbox:
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
	    case R.id.menu_discard:
		if (discardOptionListener != null)
		{
		    discardOptionListener.onDiscardOptionClick();
		}
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

    protected void onCreateOptionsMenuPostProcess(Menu menu)
    {
	Log.d(getClass().getSimpleName(),
	                "No post processing of menu options. Override this method if any post processing is necessary.");
    }

    protected void setOnDiscardOptionClick(OnDiscardOptionListener discardOptionListener)
    {
	this.discardOptionListener = discardOptionListener;
    }

    public String getAccessToken()
    {
	return accessToken;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
	return false;
    }

    @Override
    public boolean onQueryTextChange(String paramString)
    {
	return false;
    }
}

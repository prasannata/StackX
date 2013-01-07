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

import java.util.ArrayList;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SearchView;

import com.prasanna.android.listener.OnDiscardOptionListener;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.IconCache;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetImageAsyncTask;

public abstract class AbstractUserActionBarActivity extends Activity
{
    private static final String TAG = AbstractUserActionBarActivity.class.getSimpleName();

    private String accessToken;
    protected SearchView searchView;
    private IconCache iconCache = IconCache.getInstance();
    private OnDiscardOptionListener discardOptionListener;
    private MenuItem refreshMenuItem;

    protected abstract void refresh();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        refreshOperationSite();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferencesUtil.loadAccessToken(getApplicationContext());
        refreshOperationSite();
    }

    public void refreshOperationSite()
    {
        if (OperatingSite.getSite() == null)
            OperatingSite.setSite(SharedPreferencesUtil.getDefaultSite(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getActionBar().setTitle(OperatingSite.getSite().name);

        if (iconCache.containsKey(OperatingSite.getSite().name))
            setActionBarIcon(iconCache.get(OperatingSite.getSite().name));
        else
            loadIcon();

        getMenuInflater().inflate(R.menu.action_menu, menu);

        setupSearchView(menu);

        if (isAuthenticatedRealm())
            setupActionBarForAuthenticatedUser(menu);
        else
            setupActionBarForAnyUser(menu);

        refreshMenuItem = menu.findItem(R.id.menu_refresh);

        return true;
    }

    private void setupActionBarForAnyUser(Menu menu)
    {
        menu.removeItem(R.id.menu_my_profile);
        menu.removeItem(R.id.menu_my_inbox);
    }

    private void setupActionBarForAuthenticatedUser(Menu menu)
    {
        Log.d(TAG, "In authenticated realm");

        if (OperatingSite.getSite().userType == null
                || !OperatingSite.getSite().userType.equals(UserType.REGISTERED))
        {
            menu.removeItem(R.id.menu_my_profile);
            menu.removeItem(R.id.menu_my_inbox);
        }
    }

    private void setupSearchView(Menu menu)
    {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    private void loadIcon()
    {
        GetImageAsyncTask fetchImageAsyncTask = new GetImageAsyncTask(
                new AsyncTaskCompletionNotifier<Bitmap>()
                {
                    @Override
                    public void notifyOnCompletion(Bitmap result)
                    {
                        setActionBarIcon(result);
                        iconCache.add(OperatingSite.getSite().name, result);
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
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_search:
                return false;
            case R.id.menu_my_profile:
                Intent userProfileIntent = new Intent(getApplicationContext(),
                        UserProfileActivity.class);
                userProfileIntent.putExtra(StringConstants.ME, true);
                startActivity(userProfileIntent);
                return true;
            case R.id.menu_option_archive:
                Intent archiveIntent = new Intent(this, ArchiveDisplayActivity.class);
                startActivity(archiveIntent);
                return true;
            case R.id.menu_my_inbox:
                Intent userInboxIntent = new Intent(getApplicationContext(),
                        UserInboxActivity.class);
                userInboxIntent.putExtra(StringConstants.ACCESS_TOKEN, getAccessToken());
                startActivity(userInboxIntent);
                return true;
            case R.id.menu_option_change_site:
                Intent siteListIntent = new Intent(this, StackNetworkListActivity.class);
                startActivity(siteListIntent);
                return true;
            case R.id.menu_option_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_discard:
                if (discardOptionListener != null)
                {
                    discardOptionListener.onDiscardOptionClick();
                }
                return true;
            case R.id.menu_option_test_gen_notify:
                Intent notifyIntent = new Intent(UserIntentAction.NEW_MSG.getAction());
                ArrayList<InboxItem> unreadInboxItems = new ArrayList<InboxItem>();
                InboxItem inboxItem = new InboxItem();
                inboxItem.itemType = ItemType.NEW_ANSWER;
                inboxItem.title = "Python unit testing functions by using mocks";
                inboxItem.body = "You can use mock library by Michael Foord, which is part Python 3. It makes this kind of mocking ...";
                unreadInboxItems.add(inboxItem);
                notifyIntent.putExtra(UserIntentAction.NEW_MSG.getAction(), unreadInboxItems);
                sendBroadcast(notifyIntent);
                return true;
        }

        return false;
    }

    public boolean isAuthenticatedRealm()
    {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        accessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);

        return (accessToken != null);
    }

    protected void setOnDiscardOptionClick(OnDiscardOptionListener discardOptionListener)
    {
        this.discardOptionListener = discardOptionListener;
    }

    protected void startRefreshAnimation()
    {
        ImageView refreshActionView = (ImageView) getLayoutInflater().inflate(
                R.layout.refresh_action_view, null);
        Animation rotation = AnimationUtils.loadAnimation(this, R.animator.rotate_360);
        rotation.setRepeatCount(Animation.INFINITE);
        refreshActionView.startAnimation(rotation);
        refreshMenuItem.setEnabled(false);
        refreshMenuItem.setActionView(refreshActionView);
    }

    protected void hideRefreshActionAnimation()
    {
        if (refreshMenuItem != null && refreshMenuItem.getActionView() != null)
        {
            refreshMenuItem.getActionView().clearAnimation();
            refreshMenuItem.setActionView(null);
            refreshMenuItem.setEnabled(true);
        }
    }

    public String getAccessToken()
    {
        return accessToken;
    }
}

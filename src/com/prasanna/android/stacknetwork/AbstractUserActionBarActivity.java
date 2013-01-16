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
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.prasanna.android.cache.BitmapCache;
import com.prasanna.android.listener.OnDiscardOptionListener;
import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.User.UserType;
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
    private BitmapCache iconCache = BitmapCache.getInstance();
    private OnDiscardOptionListener discardOptionListener;
    private boolean showingSearchFilters = false;
    private PopupWindow popupWindow;

    protected Menu actionBarMenu;

    protected abstract void refresh();

    protected abstract boolean shouldSearchViewBeEnabled();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        refreshOperationSite();
        initializeActionBar();
    }

    private void initializeActionBar()
    {
        if (getActionBar().getTitle() == null)
            getActionBar().setTitle(OperatingSite.getSite().name);

        if (iconCache.containsKey(OperatingSite.getSite().name))
            setActionBarHomeIcon(iconCache.get(OperatingSite.getSite().name));
        else
            loadIcon();
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
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.action_menu, menu);

        if (shouldSearchViewBeEnabled())
            setupSearchItem(menu);
        else
        {
            menu.removeItem(R.id.menu_search);
            menu.removeItem(R.id.menu_search_filter);
        }

        if (isAuthenticatedRealm())
            setupActionBarForAuthenticatedUser(menu);
        else
            setupActionBarForAnyUser(menu);

        this.actionBarMenu = menu;
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

        if (OperatingSite.getSite().userType == null || !OperatingSite.getSite().userType.equals(UserType.REGISTERED))
        {
            menu.removeItem(R.id.menu_my_profile);
            menu.removeItem(R.id.menu_my_inbox);
        }
    }

    private void setupSearchItem(final Menu menu)
    {
        searchView = setupSearchMenuItemAndGetActionView(menu);
        setupSearchActionView(menu);
        setupPopupForSearchOptions();
    }

    private void setupSearchActionView(final Menu menu)
    {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus && popupWindow.isShowing())
                {
                    popupWindow.dismiss();
                    menu.findItem(R.id.menu_search_filter).setIcon(R.drawable.expand);
                    showingSearchFilters = false;
                }
            }
        });
    }

    private SearchView setupSearchMenuItemAndGetActionView(final Menu menu)
    {
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchItem.setOnActionExpandListener(new OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                menu.findItem(R.id.menu_search_filter).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.menu_search_filter).setVisible(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                menu.findItem(R.id.menu_search_filter).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menu.findItem(R.id.menu_search_filter).setVisible(false);
                if (popupWindow.isShowing())
                    popupWindow.dismiss();
                menu.findItem(R.id.menu_search_filter).setIcon(R.drawable.expand);

                showingSearchFilters = false;
                return true;
            }
        });

        return ((SearchView) searchItem.getActionView());
    }

    private void setupPopupForSearchOptions()
    {
        RelativeLayout popupLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.search_options, null);
        popupWindow = new PopupWindow(popupLayout, RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        setupSearchPrefCheckBox((CheckBox) popupLayout.findViewById(R.id.searchInTitleCB),
                        SettingsFragment.KEY_PREF_SEARCH_IN_TITLE);

        setupSearchPrefCheckBox((CheckBox) popupLayout.findViewById(R.id.searchOnlyAnsweredCB),
                        SettingsFragment.KEY_PREF_SEARCH_ONLY_ANSWERED);

        setupSearchPrefCheckBox((CheckBox) popupLayout.findViewById(R.id.searchOnlyWithAnswersCB),
                        SettingsFragment.KEY_PREF_SEARCH_ONLY_WITH_ANSWERS);

    }

    private void setupSearchPrefCheckBox(final CheckBox checkBox, final String prefName)
    {
        checkBox.setChecked(SharedPreferencesUtil.isOn(getApplicationContext(), prefName, false));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferencesUtil.setOnOff(getApplicationContext(), prefName, isChecked);
            }
        });
    }

    private void loadIcon()
    {
        GetImageAsyncTask fetchImageAsyncTask = new GetImageAsyncTask(new AsyncTaskCompletionNotifier<Bitmap>()
        {
            @Override
            public void notifyOnCompletion(Bitmap result)
            {
                setActionBarHomeIcon(result);
                iconCache.add(OperatingSite.getSite().name, result);
            }
        });

        fetchImageAsyncTask.execute(OperatingSite.getSite().iconUrl);
    }

    private void setActionBarHomeIcon(Bitmap result)
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
                return onActionBarHomeButtonClick(item);
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_search_filter:
                showSearchFilterOptions(item);
                return true;
            case R.id.menu_my_profile:
                Intent userProfileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                userProfileIntent.putExtra(StringConstants.ME, true);
                startActivity(userProfileIntent);
                return true;
            case R.id.menu_option_archive:
                Intent archiveIntent = new Intent(this, ArchiveDisplayActivity.class);
                startActivity(archiveIntent);
                return true;
            case R.id.menu_my_inbox:
                Intent userInboxIntent = new Intent(getApplicationContext(), UserInboxActivity.class);
                userInboxIntent.putExtra(StringConstants.ACCESS_TOKEN, getAccessToken());
                startActivity(userInboxIntent);
                return true;
            case R.id.menu_option_change_site:
                Intent siteListIntent = new Intent(this, StackNetworkListActivity.class);
                siteListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                siteListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(siteListIntent);
                return true;
            case R.id.menu_option_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_discard:
                if (discardOptionListener != null)
                    discardOptionListener.onDiscardOptionClick();
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

    private void showSearchFilterOptions(MenuItem item)
    {
        if (showingSearchFilters)
        {
            item.setIcon(R.drawable.expand);
            popupWindow.dismiss();
            showingSearchFilters = false;
            searchView.requestFocus();
        }
        else
        {
            item.setIcon(R.drawable.navigation_collapse);
            searchView.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.showAsDropDown(searchView, 300, 0);
            showingSearchFilters = true;
            searchView.clearFocus();
        }

    }

    protected boolean onActionBarHomeButtonClick(MenuItem item)
    {
        if (this instanceof QuestionsActivity)
            finish();

        Intent intent = new Intent(this, QuestionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return true;
    }

    public boolean isAuthenticatedRealm()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        accessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);

        return (accessToken != null);
    }

    protected void setOnDiscardOptionClick(OnDiscardOptionListener discardOptionListener)
    {
        this.discardOptionListener = discardOptionListener;
    }

    public String getAccessToken()
    {
        return accessToken;
    }
}

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

package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.sqlite.ProfileDAO;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;
import com.prasanna.android.stacknetwork.sqlite.UserAccountsDAO;
import com.prasanna.android.stacknetwork.utils.DbRequestThreadExecutor;
import com.prasanna.android.stacknetwork.utils.IntegerConstants;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserIntentService extends AbstractIntentService
{
    private static final String TAG = UserIntentService.class.getSimpleName();
    public static final int GET_USER_PROFILE = 0x1;
    public static final int GET_USER_QUESTIONS = 0x2;
    public static final int GET_USER_ANSWERS = 0x3;
    public static final int GET_USER_INBOX = 0x4;
    public static final int GET_USER_UNREAD_INBOX = 0x5;
    public static final int GET_USER_SITES = 0x6;
    public static final int GET_USER_FAVORITES = 0x7;
    public static final int CHECK_WRITE_PERMISSION = 0x101;
    public static final int DEAUTH_APP = 0x201;

    private UserServiceHelper userService = UserServiceHelper.getInstance();

    public UserIntentService()
    {
        this(TAG);
    }

    public UserIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        final ResultReceiver receiver = intent.getParcelableExtra(StringConstants.RESULT_RECEIVER);
        final int action = intent.getIntExtra(StringConstants.ACTION, -1);
        final int page = intent.getIntExtra(StringConstants.PAGE, 1);
        final boolean me = intent.getBooleanExtra(StringConstants.ME, false);
        final long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
        Bundle bundle = new Bundle();

        try
        {
            super.onHandleIntent(intent);

            switch (action)
            {
                case GET_USER_PROFILE:
                    Log.d(TAG, "getUserDetail");
                    boolean refresh = intent.getBooleanExtra(StringConstants.REFRESH, false);
                    bundle.putSerializable(StringConstants.USER, getUserDetail(me, userId, refresh, page));
                    bundle.putSerializable(StringConstants.USER_ACCOUNTS, getUserAccounts(me, userId));
                    receiver.send(GET_USER_PROFILE, bundle);
                    break;
                case GET_USER_QUESTIONS:
                    Log.d(TAG, "getQuestions");
                    bundle.putSerializable(StringConstants.QUESTIONS, getQuestions(me, userId, page));
                    receiver.send(GET_USER_QUESTIONS, bundle);
                    break;
                case GET_USER_ANSWERS:
                    Log.d(TAG, "getAnswers");
                    bundle.putSerializable(StringConstants.ANSWERS, getAnswers(me, userId, page));
                    receiver.send(GET_USER_ANSWERS, bundle);
                    break;
                case GET_USER_INBOX:
                    bundle.putSerializable(StringConstants.INBOX_ITEMS, userService.getInbox(page));
                    receiver.send(GET_USER_INBOX, bundle);
                    break;
                case GET_USER_UNREAD_INBOX:
                    getUnreadInboxItems(intent);
                    break;
                case GET_USER_SITES:
                    bundle.putSerializable(StringConstants.SITES,
                                    getUserSites(receiver, intent.getBooleanExtra(StringConstants.AUTHENTICATED, me)));
                    receiver.send(GET_USER_SITES, bundle);
                    break;
                case GET_USER_FAVORITES:
                    Log.d(TAG, "getQuestions");
                    bundle.putSerializable(StringConstants.QUESTIONS, getFavorites(me, userId, page));
                    receiver.send(GET_USER_FAVORITES, bundle);
                    break;
                case DEAUTH_APP:
                    deauthenticateApp(intent.getStringExtra(StringConstants.ACCESS_TOKEN));
                    break;
                default:
                    Log.e(TAG, "Unknown action: " + action);
                    break;
            }

        }
        catch (AbstractHttpException e)
        {
            bundle.putSerializable(StringConstants.EXCEPTION, e);
            receiver.send(ERROR, bundle);
        }
    }

    private StackXPage<User> getUserDetail(boolean me, long userId, boolean refresh, int page)
    {
        if (me)
        {
            ProfileDAO profileDAO = new ProfileDAO(getApplicationContext());
            StackXPage<User> userPage = null;
            try
            {
                profileDAO.open();
                User myProfile = null;
                if (!refresh)
                    myProfile = profileDAO.getMe(OperatingSite.getSite().apiSiteParameter);

                if (profileOlderThanThirtyMinutes(myProfile))
                {
                    userPage = userService.getMe();
                    if (userPage != null && userPage.items != null && !userPage.items.isEmpty())
                    {
                        profileDAO.deleteMe(OperatingSite.getSite().apiSiteParameter);
                        profileDAO.insert(OperatingSite.getSite().apiSiteParameter, userPage.items.get(0), true);
                        SharedPreferencesUtil.setLong(getApplicationContext(), StringConstants.USER_ID,
                                        userPage.items.get(0).id);
                    }
                }
                else
                {
                    userPage = new StackXPage<User>();
                    userPage.items = new ArrayList<User>();
                    userPage.items.add(myProfile);
                }
            }
            catch (SQLException e)
            {
                Log.d(TAG, e.getMessage());
            }
            finally
            {
                profileDAO.close();
            }

            return userPage;
        }
        else
            return userService.getUserById(userId);
    }

    private boolean profileOlderThanThirtyMinutes(User myProfile)
    {
        return myProfile == null
                        || System.currentTimeMillis() - myProfile.lastUpdateTime > IntegerConstants.MS_IN_HALF_AN_HOUR;
    }

    private HashMap<String, Account> getUserAccounts(boolean me, long userId)
    {
        if (me)
            return getMyAccounts();

        return userService.getAccounts(userId, 1);
    }

    private HashMap<String, Account> getMyAccounts()
    {
        Log.d(TAG, "getMyAccounts");

        UserAccountsDAO userAccountsDao = new UserAccountsDAO(getApplicationContext());
        try
        {
            long currentAccountId = SharedPreferencesUtil.getLong(getApplicationContext(), StringConstants.ACCOUNT_ID,
                            -1);
            if (currentAccountId != -1)
            {
                userAccountsDao.open();
                long lastUpdateTime = userAccountsDao.getLastUpdateTime();

                if (System.currentTimeMillis() - lastUpdateTime <= IntegerConstants.MS_IN_A_DAY)
                {
                    ArrayList<Account> accounts = userAccountsDao.getAccounts(currentAccountId);
                    HashMap<String, Account> accountsMap = new HashMap<String, Account>();
                    if (accounts != null)
                    {
                        for (Account account : accounts)
                            accountsMap.put(account.siteUrl, account);
                    }

                    return accountsMap;
                }
            }
        }
        catch (SQLException e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            userAccountsDao.close();
        }

        return userService.getAccounts(1);
    }

    private StackXPage<Question> getQuestions(boolean me, long userId, int page)
    {
        return me ? userService.getMyQuestions(page) : userService.getQuestionsByUser(userId, page);
    }

    private StackXPage<Answer> getAnswers(boolean me, long userId, int page)
    {
        return me ? userService.getMyAnswers(page) : userService.getAnswersByUser(userId, page);
    }

    private StackXPage<Question> getFavorites(boolean me, long userId, int page)
    {
        return me ? userService.getMyFavorites(page) : userService.getFavoritesByUser(userId, page);
    }

    private void getUnreadInboxItems(Intent intent)
    {
        int totalNewMsgs = 0;
        int page = intent.getIntExtra(StringConstants.PAGE, 1);
        StackXPage<InboxItem> pageObj = userService.getUnreadItemsInInbox(page);

        if (pageObj != null && pageObj != null && !pageObj.items.isEmpty())
        {
            Log.d(TAG, "New unread inbox items found. Notifying reeiver");
            totalNewMsgs += pageObj.items.size();
            broadcastUnreadItemsCount(totalNewMsgs, pageObj);
        }
    }

    private void broadcastUnreadItemsCount(int totalNewMsgs, StackXPage<InboxItem> unreadInboxItems)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserIntentAction.NEW_MSG.getAction());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(UserIntentAction.NEW_MSG.getAction(), unreadInboxItems);
        sendBroadcast(broadcastIntent);
    }

    private ArrayList<Site> getUserSites(ResultReceiver receiver, boolean forAuthenicatedUser)
    {
        ArrayList<Site> sites = getSites();

        if (sites != null)
        {
            Log.d(TAG, "Returning site list from DB");
            return sites;
        }

        LinkedHashMap<String, Site> linkSitesMap = userService.getAllSitesInNetwork();

        if (!forAuthenicatedUser)
            return persistAndGetList(linkSitesMap);

        LinkedHashMap<String, Site> regSitesFirstMap = new LinkedHashMap<String, Site>();
        HashMap<String, Account> linkAccountsMap = userService.getAccounts(1);

        if (linkAccountsMap != null && linkSitesMap != null)
        {
            long currentAccountId = SharedPreferencesUtil.getLong(getApplicationContext(), StringConstants.ACCOUNT_ID,
                            -1);
            if (currentAccountId == -1 && !linkAccountsMap.isEmpty())
            {
                currentAccountId = linkAccountsMap.values().iterator().next().id;

                Log.d(TAG, "Setting account id in shared preferences: " + currentAccountId);
                SharedPreferencesUtil.setLong(getApplicationContext(), StringConstants.ACCOUNT_ID, currentAccountId);
                SharedPreferencesUtil.setLong(getApplicationContext(), StringConstants.ACCOUNTS_LAST_UPDATED,
                                System.currentTimeMillis());
                DbRequestThreadExecutor.persistAccounts(getApplicationContext(), new ArrayList<Account>(linkAccountsMap.values()));
            }

            for (String siteUrl : linkAccountsMap.keySet())
            {
                if (linkSitesMap.containsKey(siteUrl))
                {
                    Log.d("Usertype for " + siteUrl, linkAccountsMap.get(siteUrl).userType.name());

                    Site site = linkSitesMap.get(siteUrl);
                    site.userType = linkAccountsMap.get(siteUrl).userType;
                    site.writePermissions = userService.getWritePermissions(site.apiSiteParameter);
                    DbRequestThreadExecutor.persistPermissions(getApplicationContext(), site, site.writePermissions);
                    regSitesFirstMap.put(siteUrl, site);
                    linkSitesMap.remove(siteUrl);
                }
            }

            regSitesFirstMap.putAll(linkSitesMap);
        }

        return persistAndGetList(regSitesFirstMap);
    }

    private ArrayList<Site> getSites()
    {
        SiteDAO siteDAO = new SiteDAO(getApplicationContext());

        try
        {
            siteDAO.open();
            return siteDAO.getSites();
        }
        catch (SQLException e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            siteDAO.close();
        }

        return null;
    }

    private ArrayList<Site> persistAndGetList(LinkedHashMap<String, Site> linkSitesMap)
    {
        ArrayList<Site> sites = new ArrayList<Site>(linkSitesMap.values());
        DbRequestThreadExecutor.persistSites(getApplicationContext(), sites);
        SharedPreferencesUtil.setBoolean(getApplicationContext(), StringConstants.SITES_INIT, true);
        return sites;
    }

    private void deauthenticateApp(String accessToken)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserIntentAction.LOGOUT.getAction());
        broadcastIntent.putExtra(UserIntentAction.LOGOUT.getAction(), userService.logout(accessToken));
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }
}

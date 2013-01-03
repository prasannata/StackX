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
import android.util.Log;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserIntentService extends AbstractIntentService
{
    private static final String TAG = UserIntentService.class.getSimpleName();
    public static final int GET_USER_PROFILE = 1;
    public static final int GET_USER_QUESTIONS = 2;
    public static final int GET_USER_ANSWERS = 3;
    public static final int GET_USER_INBOX = 4;
    public static final int GET_USER_UNREAD_INBOX = 5;
    public static final int GET_USER_SITES = 6;
    public static final int DEAUTH_APP = 7;

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
	int action = intent.getIntExtra(StringConstants.ACTION, -1);
	int page = intent.getIntExtra(StringConstants.PAGE, 1);
	boolean me = intent.getBooleanExtra(StringConstants.ME, false);
	long userId = intent.getLongExtra(StringConstants.USER_ID, -1);

	try
	{
	    switch (action)
	    {
		case GET_USER_PROFILE:
		    Log.d(TAG, "getUserDetail");
		    getUserDetail(me, userId, page);
		    break;
		case GET_USER_QUESTIONS:
		    Log.d(TAG, "getQuestions");
		    getQuestions(me, userId, page);
		    break;
		case GET_USER_ANSWERS:
		    Log.d(TAG, "getAnswers");
		    getAnswers(me, userId, page);
		    break;
		case GET_USER_INBOX:
		    broadcastSerializableExtra(UserIntentAction.INBOX.getAction(), UserIntentAction.INBOX.getAction(),
			            userService.getInbox(page));
		    break;
		case GET_USER_UNREAD_INBOX:
		    getUnreadInboxItems(intent);
		    break;
		case GET_USER_SITES:
		    getUserSites(intent.getBooleanExtra(StringConstants.AUTHENTICATED, false));
		    break;
		case DEAUTH_APP:
		    deauthenticateApp(intent.getStringExtra(StringConstants.ACCESS_TOKEN));
		    break;
		default:
		    Log.e(TAG, "Unknown action: " + action);
		    break;
	    }
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}
    }

    private void getUserDetail(boolean me, long userId, int page)
    {
	try
	{

	    if (me)
	    {
		broadcastSerializableExtra(UserIntentAction.USER_DETAIL.getAction(),
		                UserIntentAction.USER_DETAIL.getAction(), userService.getMe());
	    }
	    else
	    {
		broadcastSerializableExtra(UserIntentAction.USER_DETAIL.getAction(),
		                UserIntentAction.USER_DETAIL.getAction(), userService.getUserById(userId));
	    }
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}

	fetchUserAccountsAndBroadcast(me, userId);
    }

    private void fetchUserAccountsAndBroadcast(boolean me, long userId)
    {
	if (me)
	{
	    broadcastSerializableExtra(UserIntentAction.USER_ACCOUNTS.getAction(),
		            UserIntentAction.USER_ACCOUNTS.getAction(), userService.getAccounts(1));
	}
	else
	{
	    broadcastSerializableExtra(UserIntentAction.USER_ACCOUNTS.getAction(),
		            UserIntentAction.USER_ACCOUNTS.getAction(), userService.getAccounts(userId, 1));
	}
    }

    private void getQuestions(boolean me, long userId, int page)
    {
	if (me)
	{
	    broadcastSerializableExtra(UserIntentAction.QUESTIONS_BY_USER.getAction(),
		            QuestionIntentAction.QUESTIONS.getAction(), userService.getMyQuestions(page));
	}
	else
	{
	    if (userId > 0)
	    {
		broadcastSerializableExtra(UserIntentAction.QUESTIONS_BY_USER.getAction(),
		                QuestionIntentAction.QUESTIONS.getAction(),
		                userService.getQuestionsByUser(userId, page));
	    }
	}
    }

    private void getAnswers(boolean me, long userId, int page)
    {
	if (me)
	{
	    broadcastSerializableExtra(UserIntentAction.ANSWERS_BY_USER.getAction(),
		            UserIntentAction.ANSWERS_BY_USER.getAction(), userService.getMyAnswers(page));
	}
	else
	{
	    if (userId > 0)
	    {
		broadcastSerializableExtra(UserIntentAction.ANSWERS_BY_USER.getAction(),
		                UserIntentAction.ANSWERS_BY_USER.getAction(),
		                userService.getAnswersByUser(userId, page));
	    }
	}
    }

    private void getUnreadInboxItems(Intent intent)
    {
	int totalNewMsgs = 0;
	int page = intent.getIntExtra(StringConstants.PAGE, 1);
	ArrayList<InboxItem> unreadInboxItems = userService.getUnreadItemsInInbox(page);

	if (unreadInboxItems != null && !unreadInboxItems.isEmpty())
	{
	    totalNewMsgs += unreadInboxItems.size();
	    broadcastUnreadItemsCount(totalNewMsgs, unreadInboxItems);
	}
    }

    private void broadcastUnreadItemsCount(int totalNewMsgs, ArrayList<InboxItem> unreadInboxItems)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(UserIntentAction.NEW_MSG.getAction());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(UserIntentAction.NEW_MSG.getAction(), unreadInboxItems);
	sendBroadcast(broadcastIntent);
    }

    private void getUserSites(boolean forAuthenicatedUser)
    {
	LinkedHashMap<String, Site> linkSitesMap = userService.getAllSitesInNetwork();

	if (forAuthenicatedUser)
	{
	    LinkedHashMap<String, Site> regSitesFirstMap = new LinkedHashMap<String, Site>();
	    HashMap<String, Account> linkAccountsMap = userService.getAccounts(1);

	    if (linkAccountsMap != null && linkSitesMap != null)
	    {
		for (String siteUrl : linkAccountsMap.keySet())
		{
		    if (linkSitesMap.containsKey(siteUrl))
		    {
			Log.d("Usertype for " + siteUrl, linkAccountsMap.get(siteUrl).userType.name());

			Site site = linkSitesMap.get(siteUrl);
			site.userType = linkAccountsMap.get(siteUrl).userType;
			regSitesFirstMap.put(siteUrl, site);

			linkSitesMap.remove(siteUrl);
		    }
		}

		regSitesFirstMap.putAll(linkSitesMap);
	    }

	    broadcastSerializableExtra(StringConstants.SITES, StringConstants.SITES, new ArrayList<Site>(
		            regSitesFirstMap.values()));

	}
	else
	{
	    broadcastSerializableExtra(StringConstants.SITES, StringConstants.SITES,
		            new ArrayList<Site>(linkSitesMap.values()));
	}
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

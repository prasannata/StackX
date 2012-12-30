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
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.http.SecureHttpHelper;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserServiceHelper extends AbstractBaseServiceHelper
{
    private final String TAG = UserServiceHelper.class.getSimpleName();

    private static final UserServiceHelper userService = new UserServiceHelper();

    public static UserServiceHelper getInstance()
    {
	return userService;
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    public LinkedHashMap<String, Site> getAllSitesInNetwork()
    {
	String restEndPoint = StringConstants.SITES;
	LinkedHashMap<String, Site> sites = new LinkedHashMap<String, Site>();
	JSONObjectWrapper jsonObject = SecureHttpHelper.getInstance().executeForGzipResponse(StackUri.STACKX_API_HOST,
	                restEndPoint, AppUtils.getDefaultQueryParams());
	try
	{
	    if (jsonObject != null)
	    {
		JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);

		if (jsonArray != null)
		{
		    for (int i = 0; i < jsonArray.length(); i++)
		    {
			JSONObject siteJsonObject = jsonArray.getJSONObject(i);
			Site site = getSerializedSiteObject(new JSONObjectWrapper(siteJsonObject));
			if (site != null)
			{
			    sites.put(site.link, site);
			}
		    }
		}
	    }

	}
	catch (JSONException e)
	{
	    Log.d(getLogTag(), e.getMessage());
	}
	return sites;
    }

    private Site getSerializedSiteObject(JSONObjectWrapper siteJsonObject)
    {
	Site site = new Site();
	site.apiSiteParameter = siteJsonObject.getString(JsonFields.Site.API_SITE_PARAMETER);
	site.logoUrl = siteJsonObject.getString(JsonFields.Site.LOGO_URL);
	site.name = siteJsonObject.getString(JsonFields.Site.NAME);
	site.link = siteJsonObject.getString(JsonFields.Site.SITE_URL);
	site.faviconUrl = siteJsonObject.getString(JsonFields.Site.FAVICON_URL);
	site.iconUrl = siteJsonObject.getString(JsonFields.Site.ICON_URL);
	return site;
    }

    private ArrayList<Question> getQuestions(String restEndPoint, Map<String, String> queryParams)
    {
	JSONObjectWrapper questionsJsonResponse = executeHttpRequest(restEndPoint, queryParams);

	if (questionsJsonResponse != null)
	{
	    return getQuestionModel(questionsJsonResponse);
	}

	return null;
    }

    public ArrayList<Question> getMyQuestions(int page)
    {
	String restEndPoint = "/me/questions";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	return getQuestions(restEndPoint, queryParams);
    }

    public ArrayList<Question> getQuestionsByUser(long userId, int page)
    {
	String restEndPoint = "/users/" + userId + "/questions";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	return getQuestions(restEndPoint, queryParams);
    }

    public User getMe()
    {
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);

	JSONObjectWrapper jsonObject = SecureHttpHelper.getInstance().executeForGzipResponse(StackUri.STACKX_API_HOST,
	                "/me", queryParams);
	JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);
	JSONObject userJsonObject = getIndexFromArray(jsonArray, 0, JSONObject.class);

	return getSerializedUserObject(new JSONObjectWrapper(userJsonObject));
    }

    public User getUserById(long userId)
    {
	User user = null;
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);

	if (userId != -1)
	{
	    JSONObjectWrapper jsonObject = SecureHttpHelper.getInstance().executeForGzipResponse(
		            StackUri.STACKX_API_HOST, "/users/" + userId, queryParams);
	    if (jsonObject != null)
	    {
		JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);
		if (jsonArray != null)
		{
		    try
		    {
			JSONObject userJsonObject = jsonArray.getJSONObject(0);
			user = getSerializedUserObject(JSONObjectWrapper.wrap(userJsonObject));
		    }
		    catch (JSONException e)
		    {
			Log.e(TAG, e.getMessage());
		    }
		}
	    }
	}

	return user;
    }

    private ArrayList<Answer> getAnswers(String restEndPoint, Map<String, String> queryParams)
    {
	ArrayList<Answer> answers = new ArrayList<Answer>();

	JSONObjectWrapper answersJsonObject = executeHttpRequest(restEndPoint, queryParams);

	if (answersJsonObject != null)
	{
	    JSONArray jsonArray = answersJsonObject.getJSONArray(JsonFields.ITEMS);
	    if (jsonArray != null)
	    {
		try
		{
		    for (int i = 0; i < jsonArray.length(); i++)
		    {
			JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
			answers.add(getSerializedAnswerObject(jsonObject));
		    }
		}
		catch (JSONException e)
		{
		    Log.d(getLogTag(), e.getMessage());
		}
	    }
	}

	return answers;
    }

    public ArrayList<Answer> getMyAnswers(int page)
    {
	String restEndPoint = "/me/answers";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.QUESTION_DETAIL_FILTER);

	return getAnswers(restEndPoint, queryParams);
    }

    public ArrayList<Answer> getAnswersByUser(long userId, int page)
    {
	String restEndPoint = "/users/" + userId + "/answers";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.QUESTION_DETAIL_FILTER);
	return getAnswers(restEndPoint, queryParams);
    }

    public ArrayList<InboxItem> getInbox(int page)
    {
	String restEndPoint = "/me/inbox";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_INBOX_FILTER);

	return getInboxItems(restEndPoint, queryParams);
    }

    public ArrayList<InboxItem> getUnreadItemsInInbox(int page, Site site)
    {
	String restEndPoint = "/me/inbox/unread";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.SITE, site.apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_INBOX_FILTER);

	return getInboxItems(restEndPoint, queryParams);
    }

    private ArrayList<InboxItem> getInboxItems(String restEndPoint, Map<String, String> queryParams)
    {
	ArrayList<InboxItem> inboxItems = null;

	JSONObjectWrapper jsonOfInboxItems = executeHttpRequest(restEndPoint, queryParams);
	if (jsonOfInboxItems != null)
	{
	    JSONArray itemsArray = jsonOfInboxItems.getJSONArray(JsonFields.ITEMS);

	    if (itemsArray != null)
	    {
		inboxItems = new ArrayList<InboxItem>();

		for (int i = 0; i < itemsArray.length(); i++)
		{
		    try
		    {
			JSONObjectWrapper itemJsonObject = JSONObjectWrapper.wrap(itemsArray.getJSONObject(i));
			if (itemJsonObject != null)
			{
			    InboxItem inboxItem = new InboxItem();
			    inboxItem.questionId = itemJsonObject.getLong(JsonFields.InboxItem.QUESTION_ID);
			    inboxItem.answerId = itemJsonObject.getLong(JsonFields.InboxItem.ANSWER_ID);
			    inboxItem.commentId = itemJsonObject.getLong(JsonFields.InboxItem.COMMENT_ID);
			    inboxItem.itemType = ItemType.getValue(itemJsonObject
				            .getString(JsonFields.InboxItem.ITEM_TYPE));
			    inboxItem.title = itemJsonObject.getString(JsonFields.InboxItem.TITLE);
			    inboxItem.creationDate = itemJsonObject.getLong(JsonFields.InboxItem.CREATION_DATE);
			    inboxItem.body = itemJsonObject.getString(JsonFields.InboxItem.BODY);
			    inboxItem.unread = itemJsonObject.getBoolean(JsonFields.InboxItem.IS_UNREAD);
			    inboxItems.add(inboxItem);
			}
		    }
		    catch (JSONException e)
		    {
			Log.d(getLogTag(), e.getMessage());
		    }
		}
	    }
	}
	return inboxItems;
    }

    public HashMap<String, Account> getAccounts(int page)
    {
	String restEndPoint = "/me/associated";
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.NETWORK_USER_TYPE_FILTER);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	return getAccounts(restEndPoint, queryParams);
    }

    public HashMap<String, Account> getAccounts(long userId, int page)
    {
	String restEndPoint = "/users/" + userId + "/associated";
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	return getAccounts(restEndPoint, queryParams);
    }

    private HashMap<String, Account> getAccounts(String restEndPoint, Map<String, String> queryParams)
    {
	HashMap<String, Account> accounts = null;
	JSONObjectWrapper accountsJsonObject = executeHttpRequest(restEndPoint, queryParams);
	if (accountsJsonObject != null)
	{
	    JSONArray jsonArray = accountsJsonObject.getJSONArray(JsonFields.ITEMS);
	    if (jsonArray != null)
	    {
		accounts = new HashMap<String, Account>();

		for (int i = 0; i < jsonArray.length(); i++)
		{
		    try
		    {
			JSONObjectWrapper accountJsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
			Account account = new Account();
			account.id = accountJsonObject.getLong(JsonFields.Account.ACCOUNT_ID);
			account.userId = accountJsonObject.getLong(JsonFields.Account.USER_ID);
			account.siteName = accountJsonObject.getString(JsonFields.Account.SITE_NAME);
			account.siteUrl = accountJsonObject.getString(JsonFields.Account.SITE_URL);
			account.userType = UserType.toEnum(accountJsonObject.getString(JsonFields.Account.USER_TYPE));
			accounts.put(account.siteUrl, account);
		    }
		    catch (JSONException e)
		    {
			Log.d(getLogTag(), e.getMessage());
		    }
		}
	    }
	}
	return accounts;
    }

    public StackExchangeHttpError logout(String accessToken)
    {
	StackExchangeHttpError error = new StackExchangeHttpError();
	error.id = -1;

	String restEndPoint = "/apps/" + accessToken + "/de-authenticate";
	JSONObjectWrapper jsonObject = SecureHttpHelper.getInstance().executeForGzipResponse(StackUri.STACKX_API_HOST,
	                restEndPoint, null);
	boolean success = jsonObject != null && jsonObject.getJSONArray(JsonFields.ITEMS) != null
	                && jsonObject.getJSONArray(JsonFields.ITEMS).length() == 0;
	if (success == false)
	{
	    error.id = jsonObject.getInt(JsonFields.Error.ERROR_ID);
	    error.name = jsonObject.getString(JsonFields.Error.ERROR_NAME);
	    error.message = jsonObject.getString(JsonFields.Error.ERROR_MESSAGE);
	}

	return error;
    }

    public ArrayList<String> getTags(int page, boolean registeredForSite)
    {
	ArrayList<String> tags = null;
	String restEndPoint = registeredForSite ? "/me/tags" : "/tags";

	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	JSONObjectWrapper jsonObjectWrapper = executeHttpRequest(restEndPoint, queryParams);

	if (jsonObjectWrapper != null)
	{
	    JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);
	    if (jsonArray != null && jsonArray.length() > 0)
	    {
		tags = new ArrayList<String>();
		for (int i = 0; i < jsonArray.length(); i++)
		{
		    try
		    {
			JSONObjectWrapper tagJson = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
			tags.add(tagJson.getString(JsonFields.Tag.NAME));
		    }
		    catch (JSONException e)
		    {
			Log.d(getLogTag(), e.getMessage());
		    }
		}
	    }
	}

	return tags;
    }
}
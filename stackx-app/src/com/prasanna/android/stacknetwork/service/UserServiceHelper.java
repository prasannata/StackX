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
import java.util.LinkedHashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.Tag;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.Order;
import com.prasanna.android.stacknetwork.utils.StackUri.Sort;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class UserServiceHelper extends AbstractBaseServiceHelper {
  private final String TAG = UserServiceHelper.class.getSimpleName();

  private static final UserServiceHelper userService = new UserServiceHelper();

  public static UserServiceHelper getInstance() {
    return userService;
  }

  @Override
  protected String getLogTag() {
    return TAG;
  }

  public LinkedHashMap<String, Site> getAllSitesInNetwork() {
    String restEndPoint = StringConstants.SITES;
    LinkedHashMap<String, Site> sites = new LinkedHashMap<String, Site>();
    boolean hasMore = true;
    int page = 1;
    final int PAGE_SIZE = 100;
    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(PAGE_SIZE));

    while (hasMore) {
      queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page++));
      JSONObjectWrapper jsonObject = executeHttpGetRequest(restEndPoint, queryParams);

      try {
        hasMore = addSites(sites, jsonObject);
      }
      catch (JSONException e) {
        LogWrapper.d(getLogTag(), e.getMessage());
        hasMore = false;
      }
    }
    return sites;
  }

  private boolean addSites(LinkedHashMap<String, Site> sites, JSONObjectWrapper jsonObject) throws JSONException {
    if (jsonObject != null) {
      JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);

      if (jsonArray != null) {
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject siteJsonObject = jsonArray.getJSONObject(i);
          Site site = getSerializedSiteObject(new JSONObjectWrapper(siteJsonObject));
          if (site != null)
            sites.put(site.link, site);
        }
      }

      return jsonObject.getBoolean(JsonFields.HAS_MORE);
    }

    return false;
  }

  private Site getSerializedSiteObject(JSONObjectWrapper siteJsonObject) {
    if (siteJsonObject == null)
      return null;

    Site site = new Site();
    site.apiSiteParameter = siteJsonObject.getString(JsonFields.Site.API_SITE_PARAMETER);
    site.logoUrl = siteJsonObject.getString(JsonFields.Site.LOGO_URL);
    site.name = siteJsonObject.getString(JsonFields.Site.NAME);
    site.audience = siteJsonObject.getString(JsonFields.Site.AUDIENCE);
    site.link = siteJsonObject.getString(JsonFields.Site.SITE_URL);
    site.faviconUrl = siteJsonObject.getString(JsonFields.Site.FAVICON_URL);
    site.iconUrl = siteJsonObject.getString(JsonFields.Site.ICON_URL);
    return site;
  }

  private StackXPage<Question> getQuestions(String restEndPoint, Map<String, String> queryParams) {
    JSONObjectWrapper questionsJsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

    if (questionsJsonResponse != null) {
      return getQuestionModel(questionsJsonResponse);
    }

    return null;
  }

  public StackXPage<Question> getMyQuestions(int page) {
    return getQuestions("/me/questions", getQueryParams(page));
  }

  public StackXPage<Question> getQuestionsByUser(long userId, int page) {
    return getQuestions("/users/" + userId + "/questions", getQueryParams(page));
  }

  public StackXPage<Question> getMyFavorites(int page) {
    return getQuestions("/me/favorites", getQueryParams(page));
  }

  public StackXPage<Question> getFavoritesByUser(long userId, int page) {
    return getQuestions("/users/" + userId + "/favorites", getQueryParams(page));
  }

  private Map<String, String> getQueryParams(int page) {
    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
    queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
    queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
    queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
    return queryParams;
  }

  public StackXPage<User> getMe(String site) {
    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.SITE, site);
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);
    return getSerializedUserObject(executeHttpGetRequest("/me", queryParams));
  }

  public StackXPage<User> getUserById(long userId, String site) {
    StackXPage<User> page = null;
    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.SITE, site);
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);

    if (userId != -1)
      page = getSerializedUserObject(executeHttpGetRequest("/users/" + userId, queryParams));

    return page;
  }

  private StackXPage<Answer> getAnswers(String restEndPoint, Map<String, String> queryParams) {
    StackXPage<Answer> page = new StackXPage<Answer>();

    JSONObjectWrapper answersJsonObject = executeHttpGetRequest(restEndPoint, queryParams);

    if (answersJsonObject != null) {
      getPageInfo(answersJsonObject, page);
      page.items = new ArrayList<Answer>();
      JSONArray jsonArray = answersJsonObject.getJSONArray(JsonFields.ITEMS);
      if (jsonArray != null) {

        try {
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
            page.items.add(getSerializedAnswerObject(jsonObject));
          }
        }
        catch (JSONException e) {
          LogWrapper.d(getLogTag(), e.getMessage());
        }
      }
    }

    return page;
  }

  public StackXPage<Answer> getMyAnswers(int page) {
    String restEndPoint = "/me/answers";

    Map<String, String> queryParams = getQueryParams(page);
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);

    return getAnswers(restEndPoint, queryParams);
  }

  public StackXPage<Answer> getAnswersByUser(long userId, int page) {
    String restEndPoint = "/users/" + userId + "/answers";

    Map<String, String> queryParams = getQueryParams(page);
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);
    return getAnswers(restEndPoint, queryParams);
  }

  public ArrayList<WritePermission> getWritePermissions(String site) {
    ArrayList<WritePermission> permissions = new ArrayList<WritePermission>();

    String restEndPoint = "me/write-permissions";

    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.SITE, site);
    JSONObjectWrapper jsonObjectWrapper = executeHttpGetRequest(restEndPoint, queryParams);

    if (jsonObjectWrapper != null) {
      JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);
      if (jsonArray != null && jsonArray.length() > 0) {
        try {
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObjectWrapper permissionJsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));

            WritePermission permission = new WritePermission();
            permission.canAdd = permissionJsonObject.getBoolean(JsonFields.Permission.CAN_ADD);
            permission.canDelete = permissionJsonObject.getBoolean(JsonFields.Permission.CAN_DELETE);
            permission.canEdit = permissionJsonObject.getBoolean(JsonFields.Permission.CAN_EDIT);
            permission.maxDailyActions = permissionJsonObject.getInt(JsonFields.Permission.MAX_DAILY_ACTIONS);
            permission.minSecondsBetweenActions =
                permissionJsonObject.getInt(JsonFields.Permission.MIN_SECONDS_BETWEEN_ACTIONS);
            permission.objectType =
                ObjectType.getEnum(permissionJsonObject.getString(JsonFields.Permission.OBJECT_TYPE));
            permission.userId = permissionJsonObject.getLong(JsonFields.Permission.USER_ID);
            permissions.add(permission);
          }
        }
        catch (JSONException e) {
          LogWrapper.d(getLogTag(), e.getMessage());
        }
      }
    }

    return permissions;
  }

  public StackXPage<InboxItem> getInbox(int page) {
    String restEndPoint = "inbox";

    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
    queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
    queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_INBOX_FILTER);

    return getInboxItems(restEndPoint, queryParams);
  }

  public StackXPage<InboxItem> getUnreadItemsInInbox(int page) {
    String restEndPoint = "/inbox/unread";

    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_INBOX_FILTER);

    return getInboxItems(restEndPoint, queryParams);
  }

  private StackXPage<InboxItem> getInboxItems(String restEndPoint, Map<String, String> queryParams) {
    StackXPage<InboxItem> page = null;

    JSONObjectWrapper jsonOfInboxItems = executeHttpGetRequest(restEndPoint, queryParams);
    if (jsonOfInboxItems != null) {
      JSONArray itemsArray = jsonOfInboxItems.getJSONArray(JsonFields.ITEMS);

      if (itemsArray != null) {
        page = new StackXPage<InboxItem>();
        getPageInfo(jsonOfInboxItems, page);

        page.items = new ArrayList<InboxItem>();

        for (int i = 0; i < itemsArray.length(); i++) {
          try {
            JSONObjectWrapper itemJsonObject = JSONObjectWrapper.wrap(itemsArray.getJSONObject(i));
            if (itemJsonObject != null)
              page.items.add(getSerializedInboxItem(itemJsonObject));
          }
          catch (JSONException e) {
            LogWrapper.d(getLogTag(), e.getMessage());
          }

        }
      }
    }
    return page;
  }

  private InboxItem getSerializedInboxItem(JSONObjectWrapper itemJsonObject) {
    InboxItem inboxItem = new InboxItem();
    inboxItem.questionId = itemJsonObject.getLong(JsonFields.InboxItem.QUESTION_ID);
    inboxItem.answerId = itemJsonObject.getLong(JsonFields.InboxItem.ANSWER_ID);
    inboxItem.commentId = itemJsonObject.getLong(JsonFields.InboxItem.COMMENT_ID);
    inboxItem.itemType = ItemType.getValue(itemJsonObject.getString(JsonFields.InboxItem.ITEM_TYPE));
    inboxItem.title = itemJsonObject.getString(JsonFields.InboxItem.TITLE);
    inboxItem.creationDate = itemJsonObject.getLong(JsonFields.InboxItem.CREATION_DATE);
    inboxItem.body = itemJsonObject.getString(JsonFields.InboxItem.BODY);
    inboxItem.unread = itemJsonObject.getBoolean(JsonFields.InboxItem.IS_UNREAD);
    inboxItem.site = getSerializedSiteObject(itemJsonObject.getJSONObject(JsonFields.InboxItem.SITE));
    return inboxItem;
  }

  public HashMap<String, Account> getMyAccount() {
    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.NETWORK_USER_TYPE_FILTER);
    return getAccounts("/me/associated", 1, queryParams);
  }

  public HashMap<String, Account> getAccount(long accountId) {
    return getAccounts("/users/" + accountId + "/associated", 1, AppUtils.getDefaultQueryParams());
  }

  private HashMap<String, Account> getAccounts(String restEndPoint, int page, Map<String, String> queryParams) {
    final int PAGE_SIZE = 100;
    boolean hasMore = true;
    HashMap<String, Account> accounts = new HashMap<String, Account>();

    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(PAGE_SIZE));

    while (hasMore) {
      queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page++));
      hasMore = addAccountsToMap(accounts, executeHttpGetRequest(restEndPoint, queryParams));
    }
    return accounts;
  }

  private boolean addAccountsToMap(HashMap<String, Account> accounts, JSONObjectWrapper accountsJsonObject) {
    if (accountsJsonObject != null) {
      JSONArray jsonArray = accountsJsonObject.getJSONArray(JsonFields.ITEMS);
      if (jsonArray != null) {
        for (int i = 0; i < jsonArray.length(); i++) {
          try {
            JSONObjectWrapper accountJsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
            Account account = new Account();
            account.id = accountJsonObject.getLong(JsonFields.Account.ACCOUNT_ID);
            account.userId = accountJsonObject.getLong(JsonFields.Account.USER_ID);
            account.siteName = accountJsonObject.getString(JsonFields.Account.SITE_NAME);
            account.siteUrl = accountJsonObject.getString(JsonFields.Account.SITE_URL);
            account.userType = UserType.toEnum(accountJsonObject.getString(JsonFields.Account.USER_TYPE));
            accounts.put(account.siteUrl, account);
          }
          catch (JSONException e) {
            LogWrapper.d(getLogTag(), e.getMessage());
          }
        }
      }

      return accountsJsonObject.getBoolean(JsonFields.HAS_MORE);
    }

    return false;
  }

  public StackExchangeHttpError logout(String accessToken) {
    StackExchangeHttpError error = new StackExchangeHttpError();
    error.id = -1;

    String restEndPoint = "/apps/" + accessToken + "/de-authenticate";
    JSONObjectWrapper jsonObject = executeHttpGetRequest(restEndPoint, null);
    if (jsonObject != null) {
      String deauthenticatedAccessToken = jsonObject.getString(JsonFields.AccessToken.ACCESS_TOKEN);
      if (deauthenticatedAccessToken == null || !accessToken.equals(deauthenticatedAccessToken)) {
        error.id = jsonObject.getInt(JsonFields.Error.ERROR_ID);
        error.name = jsonObject.getString(JsonFields.Error.ERROR_NAME);
        error.message = jsonObject.getString(JsonFields.Error.ERROR_MESSAGE);
      }
    }

    return error;
  }

  public LinkedHashSet<Tag> getTags(String site, int pageSize, boolean meTags) {
    int page = 1;
    LinkedHashSet<Tag> tags = null;
    String restEndPoint = meTags ? "/me/tags" : "/tags";
    boolean hasMore = true;

    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
    queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(pageSize));
    queryParams.put(StackUri.QueryParams.SORT, meTags ? Sort.NAME : Sort.POPULAR);
    queryParams.put(StackUri.QueryParams.ORDER, meTags ? Order.ASC : Order.DESC);

    while (hasMore) {
      queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page++));
      JSONObjectWrapper jsonObjectWrapper = executeHttpGetRequest(restEndPoint, queryParams);

      if (jsonObjectWrapper != null) {
        JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);
        if (jsonArray != null && jsonArray.length() > 0) {
          if (tags == null)
            tags = new LinkedHashSet<Tag>();

          for (int i = 0; i < jsonArray.length(); i++) {
            try {
              JSONObjectWrapper tagJson = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
              tags.add(new Tag(tagJson.getString(JsonFields.Tag.NAME)));
            }
            catch (JSONException e) {
              LogWrapper.d(getLogTag(), e.getMessage());
            }
          }
        }

        /* Get all the tags only for a registered user */
        hasMore = meTags ? jsonObjectWrapper.getBoolean(JsonFields.HAS_MORE) : false;

        /*
         * Dont bombard the server if the user has like 10-15 pages of tags,
         * delay each request by 100ms
         */
        sleep(100);
      }
    }

    return tags;
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    }
    catch (InterruptedException e) {
      LogWrapper.e(TAG, e.getMessage());
    }
  }
}

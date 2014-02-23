/*
    Copyright (C) 2014 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.utils;

public class StackUri {
  public static final String OAUTH_DIALOG_URL = "https://stackexchange.com/oauth/dialog";
  public static final String STACKX_API_HOST = "https://api.stackexchange.com/2.2";

  public static class QueryParams {
    public static final String SITE = "site";
    public static final String FILTER = "filter";
    public static final String SORT = "sort";
    public static final String PAGE = "page";
    public static final String ORDER = "order";
    public static final String PAGE_SIZE = "pageSize";
    public static final String IN_TITLE = "intitle";
    public static final String CLIENT_ID = "client_id";
    public static final String KEY = "key";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String SCOPE = "scope";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String SINCE = "since";
    public static final String TAGGED = "tagged";
    public static final String TITLE = "title";
    public static final String BODY = "body";
  }

  public static class QueryParamDefaultValues {
    public static final int PAGE_SIZE = 15;
    public static final int ANSWERS_PAGE_SIZE = 10;
    public static final String ORDER = "desc";
    public static final String DEFAULT_FILTER = "default";
    public static final String COLLECTION_FILTER = "!)r(UklJydkr9N6a-Q2gg";
    public static final String ITEM_DETAIL_FILTER = "!9aMz979rkLNvEtm-gX70M.YWXodU)2gsgRenveU4XIunO)*0KsgmpeTtDO*";
    public static final String USER_DETAIL_FILTER = "!-q2RdWJy";
    public static final String USER_INBOX_FILTER = "!-q2RaosI";
    public static final String COMMENT_FILTER = "!-.mgWN4RPTQu";
    public static final String NETWORK_USER_TYPE_FILTER = "!-q2RbCAp";
    public static final String CLIENT_ID = "202";
    public static final String KEY = "Nt8I5NbbbzXXPPVwE0ujRg((";
    public static final String SCOPE = "read_inbox,no_expiry,write_access,private_info";
    public static final String REDIRECT_URI = "https://stackexchange.com/oauth/login_success";
  }

  public static class Sort {
    public static final String ACTIVITY = "activity";
    public static final String POPULAR = "popular";
    public static final String NAME = "name";
    public static final String VOTES = "votes";
    public static final String CREATION = "creation";
    public static final String WEEK = "week";
    public static final String RELEVANCE = "relevance";
  }

  public static class Order {
    public static final String ASC = "asc";
    public static final String DESC = "desc";

  }
}

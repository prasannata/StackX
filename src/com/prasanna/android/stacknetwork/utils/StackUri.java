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

package com.prasanna.android.stacknetwork.utils;

public class StackUri
{
    public static final String OAUTH_DIALOG_URL = "https://stackexchange.com/oauth/dialog";
    public static final String STACKX_API_HOST = "https://api.stackexchange.com/2.1";

    public static class QueryParams
    {
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
    }

    public static class QueryParamDefaultValues
    {
	public static final int PAGE_SIZE = 15;
	public static final int ANSWERS_PAGE_SIZE = 3;
	public static final String ORDER = "desc";
	public static final String QUESTION_DETAIL_FILTER = "!3vWTEWZ2QQ3pULbNz";
	public static final String USER_DETAIL_FILTER = "!-q2RdWJy";
	public static final String USER_INBOX_FILTER = "!-q2RaosI";
	public static final String NETWORK_USER_TYPE_FILTER = "!-q2RbCAp";
	public static final String CLIENT_ID = "202";
	public static final String KEY = "Nt8I5NbbbzXXPPVwE0ujRg((";
	public static final String SCOPE = "read_inbox,no_expiry";
	public static final String REDIRECT_URI = "http://oauth.prasanna.stackx.com";
    }

    public static class Sort
    {
	public static final String ACTIVITY = "activity";
	public static final String POPULAR = "popular";
	public static final String NAME = "name";
	public static final String VOTES = "votes";
    }
}

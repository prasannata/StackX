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

public class JsonFields {
  public static final String ITEMS = "items";
  public static final String QUOTA_REMAINING = "quota_remaining";
  public static final String QUOTA_MAX = "quota_max";
  public static final String HAS_MORE = "has_more";

  public static class CommonFields {
    public static final String SCORE = "score";
    public static final String OWNER = "owner";
    public static final String BODY = "body";
    public static final String TITLE = "title";
    public static final String CREATION_DATE = "creation_date";
    public static final String LINK = "link";
  }

  public static class Post extends CommonFields {
    public static final String POST_ID = "post_id";
    public static final String POST_TYPE = "post_type";
  }

  public static class Question extends CommonFields {
    public static final String QUESTION_ID = "question_id";
    public static final String IS_ANSWERED = "is_answered";
    public static final String ANSWER_COUNT = "answer_count";
    public static final String VIEW_COUNT = "view_count";
    public static final String TAGS = "tags";
    public static final String ACCEPTED_ANSWER_ID = "accepted_answer_id";
  }

  public static class Answer extends CommonFields {
    public static final String ANSWER_ID = "answer_id";
    public static final String QUESTION_ID = "question_id";
    public static final String IS_ACCEPTED = "is_accepted";
  }

  public static class Comment extends CommonFields {
    public static final String POST_ID = "post_id";
    public static final String COMMENT_ID = "comment_id";
    public static final String POST_TYPE = "post_type";
  }

  public static class Site {
    public static final String NAME = "name";
    public static final String AUDIENCE = "audience";
    public static final String LOGO_URL = "logo_url";
    public static final String API_SITE_PARAMETER = "api_site_parameter";
    public static final String SITE_URL = "site_url";
    public static final String FAVICON_URL = "favicon_url";
    public static final String ICON_URL = "icon_url";
  }

  public static class User extends CommonFields {
    public static final String USER_ID = "user_id";
    public static final String ACCOUNT_ID = "account_id";
    public static final String DISPLAY_NAME = "display_name";
    public static final String REPUTATION = "reputation";
    public static final String PROFILE_IMAGE = "profile_image";
    public static final String ACCEPT_RATE = "accept_rate";
    public static final String QUESTION_COUNT = "question_count";
    public static final String ANSWER_COUNT = "answer_count";
    public static final String UP_VOTE_COUNT = "up_vote_count";
    public static final String DOWN_VOTE_COUNT = "down_vote_count";
    public static final String VIEW_COUNT = "view_count";
    public static final String LAST_ACCESS_DATE = "last_access_date";
    public static final String BADGE_COUNTS = "badge_counts";
    public static final String USER_TYPE = "user_type";
  }

  public static class Account {
    public static final String ACCOUNT_ID = "account_id";
    public static final String USER_ID = "user_id";
    public static final String SITE_NAME = "site_name";
    public static final String SITE_URL = "site_url";
    public static final String USER_TYPE = "user_type";
  }

  public static class BadgeCounts {
    public static final String GOLD = "gold";
    public static final String SILVER = "silver";
    public static final String BRONZE = "bronze";
  }

  public static class InboxItem extends CommonFields {
    public static final String QUESTION_ID = "question_id";
    public static final String ANSWER_ID = "answer_id";
    public static final String COMMENT_ID = "comment_id";
    public static final String ITEM_TYPE = "item_type";
    public static final String SITE = "site";
    public static final String IS_UNREAD = "is_unread";
  }

  public static class Error {
    public static final String ERROR_ID = "error_id";
    public static final String ERROR_NAME = "error_name";
    public static final String ERROR_MESSAGE = "error_message";
  }

  public static class Permission {
    public static final String CAN_ADD = "can_add";
    public static final String CAN_DELETE = "can_delete";
    public static final String CAN_EDIT = "can_edit";
    public static final String MAX_DAILY_ACTIONS = "max_daily_actions";
    public static final String MIN_SECONDS_BETWEEN_ACTIONS = "min_seconds_between_actions";
    public static final String OBJECT_TYPE = "object_type";
    public static final String USER_ID = "user_id";
  }

  public static class Tag {
    public static final String NAME = "name";
  }

  public static class AccessToken {
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRES_ON_DATE = "expires_on_date";
    public static final String ACCOUNT_ID = "account_id";
  }

}

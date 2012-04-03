package com.prasanna.android.stacknetwork.utils;

public class JsonFields
{
    public static final String ITEMS = "items";

    public static class BaseUserContribFields
    {
        public static final String SCORE = "score";
        public static final String OWNER = "owner";
        public static final String BODY = "body";
        public static final String TITLE = "title";
        public static final String CREATION_DATE = "creation_date";
    }

    public static class Question extends BaseUserContribFields
    {
        public static final String QUESTION_ID = "question_id";
        public static final String IS_ANSWERED = "is_answered";
        public static final String ANSWER_COUNT = "answer_count";
        public static final String VIEW_COUNT = "view_count";
        public static final String TAGS = "tags";
        public static final String ACCEPTED_ANSWER_ID = "accepted_answer_id";
    }

    public static class Answer extends BaseUserContribFields
    {
        public static final String ANSWER_ID = "answer_id";
        public static final String QUESTION_ID = "question_id";
        public static final String IS_ACCEPTED = "is_accepted";
    }

    public static class Comment extends BaseUserContribFields
    {
        public static final String COMMENT_ID = "comment_id";
    }

    public static class Site
    {
        public static final String NAME = "name";
        public static final String LOGO_URL = "logo_url";
        public static final String API_SITE_PARAMETER = "api_site_parameter";
        public static final String SITE_URL = "site_url";
    }

    public static class User
    {
        public static final String USER_ID = "user_id";
        public static final String ACCOUNT_ID = "user_id";
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
    }

    public static class BadgeCounts
    {
        public static final String GOLD = "gold";
        public static final String SILVER = "silver";
        public static final String BRONZE = "bronze";
    }

    public static class InboxItem extends BaseUserContribFields
    {
        public static final String QUESTION_ID = "question_id";
        public static final String ANSWER_ID = "answer_id";
        public static final String COMMENT_ID = "comment_id";
        public static final String ITEM_TYPE = "item_type";
        public static final String SITE = "site";
    }

    public static class Error
    {
        public static final String ERROR_ID = "error_id";
        public static final String ERROR_NAME = "error_name";
        public static final String ERROR_MESSAGE = "error_message";
    }

}

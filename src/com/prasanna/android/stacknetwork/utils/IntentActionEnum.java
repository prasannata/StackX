package com.prasanna.android.stacknetwork.utils;

public class IntentActionEnum
{
    public interface IntentAction
    {
    }

    public enum QuestionIntentAction implements IntentAction
    {
        QUESTIONS("questions"),
        QUESTION_BODY("questioBody"),
        QUESTION_FULL_DETAILS("questionFullDetails"),
        QUESTION_ANSWERS("questionAnswers"),
        QUESTION_COMMENTS("questionComments"),
        QUESTION_SEARCH("questionSearch"),
        TAGS_FAQ("tagsFaq");
        private final String extra;

        private QuestionIntentAction(String extra)
        {
            this.extra = extra;
        }

        public String getExtra()
        {
            return extra;
        }
    }

    public enum UserIntentAction implements IntentAction
    {
        INBOX("inbox"),
        ALL_USERS("users"),
        USER_DETAIL("userDetail"),
        USER_ACCOUNTS("userAccounts"),
        QUESTIONS_BY_USER("questionsByUser"),
        ANSWERS_BY_USER("answersByUser"),
        LOGOUT("logout");
        private final String extra;

        private UserIntentAction(String extra)
        {
            this.extra = extra;
        }

        public String getExtra()
        {
            return extra;
        }
    }
}

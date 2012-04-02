package com.prasanna.android.stacknetwork.utils;

public class IntentActionEnum
{
    public enum QuestionIntentAction implements IntentAction
    {
        QUESTIONS("questions"),
        QUESTION_BODY("questioBody"),
        QUESTION_FULL_DETAILS("questionFullDetails"),
        QUESTION_ANSWERS("questionAnswers"),
        QUESTION_COMMENTS("questionComments"),
        QUESTION_SEARCH("questionSearch"), ;
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
        ALL_USERS("users"),
        USER_DETAIL("userDetail"),
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

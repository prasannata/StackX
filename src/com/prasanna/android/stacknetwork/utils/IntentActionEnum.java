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

public class IntentActionEnum
{
    public interface IntentAction
    {
        String getExtra();
    }

    public enum QuestionIntentAction implements IntentAction
    {
        QUESTIONS("com.prasanna.stacknetwork.questions"),
        QUESTION_BODY("com.prasanna.stacknetwork.questionBody"),
        QUESTION_FULL_DETAILS("com.prasanna.stacknetwork.questionFullDetails"),
        QUESTION_ANSWERS("com.prasanna.stacknetwork.questionAnswers"),
        QUESTION_COMMENTS("com.prasanna.stacknetwork.questionComments"),
        QUESTION_SEARCH("com.prasanna.stacknetwork.questionSearch"),
        TAGS_FAQ("com.prasanna.stacknetwork.tagsFaq");
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
        INBOX("com.prasanna.stacknetwork.inbox"),
        ALL_USERS("com.prasanna.stacknetwork.users"),
        USER_DETAIL("com.prasanna.stacknetwork.userDetail"),
        USER_ACCOUNTS("com.prasanna.stacknetwork.userAccounts"),
        QUESTIONS_BY_USER("com.prasanna.stacknetwork.questionsByUser"),
        ANSWERS_BY_USER("com.prasanna.stacknetwork.answersByUser"),
        LOGOUT("com.prasanna.stacknetwork.logout"),
        NEW_MSG("com.prasanna.stacknetwork.newMsg");
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

    public enum ErrorIntentAction implements IntentAction
    {
        HTTP_ERROR("com.prasanna.stacknetwork.http.error");

        private final String extra;

        private ErrorIntentAction(String extra)
        {
            this.extra = extra;
        }

        @Override
        public String getExtra()
        {
            return extra;
        }
    }
}
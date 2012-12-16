/*
    Copyright 2012 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.utils;

public class IntentActionEnum
{
    public enum QuestionIntentAction implements IntentAction
    {
	ALL_QUESTIONS("questions"), QUESTION_DETAILS("questionDetails"), QUESTION_COMMENTS(
	        "questionComments");
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
	ALL_USERS("users"), USER_DETAIL("userDetail"), QUESTIONS_BY_USER("questionsByUser");
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

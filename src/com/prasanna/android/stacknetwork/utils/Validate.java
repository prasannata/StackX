package com.prasanna.android.stacknetwork.utils;

public class Validate
{
    private static final String DEFAULT_FAIL_MESSAGE = "Validation failed";

    private static String getMessage(String message)
    {
	if (message != null)
	    return message;

	return DEFAULT_FAIL_MESSAGE;
    }

    public static void notNull(Object object, String message)
    {
	if (object == null)
	{
	    throw new IllegalArgumentException(getMessage(message));
	}
    }

    public static void notNull(Object... objects)
    {
	if (objects == null)
	{
	    throw new IllegalArgumentException(getMessage(DEFAULT_FAIL_MESSAGE));
	}

	for (int i = 0; i < objects.length; i++)
	{
	    notNull(objects[i], "Arg " + i + " is null");
	}
    }

}

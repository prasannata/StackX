package com.prasanna.android.stacknetwork.exceptions;

public class HttpErrorException extends RuntimeException
{
    private static final long serialVersionUID = 653718270475335315L;
    private final int code;

    public HttpErrorException(int code, String text)
    {
	super(text);
	this.code = code;
    }

    public int getCode()
    {
	return code;
    }
}

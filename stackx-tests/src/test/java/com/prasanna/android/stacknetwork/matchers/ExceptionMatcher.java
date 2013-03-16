package com.prasanna.android.stacknetwork.matchers;

import com.prasanna.android.http.HttpException;

public class ExceptionMatcher<T extends HttpException>
{
    public boolean isEqual(T expectedException, T exception)
    {
        if (expectedException == null && exception == null)
            return true;

        if (expectedException == null)
            return false;

        if (expectedException.getStatusCode() != exception.getStatusCode())
            return false;

        if (expectedException.getCode() == null && exception.getCode() != null)
            return false;

        if (expectedException.getCode() != null && exception.getCode() == null)
            return false;

        if (exception.getCode() != null && !exception.getCode().equals(expectedException.getCode()))
            return false;

        if (expectedException.getStatusDescription() == null && exception.getStatusDescription() != null)
            return false;

        if (expectedException.getStatusDescription() != null && exception.getStatusDescription() == null)
            return false;

        if (exception.getStatusDescription() != null
                        && !exception.getStatusDescription().equals(expectedException.getStatusDescription()))
            return false;

        if (expectedException.getErrorResponse() == null && exception.getErrorResponse() != null)
            return false;

        if (expectedException.getErrorResponse() != null && exception.getErrorResponse() == null)
            return false;

        if (exception.getErrorResponse() != null
                        && !exception.getErrorResponse().equals(expectedException.getErrorResponse()))
            return false;

        return true;
    }
}

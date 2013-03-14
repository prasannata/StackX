package com.prasanna.android.stacknetwork.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.prasanna.android.http.ServerException;

public class ServerExceptionMatcher extends BaseMatcher<ServerException>
{
    private final ServerException expectedException;

    public ServerExceptionMatcher(ServerException expectedException)
    {
        this.expectedException = expectedException;
    }

    @Override
    public boolean matches(Object item)
    {
        ServerException exception = (ServerException) item;

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

    @Override
    public void describeTo(Description description)
    {
        // TODO Auto-generated method stub

    }

}

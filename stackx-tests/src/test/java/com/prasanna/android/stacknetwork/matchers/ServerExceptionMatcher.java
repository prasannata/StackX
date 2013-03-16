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
        return new ExceptionMatcher<ServerException>().isEqual(expectedException, (ServerException) item);
    }

    @Override
    public void describeTo(Description description)
    {
    }

}

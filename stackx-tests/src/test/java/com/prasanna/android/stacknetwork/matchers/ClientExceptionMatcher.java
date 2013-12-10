package com.prasanna.android.stacknetwork.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.prasanna.android.http.ClientException;

public class ClientExceptionMatcher extends BaseMatcher<ClientException> {
  private final ClientException expectedException;

  public ClientExceptionMatcher(ClientException expectedException) {
    this.expectedException = expectedException;
  }

  @Override
  public boolean matches(Object item) {
    return new ExceptionMatcher<ClientException>().isEqual(expectedException, (ClientException) item);
  }

  @Override
  public void describeTo(Description description) {
  }

}

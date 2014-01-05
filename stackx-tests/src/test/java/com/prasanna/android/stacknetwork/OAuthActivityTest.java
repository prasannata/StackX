package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowWebView;

import android.net.Uri;
import android.webkit.WebView;

import com.prasanna.android.stacknetwork.utils.StackUri;

@RunWith(RobolectricTestRunner.class)
public class OAuthActivityTest extends AbstractBaseActivityTest {
  private OAuthActivity oAuthActivity;

  @Before
  public void setup() {
    OAuthActivity.setTestMode();
    oAuthActivity = createActivity(OAuthActivity.class);
  }

  @Test
  public void oauthWithWebView() throws MalformedURLException {
    WebView webView = (WebView) oAuthActivity.findViewById(R.id.web_view);
    assertNotNull(webView);
    ShadowWebView shadowWebView = Robolectric.shadowOf(webView);

    URL expectedUrl = new URL(StackUri.OAUTH_DIALOG_URL);
    URL url = new URL(shadowWebView.getLastLoadedUrl());

    assertOAuthUrl(expectedUrl, url);
  }

  private void assertOAuthUrl(URL expectedUrl, URL url) {
    assertNotNull(url);
    assertEquals(expectedUrl.getProtocol(), url.getProtocol());
    assertEquals(expectedUrl.getHost(), url.getHost());
    assertEquals(expectedUrl.getPath(), url.getPath());

    assertQueryParams(url);
  }

  private void assertQueryParams(URL url) {
    HashMap<String, String> expectedQueryParams = new HashMap<String, String>();
    expectedQueryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);
    expectedQueryParams.put(StackUri.QueryParams.SCOPE, StackUri.QueryParamDefaultValues.SCOPE);
    expectedQueryParams.put(StackUri.QueryParams.REDIRECT_URI, StackUri.QueryParamDefaultValues.REDIRECT_URI);

    String query = url.getQuery();
    assertNotNull(query);
    for (String param : query.split("&")) {
      String[] nameValuePair = param.split("=");
      assertNotNull(nameValuePair);
      assertTrue(nameValuePair.length == 2);
      assertEquals(Uri.encode(expectedQueryParams.get(nameValuePair[0])), nameValuePair[1]);
      expectedQueryParams.remove(nameValuePair[0]);
    }

    assertTrue(expectedQueryParams.isEmpty());
  }
}

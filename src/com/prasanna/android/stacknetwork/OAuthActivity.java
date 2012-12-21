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

package com.prasanna.android.stacknetwork;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.prasanna.android.stacknetwork.utils.AlarmUtils;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class OAuthActivity extends Activity
{
    private static final String TAG = OAuthActivity.class.getName();
    private static final String YAHOO_LOGIN_URL = "https://login.yahoo.com";
    private static final String PROGRESS_BAR_TEXT = "to stackexchange.com...";
    private static final String LOGIN = "Login";

    private ProgressDialog progressDialog;
    private String oauthUrl;

    private class OAuthWebViewClient extends WebViewClient
    {
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
	{
	    handler.proceed();
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
	    Log.d(TAG, url);

	    if (url.startsWith(StringConstants.OAUTH_REDIRECT_URL))
	    {
		Intent listStackNetworkIntent = new Intent(view.getContext(), StackNetworkListActivity.class);

		Uri uri = Uri.parse(url);
		String accessToken = uri.getFragment();
		if (accessToken != null)
		{
		    String[] nameValuePair = accessToken.split("=");
		    if (nameValuePair != null && nameValuePair.length == 2
			            && nameValuePair[0].equals(StringConstants.ACCESS_TOKEN))
		    {
			CacheUtils.cacheAccessToken(getApplicationContext(), nameValuePair[1]);
		    }
		}

		AlarmUtils.createInboxRefreshAlarm(OAuthActivity.this);

		startActivity(listStackNetworkIntent);

		finish();
	    }
	    else
	    {
		view.loadUrl(url);
	    }
	    return true;
	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
	    Log.d(TAG, "onPageFinished: " + url);

	    if (oauthUrl != null && url.startsWith(StackUri.OAUTH_DIALOG_URL) && progressDialog != null)
	    {
		progressDialog.dismiss();
		progressDialog = null;
	    }
	    else
	    {
		/*
	         * Yahoo's login page does not seem to support horizontal
	         * scrolling inside webview, so enabling wide view port. Not
	         * good.
	         */
		if (url.startsWith(YAHOO_LOGIN_URL))
		{
		    view.getSettings().setUseWideViewPort(true);
		}
	    }
	    super.onPageFinished(view, url);
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	
	progressDialog = ProgressDialog.show(OAuthActivity.this, "", PROGRESS_BAR_TEXT);
	setContentView(R.layout.webview);
	WebView webview = initWebview();

	Builder uriBuilder = Uri.parse(StackUri.OAUTH_DIALOG_URL).buildUpon();
	uriBuilder.appendQueryParameter(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);
	uriBuilder.appendQueryParameter(StackUri.QueryParams.SCOPE, StackUri.QueryParamDefaultValues.SCOPE);
	uriBuilder.appendQueryParameter(StackUri.QueryParams.REDIRECT_URI,
	                StackUri.QueryParamDefaultValues.REDIRECT_URI);

	oauthUrl = uriBuilder.build().toString();
	webview.loadUrl(oauthUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView initWebview()
    {
	WebView webview = (WebView) findViewById(R.id.web_view);
	webview.setVerticalScrollBarEnabled(true);
	webview.setHorizontalScrollBarEnabled(true);
	webview.getSettings().setJavaScriptEnabled(true);
	webview.getSettings().setDomStorageEnabled(true);
	webview.getSettings().setLoadsImagesAutomatically(true);
	webview.getSettings().setSaveFormData(false);
	webview.getSettings().setSavePassword(false);
	webview.getSettings().setBlockNetworkImage(false);
	webview.getSettings().setUseWideViewPort(false);
	webview.setWebChromeClient(new WebChromeClient()
	{
	    public void onProgressChanged(WebView view, int progress)
	    {
		setTitle("Loading...");
		setProgress(progress * 100);
		if (progress == 100)
		{
		    setTitle(LOGIN);
		}
	    }
	});
	webview.setWebViewClient(new OAuthWebViewClient());
	webview.requestFocus(View.FOCUS_DOWN);
	return webview;
    }
}

package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class OAuthActivity extends Activity
{
    private static final String TAG = OAuthActivity.class.getName();

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
			Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
			                .edit();
			prefEditor.putString(StringConstants.ACCESS_TOKEN, nameValuePair[1]);
			prefEditor.commit();
		    }
		}

		startActivity(listStackNetworkIntent);
		finish();
	    }
	    else
	    {
		view.loadUrl(url);
	    }
	    return true;
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.webview);
	WebView webview = (WebView) findViewById(R.id.web_view);
	webview.setVerticalScrollBarEnabled(true);
	webview.setHorizontalScrollBarEnabled(true);
	webview.getSettings().setJavaScriptEnabled(true);
	webview.getSettings().setDomStorageEnabled(true);
	webview.getSettings().setLoadsImagesAutomatically(true);
	webview.getSettings().setSaveFormData(false);
	webview.getSettings().setSavePassword(false);
	webview.getSettings().setBlockNetworkImage(false);
	webview.setWebChromeClient(new WebChromeClient());
	webview.setWebViewClient(new OAuthWebViewClient());
	webview.requestFocus(View.FOCUS_DOWN);

	Builder uriBuilder = Uri.parse(StackUri.OAUTH_DIALOG_URL).buildUpon();
	uriBuilder.appendQueryParameter(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParams.CLIENT_ID);
	uriBuilder.appendQueryParameter(StackUri.QueryParams.SCOPE, StackUri.QueryParams.CLIENT_ID);
	uriBuilder.appendQueryParameter(StackUri.QueryParams.REDIRECT_URI, StackUri.QueryParams.CLIENT_ID);

	webview.loadUrl(uriBuilder.build().toString());
    }
}

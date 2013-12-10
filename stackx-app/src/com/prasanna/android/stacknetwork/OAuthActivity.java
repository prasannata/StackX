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
import android.widget.ProgressBar;

import com.prasanna.android.stacknetwork.sqlite.TagDAO;
import com.prasanna.android.stacknetwork.utils.AlarmUtils;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class OAuthActivity extends Activity {
    private static final String TAG = OAuthActivity.class.getName();
    private static final String YAHOO_LOGIN_URL = "https://login.yahoo.com";
    private static final String PROGRESS_BAR_TEXT = "to stackexchange.com...";
    private static final String LOGIN = "Login";

    private ProgressDialog progressDialog;
    private String oauthUrl;

    private class OAuthWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error != null) {
                Log.e(TAG, error.toString());
            }

            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogWrapper.d(TAG, url);

            if (url.startsWith(StringConstants.OAUTH_REDIRECT_URL)) {
                deleteStoredTags();

                AlarmUtils.cancelInboxRefreshAlarm(getApplicationContext());
                AlarmUtils.cancelPeriodicAccountSync(getApplicationContext());
                cacheAccessToken(url);
                AlarmUtils.activatePeriodicAccountSync(getApplicationContext());
                startSiteListActivity(view);
                OAuthActivity.this.finish();
            }
            else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (oauthUrl != null && url.startsWith(StackUri.OAUTH_DIALOG_URL) && progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            else {
                /*
                 * Yahoo's login page does not seem to support horizontal
                 * scrolling inside webview, so enabling wide view port. Not
                 * good.
                 */
                if (url.startsWith(YAHOO_LOGIN_URL))
                    view.getSettings().setUseWideViewPort(true);
            }
            super.onPageFinished(view, url);
        }
    }

    private void startSiteListActivity(WebView view) {
        Intent listStackNetworkIntent = new Intent(view.getContext(), StackNetworkListActivity.class);
        listStackNetworkIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        listStackNetworkIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        listStackNetworkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(listStackNetworkIntent);
    }

    private void cacheAccessToken(String url) {
        Uri uri = Uri.parse(url);
        String accessToken = uri.getFragment();
        if (accessToken != null) {
            String[] nameValuePair = accessToken.split("=");
            if (nameValuePair != null && nameValuePair.length == 2
                    && nameValuePair[0].equals(StringConstants.ACCESS_TOKEN)) {
                AppUtils.setAccessToken(getApplicationContext(), nameValuePair[1]);
            }
        }
    }

    private void deleteStoredTags() {
        TagDAO tagDAO = new TagDAO(this);

        try {
            tagDAO.open();
            tagDAO.deleteAll();
        }
        finally {
            tagDAO.close();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        AppUtils.clearDefaultSite(getApplicationContext());
        webview.loadUrl(oauthUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView initWebview() {
        final WebView webview = (WebView) findViewById(R.id.web_view);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.webviewProgress);

        webview.setVerticalScrollBarEnabled(true);
        webview.setHorizontalScrollBarEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setSaveFormData(false);
        webview.getSettings().setSavePassword(false);
        webview.getSettings().setBlockNetworkImage(false);
        webview.getSettings().setUseWideViewPort(false);
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                setTitle("Loading...");

                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }

                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                    setTitle(LOGIN);
                }
            }
        });
        webview.setWebViewClient(new OAuthWebViewClient());
        webview.requestFocus(View.FOCUS_DOWN);
        return webview;
    }
}

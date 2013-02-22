/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.prasanna.android.http.HttpContentTypes;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class FullscreenTextActivity extends Activity
{
    private final String BASE_URL = "file:///android_asset/google_code_prettify/prettify.js";
    private final String CODE_HTML_PREFIX = "<html><head>"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
                    + "<link href=\"prettify.css\" type=\"text/css\" rel=\"stylesheet\" />"
                    + "<script type=\"text/javascript\" src=\"prettify.js\"></script>"
                    + "<title>Insert title here</title>" + "</head>"
                    + "<body onload=\"prettyPrint();\" bgcolor=\"white\">" + "<pre class=\"prettyprint linenums\">";
    private final String CODE_HTML_SUFFIX = "</pre></body></html>";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.code_full_screen);

        getActionBar().hide();

        CharSequence text = getIntent().getCharSequenceExtra(StringConstants.TEXT);

        WebView webView = (WebView) findViewById(R.id.fullScreenText);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(BASE_URL, CODE_HTML_PREFIX + MarkdownFormatter.escapeHtml(text) + CODE_HTML_SUFFIX,
                        HttpContentTypes.TEXT_HTML, HTTP.UTF_8, null);
    }
}

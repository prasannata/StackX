package com.prasanna.android.stacknetwork;

import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FullscreenTextActivity extends Activity
{
    private String htmlString = "<html><head>"
	            + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
	            + "<link href=\"prettify.css\" type=\"text/css\" rel=\"stylesheet\" />"
	            + "<script type=\"text/javascript\" src=\"prettify.js\"></script>"
	            + "<title>Insert title here</title>" + "</head>"
	            + "<body onload=\"prettyPrint();\" bgcolor=\"white\">" + "<pre class=\"prettyprint linenums\">";

    private String tail = "</pre></body></html>";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.code_full_screen);

	getActionBar().hide();
	WebView webView = (WebView) findViewById(R.id.fullScreenText);
	CharSequence charSequenceExtra = getIntent().getCharSequenceExtra("text");
	webView.setWebChromeClient(new WebChromeClient());
	webView.setWebViewClient(new WebViewClient());
	webView.getSettings().setJavaScriptEnabled(true);
	webView.loadDataWithBaseURL("file:///android_asset/google_code_prettify/prettify.js", htmlString
	                + MarkdownFormatter.escapeHtml(charSequenceExtra) + tail, "text/html", "utf-8", null);
    }
}

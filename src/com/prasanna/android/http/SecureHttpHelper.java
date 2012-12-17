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

package com.prasanna.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.stacknetwork.utils.Validate;

public final class SecureHttpHelper
{
    private final String TAG = SecureHttpHelper.class.getSimpleName();

    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final String GZIP = "gzip";
    private static final HttpGzipResponseInterceptor gzipHttpInterceptor = new HttpGzipResponseInterceptor(GZIP,
	            GzipDecompressingEntity.class);

    private static class GzipDecompressingEntity extends HttpEntityWrapper
    {
	public GzipDecompressingEntity(final HttpEntity entity)
	{
	    super(entity);
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException
	{
	    return new GZIPInputStream(wrappedEntity.getContent());
	}

	@Override
	public long getContentLength()
	{
	    return -1;
	}
    }

    private static SecureHttpHelper httpHelper = new SecureHttpHelper();

    private SecureHttpHelper()
    {
    }

    public static SecureHttpHelper getInstance()
    {
	return httpHelper;
    }

    public Bitmap fetchImage(String absoluteUrl)
    {
	Bitmap bitmap = null;
	try
	{
	    if (absoluteUrl != null)
	    {
		DefaultHttpClient client = createHttpClient();
		HttpGet request = new HttpGet(absoluteUrl);

		HttpResponse response = client.execute(request);
		bitmap = BitmapFactory.decodeStream(response.getEntity().getContent());
	    }
	}
	catch (ClientProtocolException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (IOException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (KeyManagementException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (UnrecoverableKeyException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (KeyStoreException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (NoSuchAlgorithmException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (CertificateException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	return bitmap;
    }

    public JSONObjectWrapper executeForGzipResponse(String host, String path, Map<String, String> queryParams)
    {
	JSONObjectWrapper jsonObject = null;
	try
	{
	    DefaultHttpClient client = getClientForGzipResponse();
	    HttpGet request = new HttpGet(buildUri(host, path, queryParams));
	    request.setHeader(HttpHeaderParams.ACCEPT, HttpContentTypes.APPLICATION_JSON);

	    Log.d(TAG, "HTTP request to: " + request.getURI().toString());

	    HttpResponse httpResponse = client.execute(request);
	    HttpEntity entity = httpResponse.getEntity();
	    String jsonText = EntityUtils.toString(entity, HTTP.UTF_8);
	    int statusCode = httpResponse.getStatusLine().getStatusCode();

	    if (statusCode == HttpStatus.SC_OK)
	    {
		jsonObject = new JSONObjectWrapper(new JSONObject(jsonText));
	    }
	    else
	    {
		Log.d(TAG, "Http request failed: " + statusCode);
		Log.d(TAG, "Http request failure message: " + jsonText);

		JSONObject error = new JSONObject(jsonText);
		error.put(StringConstants.ERROR, true);
		error.put(StringConstants.STATUS_CODE, statusCode);
		jsonObject = new JSONObjectWrapper(error);
	    }
	}

	catch (ClientProtocolException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (IOException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (JSONException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (KeyManagementException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (UnrecoverableKeyException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (KeyStoreException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (NoSuchAlgorithmException e)
	{
	    Log.e(TAG, e.getMessage());
	}
	catch (CertificateException e)
	{
	    Log.e(TAG, e.getMessage());
	}

	return jsonObject;
    }

    private DefaultHttpClient getClientForGzipResponse() throws KeyStoreException, NoSuchAlgorithmException,
	            CertificateException, IOException, KeyManagementException, UnrecoverableKeyException
    {
	DefaultHttpClient client = createHttpClient();
	client.addResponseInterceptor(gzipHttpInterceptor);
	return client;
    }

    private DefaultHttpClient createHttpClient() throws KeyStoreException, IOException, NoSuchAlgorithmException,
	            CertificateException, KeyManagementException, UnrecoverableKeyException
    {
	KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	trustStore.load(null, null);

	SSLSocketFactory sf = new SSLSocketFactoryX509(trustStore);
	sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	HttpParams params = new BasicHttpParams();
	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
	SchemeRegistry schemeRegistry = new SchemeRegistry();
	schemeRegistry.register(new Scheme(SCHEME_HTTPS, sf, HTTPS_PORT));
	schemeRegistry.register(new Scheme(SCHEME_HTTP, PlainSocketFactory.getSocketFactory(), HTTP_PORT));

	SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
	return new DefaultHttpClient(mgr, params);
    }

    public static String buildUri(String host, String path, Map<String, String> queryParams)
    {
	Validate.notNull(host, path);

	Builder uriBuilder = Uri.parse(host).buildUpon();
	uriBuilder = uriBuilder.appendPath(path);
	if (queryParams != null)
	{
	    for (Map.Entry<String, String> entrySet : queryParams.entrySet())
	    {
		uriBuilder.appendQueryParameter(entrySet.getKey(), Uri.encode(entrySet.getValue()));
	    }
	}

	return uriBuilder.build().toString();
    }
}

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.Validate;
import com.prasanna.android.utils.LogWrapper;

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

    public static class HttpErrorFamily
    {
        public static final int CLIENT_ERROR = 400;
        public static final int SERVER_ERROR = 500;
    }

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
                LogWrapper.d(TAG, "Get image: " + absoluteUrl);

                DefaultHttpClient client = createHttpClient();
                HttpGet request = new HttpGet(absoluteUrl);

                HttpResponse response = client.execute(request);
                bitmap = BitmapFactory.decodeStream(response.getEntity().getContent());
            }
        }
        catch (ClientProtocolException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (IOException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (KeyManagementException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (UnrecoverableKeyException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (KeyStoreException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (CertificateException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        return bitmap;
    }

    public JSONObjectWrapper executePostForGzipResponse(String host, String path, Map<String, String> requestHeaders,
                    Map<String, String> queryParams, HttpEntity httpEntity)
    {
        try
        {
            DefaultHttpClient client = getClientForGzipResponse();
            HttpPost request = new HttpPost(buildUri(host, path, queryParams));

            if (requestHeaders != null)
            {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet())
                    request.setHeader(entry.getKey(), entry.getValue());
            }
            request.setEntity(httpEntity);

            return executeRequest(client, request);
        }
        catch (ClientProtocolException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (IOException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (JSONException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (KeyManagementException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (UnrecoverableKeyException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (KeyStoreException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (CertificateException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }

        return null;
    }

    public JSONObjectWrapper executeGetForGzipResponse(String host, String path, Map<String, String> queryParams)
    {
        try
        {
            DefaultHttpClient client = getClientForGzipResponse();
            HttpGet request = new HttpGet(buildUri(host, path, queryParams));
            request.setHeader(HttpHeaderParams.ACCEPT, HttpContentTypes.APPLICATION_JSON);

            return executeRequest(client, request);
        }
        catch (ClientProtocolException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (IOException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (JSONException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (KeyManagementException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (UnrecoverableKeyException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (KeyStoreException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }
        catch (CertificateException e)
        {
            LogWrapper.e(TAG, e.getMessage());
        }

        return null;
    }

    private JSONObjectWrapper executeRequest(DefaultHttpClient client, HttpRequestBase request) throws IOException,
                    ClientProtocolException, JSONException
    {
        JSONObjectWrapper jsonObject = null;

        LogWrapper.d(TAG, "HTTP request to: " + request.getURI().toString());
        HttpResponse httpResponse = client.execute(request);
        HttpEntity entity = httpResponse.getEntity();
        String jsonText = EntityUtils.toString(entity, HTTP.UTF_8);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_OK)
        {
            LogWrapper.d(TAG, request + " was successful");
            jsonObject = new JSONObjectWrapper(new JSONObject(jsonText));
        }
        else
        {
            LogWrapper.d(TAG, "Http request failed: " + statusCode);
            LogWrapper.d(TAG, "Http request failure message: " + jsonText);
            String statusDescription = httpResponse.getStatusLine().getReasonPhrase();

            if (statusCode >= HttpErrorFamily.SERVER_ERROR)
                throw new ServerException(statusCode, statusDescription, jsonText);
            else if (statusCode >= HttpErrorFamily.CLIENT_ERROR)
                throw new ClientException(statusCode, statusDescription, jsonText);
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

        Builder uriBuilder = Uri.parse(host).buildUpon().appendPath(path);
        if (queryParams != null)
        {
            for (Map.Entry<String, String> entrySet : queryParams.entrySet())
            {
                uriBuilder.appendQueryParameter(entrySet.getKey(), entrySet.getValue());
            }
        }

        return uriBuilder.build().toString();
    }
}

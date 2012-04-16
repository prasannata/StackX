package com.prasanna.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
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
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;

public class HttpHelper
{
    private final String TAG = HttpHelper.class.getSimpleName();

    private String host;
    private static final String GZIP = "gzip";

    static class GzipDecompressingEntity extends HttpEntityWrapper
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

    private static HttpHelper httpHelper = new HttpHelper();

    private HttpHelper()
    {
    }

    public static HttpHelper getInstance()
    {
        return httpHelper;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public class MySSLSocketFactory extends SSLSocketFactory
    {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException
        {
            super(truststore);

            TrustManager tm = new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                }

                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]
            { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                UnknownHostException
        {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException
        {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public Bitmap fetchImage(String absoluteUrl)
    {
        Bitmap bitmap = null;
        try
        {
            if (absoluteUrl != null)
            {
                DefaultHttpClient client = getClient(null, null);
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

    public JSONObjectWrapper getRequestForJsonWithGzipEncoding(String path, Map<String, String> queryParams)
    {
        JSONObjectWrapper jsonObject = null;
        try
        {
            DefaultHttpClient client = getClient(GZIP, GzipDecompressingEntity.class);
            HttpGet request = new HttpGet(buildDecodedUri(path, queryParams));
            request.setHeader(HttpHeaderParams.ACCEPT, HttpContentTypes.APPLICATION_JSON);
            Log.d(TAG, "HTTP request to: " + request.getURI().toString());
            HttpResponse httpResponse = client.execute(request);
            HttpEntity entity = httpResponse.getEntity();
            String jsonText = EntityUtils.toString(entity, HTTP.UTF_8);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                jsonObject = new JSONObjectWrapper(new JSONObject(jsonText));
            }
            else
            {
                Log.d(TAG, "Http request failed: " + httpResponse.getStatusLine().getStatusCode());
                Log.d(TAG, "Http request failure message: " + jsonText);
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

    private DefaultHttpClient getClient(final String contentEncoding,
            final Class<? extends HttpEntityWrapper> entityWrapper) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException, UnrecoverableKeyException
    {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https", sf, 443));
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
        DefaultHttpClient client = new DefaultHttpClient(mgr, params);

        if (contentEncoding != null && entityWrapper != null)
        {
            client.addResponseInterceptor(new HttpResponseInterceptor()
            {
                public void process(final HttpResponse response, final HttpContext context) throws HttpException,
                        IOException
                {
                    HttpEntity entity = response.getEntity();
                    Header encodingHeader = entity.getContentEncoding();
                    if (encodingHeader != null)
                    {
                        HeaderElement[] codecs = encodingHeader.getElements();
                        for (int i = 0; i < codecs.length; i++)
                        {
                            if (codecs[i].getName().equalsIgnoreCase(contentEncoding))
                            {
                                try
                                {
                                    entityWrapper.getConstructor(HttpEntity.class).newInstance(entity);
                                    response.setEntity(new GzipDecompressingEntity(entity));
                                    return;
                                }
                                catch (IllegalArgumentException e)
                                {
                                    Log.e(TAG, e.getMessage());
                                }
                                catch (SecurityException e)
                                {
                                    Log.e(TAG, e.getMessage());
                                }
                                catch (InstantiationException e)
                                {
                                    Log.e(TAG, e.getMessage());
                                }
                                catch (IllegalAccessException e)
                                {
                                    Log.e(TAG, e.getMessage());
                                }
                                catch (InvocationTargetException e)
                                {
                                    Log.e(TAG, e.getMessage());
                                }
                                catch (NoSuchMethodException e)
                                {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
            });
        }

        return client;
    }

    public String buildDecodedUri(String path, Map<String, String> queryParams)
    {
        Builder uriBuilder = Uri.parse(host).buildUpon();
        uriBuilder = uriBuilder.appendPath(path);
        if (queryParams != null)
        {
            for (Map.Entry<String, String> entrySet : queryParams.entrySet())
            {
                uriBuilder.appendQueryParameter(entrySet.getKey(), Uri.encode(entrySet.getValue()));
            }
        }

        return Uri.decode(uriBuilder.build().toString());
    }
}

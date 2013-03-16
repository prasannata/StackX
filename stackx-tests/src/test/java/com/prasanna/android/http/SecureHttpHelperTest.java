package com.prasanna.android.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import android.graphics.Bitmap;

import com.prasanna.android.stacknetwork.matchers.ClientExceptionMatcher;
import com.prasanna.android.stacknetwork.matchers.ServerExceptionMatcher;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.QueryParamDefaultValues;

@RunWith(MockitoJUnitRunner.class)
public class SecureHttpHelperTest
{
    private static final String SCHEME = "https";
    private static final String HOST = "www.example.com";
    private static final String PATH = "/v1/path";

    @Mock
    private HttpGzipResponseInterceptor httpGzipResponseInterceptor;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpPost httpPost;

    @Mock
    private HttpGet httpGet;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private SecureHttpHelperStub secureHttpHelper;

    private class SecureHttpHelperStub extends SecureHttpHelper
    {
        @Override
        protected HttpClient createSecureHttpClient()
        {
            return httpClient;
        }

        @Override
        protected HttpGet getHttpGetObject(String absoluteUrl)
        {
            mockGetUri(httpGet, absoluteUrl);
            return httpGet;
        }

        @Override
        protected HttpPost getHttpPostObject(String absoluteUrl)
        {
            mockGetUri(httpPost, absoluteUrl);
            return httpPost;
        }

        @Override
        protected Bitmap getBitmap(HttpResponse response) throws IOException
        {
            return null;
        }

        protected String buildUri(String host, String path, Map<String, String> queryParams)
        {
            assertEquals(HOST, host);
            assertEquals(PATH, path);
            assertEquals(getQueryParams(), queryParams);

            try
            {
                URIBuilder uriBuilder =
                                new URIBuilder().setScheme("https").setHost("www.example.com").setPath("/v1/path");
                for (String key : queryParams.keySet())
                    uriBuilder.addParameter(key, queryParams.get(key));

                return uriBuilder.build().toASCIIString();
            }
            catch (URISyntaxException e)
            {
                fail(e.getMessage());
            }

            return null;
        }

        private void mockGetUri(HttpRequestBase httpRequestBase, String absoluteUrl)
        {
            try
            {
                when(httpRequestBase.getURI()).thenReturn(new URI(absoluteUrl));
            }
            catch (URISyntaxException e)
            {
                fail(e.getMessage());
            }
        }
    }

    private Map<String, String> getQueryParams()
    {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.PAGE, "1");
        queryParams.put(StackUri.QueryParams.CLIENT_ID, QueryParamDefaultValues.CLIENT_ID);
        return queryParams;
    }

    private Map<String, String> getRequestHeaders()
    {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(HttpHeaderParams.CONTENT_TYPE, HttpContentTypes.APPLICATION_FROM_URL_ENCODED);
        requestHeaders.put(HttpHeaderParams.ACCEPT, HttpContentTypes.APPLICATION_JSON);
        return requestHeaders;
    }

    private <T extends HttpRequestBase> void verifyExecute(Class<T> argClass) throws IOException,
                    ClientProtocolException
    {
        ArgumentCaptor<T> httpGetArgCaptor = ArgumentCaptor.forClass(argClass);
        verify(httpClient, times(1)).execute(httpGetArgCaptor.capture());
        T value = httpGetArgCaptor.getValue();

        assertNotNull(value);
        URI uri = value.getURI();

        assertNotNull(uri);
        assertEquals(SCHEME, uri.getScheme());
        assertEquals(HOST, uri.getHost());
        assertEquals(PATH, uri.getPath());
        assertEquals(getExpectedQuery(), uri.getQuery());
    }

    private String getExpectedQuery()
    {
        URIBuilder uriBuilder = new URIBuilder();
        Map<String, String> queryParams = getQueryParams();
        for (String key : queryParams.keySet())
            uriBuilder.setParameter(key, queryParams.get(key));
        return uriBuilder.toString().substring(1);
    }

    private StatusLine getStatusLine(final int statusCode, final String reasonPhrase)
    {
        return new StatusLine()
        {

            @Override
            public int getStatusCode()
            {
                return statusCode;
            }

            @Override
            public String getReasonPhrase()
            {
                return reasonPhrase;
            }

            @Override
            public ProtocolVersion getProtocolVersion()
            {
                return new ProtocolVersion(SCHEME, 1, 1);
            }
        };
    }

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(SecureHttpHelperTest.class);
        secureHttpHelper = new SecureHttpHelperStub();
    }

    @Test
    public void executeHttpGet() throws ClientProtocolException, IOException
    {
        when(httpClient.execute(httpGet)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(getStatusLine(HttpStatus.SC_OK, "OK"));
        when(httpResponse.getEntity()).thenReturn(new StringEntity("{'var' : 'val'}"));

        JSONObjectWrapper jsonObjectWrapper = secureHttpHelper.executeHttpGet(HOST, PATH, getQueryParams(), null);
        assertNotNull(jsonObjectWrapper);
        assertEquals("val", jsonObjectWrapper.getString("var"));

        verifyExecute(HttpGet.class);
    }

    @Test
    public void executeHttpGetThrowsServerException() throws ClientProtocolException, IOException
    {
        expectedException.expect(ServerException.class);
        expectedException.expect(new ServerExceptionMatcher(new ServerException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Server Error", "{'error' : 'server'}")));

        when(httpClient.execute(httpGet)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(
                        getStatusLine(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Server Error"));
        when(httpResponse.getEntity()).thenReturn(new StringEntity("{'error' : 'server'}"));

        secureHttpHelper.executeHttpGet(HOST, PATH, getQueryParams(), null);
    }

    @Test
    public void executeHttpGetThrowsClientException() throws ClientProtocolException, IOException
    {
        expectedException.expect(ClientException.class);
        expectedException.expect(new ClientExceptionMatcher(new ClientException(HttpStatus.SC_BAD_REQUEST,
                        "Bad Request", "{'error' : 'client'}")));

        when(httpClient.execute(httpGet)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(getStatusLine(HttpStatus.SC_BAD_REQUEST, "Bad Request"));
        when(httpResponse.getEntity()).thenReturn(new StringEntity("{'error' : 'client'}"));

        secureHttpHelper.executeHttpGet(HOST, PATH, getQueryParams(), null);
    }

    @Test
    public void executeHttpPost() throws ClientProtocolException, IOException
    {
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(getStatusLine(HttpStatus.SC_OK, "OK"));
        when(httpResponse.getEntity()).thenReturn(new StringEntity("{'var' : 'val'}"));

        Map<String, String> requestHeaders = getRequestHeaders();
        JSONObjectWrapper jsonObjectWrapper =
                        secureHttpHelper.executeHttpPost(HOST, PATH, requestHeaders, getQueryParams(), null, null);
        assertNotNull(jsonObjectWrapper);
        assertEquals("val", jsonObjectWrapper.getString("var"));

        for (String key : requestHeaders.keySet())
            verify(httpPost, times(1)).setHeader(key, requestHeaders.get(key));

        verifyExecute(HttpPost.class);
    }

    @Test
    public void getImage() throws ClientProtocolException, IOException
    {
        final String URI = "http://www.example.com/image.png";
        when(httpClient.execute(httpGet)).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                return -1;
            }
        });

        secureHttpHelper.getImage(URI);

        ArgumentCaptor<HttpGet> httpGetArgCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient, times(1)).execute(httpGetArgCaptor.capture());
        HttpGet value = httpGetArgCaptor.getValue();
        assertNotNull(value);
        assertEquals(URI, value.getURI().toASCIIString());
    }

    @Test
    public void getImageThrowsIOException() throws ClientProtocolException, IOException
    {
        expectedException.expect(ClientException.class);

        final String URI = "http://www.example.com/image.png";
        when(httpClient.execute(httpGet)).thenThrow(new IOException("Connection closed"));

        assertNull(secureHttpHelper.getImage(URI));

        ArgumentCaptor<HttpGet> httpGetArgCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient, times(1)).execute(httpGetArgCaptor.capture());
        HttpGet value = httpGetArgCaptor.getValue();
        assertNotNull(value);
        assertEquals(URI, value.getURI().toASCIIString());
    }

    @Test
    public void getImageWithNullUrl() throws ClientProtocolException, IOException
    {
        assertNull(secureHttpHelper.getImage(null));
    }

}

package com.prasanna.android.stacknetwork.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.prasanna.android.http.HttpContentTypes;
import com.prasanna.android.http.HttpGzipResponseInterceptor;
import com.prasanna.android.http.HttpHeaderParams;
import com.prasanna.android.http.SecureHttpHelper;
import com.prasanna.android.json.JsonUtil;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.QueryParamDefaultValues;

@RunWith(MockitoJUnitRunner.class)
public class WriteServiceHelperTest extends AbstractBaseServiceHelperTest
{
    private class WriteServiceHelperStub extends WriteServiceHelper
    {
        @Override
        protected SecureHttpHelper getHttpHelper()
        {
            return httpHelper;
        }
    }

    private WriteServiceHelperStub writeServiceHelper;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(UserServiceHelperTest.class);
        writeServiceHelper = new WriteServiceHelperStub();
        setDefaultSite("Stack Overflow", "stackoverflow");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addComment() throws JSONException, UnsupportedEncodingException
    {
        Comment expectedComment = getComment(1L, 1L);

        JSONObjectWrapper jsonObjectWrapper = JsonUtil.commentToJsonObjectWrapper(expectedComment);

        when(
                        httpHelper.executeHttpPost(anyString(), anyString(), (Map<String, String>) anyMap(),
                                        (Map<String, String>) anyMap(), (UrlEncodedFormEntity) anyObject(),
                                        (HttpResponseInterceptor) anyObject())).thenReturn(jsonObjectWrapper);

        assertAddedComment(expectedComment, writeServiceHelper.addComment(1L, "body"), "/posts/1/comments/add");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void editComment() throws JSONException, UnsupportedEncodingException
    {
        Comment expectedComment = getComment(1L, 1L);

        JSONObjectWrapper jsonObjectWrapper = JsonUtil.commentToJsonObjectWrapper(expectedComment);

        when(
                        httpHelper.executeHttpPost(anyString(), anyString(), (Map<String, String>) anyMap(),
                                        (Map<String, String>) anyMap(), (UrlEncodedFormEntity) anyObject(),
                                        (HttpResponseInterceptor) anyObject())).thenReturn(jsonObjectWrapper);

        assertAddedComment(expectedComment, writeServiceHelper.editComment(1L, "body"), "/comments/1/edit");
    }

    @Test
    public void deleteComment()
    {
        writeServiceHelper.deleteComment(1L);
        assertDeletedComment("/comments/1/delete", getBasicNameValuePartListForWriteComment());
    }
    
    private void assertDeletedComment(String path, List<BasicNameValuePair> basicNameValuePartListForWriteComment)
    {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(HttpHeaderParams.CONTENT_TYPE, HttpContentTypes.APPLICATION_FROM_URL_ENCODED);

        verifyHttpPostExecuteInvocation(path, requestHeaders, getBasicNameValuePartListForWriteComment());
    }

    private void assertAddedComment(Comment comment, Comment addedComment, String path)
    {
        assertNotNull(addedComment);
        assertCommentEquals(comment, addedComment);
        verifyHttpPostExecuteInvocation(path, getExpectedRequestHeader(), getExpectedUrlEncodedNameValuePair(comment));
    }

    private List<BasicNameValuePair> getExpectedUrlEncodedNameValuePair(Comment comment)
    {
        List<BasicNameValuePair> nameValuePair = getBasicNameValuePartListForWriteComment();
        nameValuePair.add(new BasicNameValuePair(StackUri.QueryParams.FILTER,
                        QueryParamDefaultValues.ITEM_DETAIL_FILTER));
        nameValuePair.add(new BasicNameValuePair(StackUri.QueryParams.BODY, comment.body));
        return nameValuePair;
    }

    private Map<String, String> getExpectedRequestHeader()
    {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(HttpHeaderParams.CONTENT_TYPE, HttpContentTypes.APPLICATION_FROM_URL_ENCODED);
        requestHeaders.put(HttpHeaderParams.ACCEPT, HttpContentTypes.APPLICATION_JSON);
        return requestHeaders;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void verifyHttpPostExecuteInvocation(String expectedPath, Map<String, String> expectedRequestHeaders,
                    List<BasicNameValuePair> expectedNameValuePair)
    {
        ArgumentCaptor<String> hostArgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pathArgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> requestHeadersArgCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> queryParamsArgCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<UrlEncodedFormEntity> httpEntityArgCaptor = ArgumentCaptor.forClass(UrlEncodedFormEntity.class);
        ArgumentCaptor<HttpGzipResponseInterceptor> interceptorArgCaptor =
                        ArgumentCaptor.forClass(HttpGzipResponseInterceptor.class);

        verify(httpHelper, times(1)).executeHttpPost(hostArgCaptor.capture(), pathArgCaptor.capture(),
                        requestHeadersArgCaptor.capture(), queryParamsArgCaptor.capture(),
                        httpEntityArgCaptor.capture(), interceptorArgCaptor.capture());

        assertEquals(StackUri.STACKX_API_HOST, hostArgCaptor.getValue());
        assertEquals(expectedPath, pathArgCaptor.getValue());
        assertEquals(expectedRequestHeaders, requestHeadersArgCaptor.getValue());
        assertNull(queryParamsArgCaptor.getValue());
        assertEquals(SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR, interceptorArgCaptor.getValue());
    }
}

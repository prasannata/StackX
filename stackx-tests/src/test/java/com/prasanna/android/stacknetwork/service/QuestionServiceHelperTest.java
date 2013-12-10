package com.prasanna.android.stacknetwork.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.prasanna.android.http.HttpGzipResponseInterceptor;
import com.prasanna.android.http.SecureHttpHelper;
import com.prasanna.android.http.ServerException;
import com.prasanna.android.json.JsonUtil;
import com.prasanna.android.stacknetwork.matchers.ServerExceptionMatcher;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.service.AbstractBaseServiceHelper.JSONParser;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.Order;
import com.prasanna.android.stacknetwork.utils.StringConstants;

@RunWith(MockitoJUnitRunner.class)
public class QuestionServiceHelperTest extends AbstractBaseServiceHelperTest {

  private class QuestionServiceHelperStub extends QuestionServiceHelper {
    @Override
    protected SecureHttpHelper getHttpHelper() {
      return httpHelper;
    }
  }

  private QuestionServiceHelperStub questionServiceHelper;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(QuestionServiceHelperTest.class);
    questionServiceHelper = new QuestionServiceHelperStub();
    setDefaultSite("Stack Overflow", "stackoverflow");
  }

  @Test
  public void getQuestionBodyForId() throws JSONException, IOException {
    final String BODY = "body";
    JSONObjectWrapper jsonObjectWrapper = getMockQuestionBodyJsonObject();

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);

    when(
        httpHelper.executeHttpGet(StackUri.STACKX_API_HOST, "questions/" + 1, expectedQueryParams,
            SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR, AbstractBaseServiceHelper.JSON_PARSER)).thenReturn(
        jsonObjectWrapper);

    assertEquals(BODY, questionServiceHelper.getQuestionBodyForId(1, site.apiSiteParameter));
  }

  private JSONObjectWrapper getMockQuestionBodyJsonObject() throws JSONException, IOException {
    HashMap<String, String> nameValueMap = new HashMap<String, String>();
    nameValueMap.put(JsonFields.Question.BODY, "body");
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.toJsonObjectWrapper(nameValueMap);
    return jsonObjectWrapper;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getQuestionBodyForIdReturnsNull() {
    when(
        httpHelper.executeHttpGet(anyString(), anyString(), (Map<String, String>) anyMap(),
            (HttpGzipResponseInterceptor) anyObject(), (JSONParser) anyObject())).thenReturn(null);
    assertNull(questionServiceHelper.getQuestionBodyForId(1, "stackoverflow"));
  }

  @Test
  public void getQuestionFullDetails() throws JSONException, IOException {
    Question expectedQuestion = getQuestion(true, true);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.toJsonObjectWrapper(expectedQuestion);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);
    when(
        httpHelper.executeHttpGet(StackUri.STACKX_API_HOST, "questions/" + 1, expectedQueryParams,
            SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR, AbstractBaseServiceHelper.JSON_PARSER)).thenReturn(
        jsonObjectWrapper);

    Question question = questionServiceHelper.getQuestionFullDetails(1, site.apiSiteParameter);
    assertQuestionEquals(expectedQuestion, question);
  }

  @Test
  public void getQuestionComments() throws JSONException, IOException {
    final long POST_ID = 1001L;
    final int PAGE = 1;

    ArrayList<Comment> expectedComments = getComments(POST_ID);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.commentsToJsonObjectWrapper(expectedComments);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.COMMENT_FILTER);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.CREATION);
    expectedQueryParams.put(StackUri.QueryParams.ORDER, Order.ASC);

    String expectedRestEndpoint = StringConstants.QUESTIONS + "/" + POST_ID + "/" + StringConstants.COMMENTS;
    mockRestCall(expectedRestEndpoint, expectedQueryParams, jsonObjectWrapper);

    StackXPage<Comment> commentsPage =
        questionServiceHelper.getComments(StringConstants.QUESTIONS, site.apiSiteParameter, String.valueOf(POST_ID),
            PAGE);
    assertNotNull(commentsPage);
    assertCommentsEquals(expectedComments, commentsPage.items);
  }

  @Test
  public void getQuestionAnswers() throws JSONException, IOException {
    final long QUESTION_ID = 1001L;
    final int PAGE = 1;

    ArrayList<Answer> expectedAnswers = new ArrayList<Answer>();
    expectedAnswers.add(getAnswer(QUESTION_ID, true, false));
    expectedAnswers.add(getAnswer(QUESTION_ID, true, true));
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.answersToJsonObjectWrapper(expectedAnswers);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE,
        String.valueOf(StackUri.QueryParamDefaultValues.ANSWERS_PAGE_SIZE));
    expectedQueryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.VOTES);

    String expectedRestEndpoint = StringConstants.QUESTIONS + "/" + QUESTION_ID + "/" + StringConstants.ANSWERS;
    mockRestCall(expectedRestEndpoint, expectedQueryParams, jsonObjectWrapper);

    ArrayList<Answer> answers = questionServiceHelper.getAnswersForQuestion(QUESTION_ID, site.apiSiteParameter, PAGE);
    assertNotNull(answers);
    assertAnswersEquals(expectedAnswers, answers);
  }

  @Test
  public void getAnswerComments() throws JSONException, IOException {
    final long POST_ID = 1001L;
    final int PAGE = 1;

    ArrayList<Comment> expectedComments = getComments(POST_ID);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.commentsToJsonObjectWrapper(expectedComments);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.COMMENT_FILTER);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.CREATION);
    expectedQueryParams.put(StackUri.QueryParams.ORDER, Order.ASC);

    String expectedRestEndpoint = StringConstants.ANSWERS + "/" + POST_ID + "/" + StringConstants.COMMENTS;
    mockRestCall(expectedRestEndpoint, expectedQueryParams, jsonObjectWrapper);

    StackXPage<Comment> commentsPage =
        questionServiceHelper
            .getComments(StringConstants.ANSWERS, site.apiSiteParameter, String.valueOf(POST_ID), PAGE);
    assertNotNull(commentsPage);
    assertCommentsEquals(expectedComments, commentsPage.items);
  }

  @Test
  public void getAllQuestions() throws JSONException, IOException {
    final int PAGE = 1;
    final String SORT = StackUri.Sort.CREATION;

    ArrayList<Question> expectedQuestions = getQuestions(5);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.SORT, SORT);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
    expectedQueryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);

    mockRestCall(StringConstants.QUESTIONS, expectedQueryParams, jsonObjectWrapper);

    StackXPage<Question> questionsPage = questionServiceHelper.getAllQuestions(SORT, PAGE);
    assertNotNull(questionsPage);
    assertQuestionsEquals(expectedQuestions, questionsPage.items);
  }

  @Test
  public void getSimilar() throws JSONException, IOException {
    final String TITLE = "title";
    final int PAGE = 1;

    ArrayList<Question> expectedQuestions = getQuestions(5);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.TITLE, TITLE);
    expectedQueryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.RELEVANCE);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

    mockRestCall("similar", expectedQueryParams, jsonObjectWrapper);

    StackXPage<Question> questionsPage = questionServiceHelper.getSimilar(TITLE, PAGE);
    assertNotNull(questionsPage);
    assertQuestionsEquals(expectedQuestions, questionsPage.items);
  }

  @Test
  public void getRelated() throws JSONException, IOException {
    final long QUESTION_ID = 10001L;
    final int PAGE = 1;

    ArrayList<Question> expectedQuestions = getQuestions(5);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
    expectedQueryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

    String expectedRestEndpoint = StringConstants.QUESTIONS + "/" + QUESTION_ID + "/" + StringConstants.RELATED;
    mockRestCall(expectedRestEndpoint, expectedQueryParams, jsonObjectWrapper);

    StackXPage<Question> questionsPage = questionServiceHelper.getRelatedQuestions(QUESTION_ID, PAGE);
    assertNotNull(questionsPage);
    assertQuestionsEquals(expectedQuestions, questionsPage.items);
  }

  @Test
  public void getQuestionsForTag() throws JSONException, IOException {
    final String TAG = "tag";
    final int PAGE = 1;
    final String SORT = StackUri.Sort.CREATION;

    ArrayList<Question> expectedQuestions = getQuestions(5);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.TAGGED, TAG);
    expectedQueryParams.put(StackUri.QueryParams.SORT, SORT);
    expectedQueryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

    String expectedRestEndpoint = "search/advanced";
    mockRestCall(expectedRestEndpoint, expectedQueryParams, jsonObjectWrapper);

    StackXPage<Question> questionsPage = questionServiceHelper.getQuestionsForTag(TAG, SORT, PAGE);
    assertNotNull(questionsPage);
    assertQuestionsEquals(expectedQuestions, questionsPage.items);
  }

  @Test
  public void getFaqsForTag() throws JSONException, IOException {
    final String TAG = "tag";
    final int PAGE = 1;

    ArrayList<Question> expectedQuestions = getQuestions(5);
    JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

    String expectedRestEndpoint = StringConstants.TAGS + "/" + TAG + "/faq";
    mockRestCall(expectedRestEndpoint, expectedQueryParams, jsonObjectWrapper);

    StackXPage<Question> questionsPage = questionServiceHelper.getFaqForTag(TAG, PAGE);
    assertNotNull(questionsPage);
    assertQuestionsEquals(expectedQuestions, questionsPage.items);
  }

  @Test
  public void getThrowsServerException() {
    expectedException.expect(ServerException.class);
    expectedException.expect(new ServerExceptionMatcher(new ServerException(404, "Not found", null)));

    final String TAG = "tag";
    final int PAGE = 1;

    HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
    expectedQueryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
    expectedQueryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

    String expectedRestEndpoint = StringConstants.TAGS + "/" + TAG + "/faq";

    when(
        httpHelper.executeHttpGet(StackUri.STACKX_API_HOST, expectedRestEndpoint, expectedQueryParams,
            SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR, AbstractBaseServiceHelper.JSON_PARSER)).thenThrow(
        new ServerException(404, "Not found", null));

    questionServiceHelper.getFaqForTag(TAG, PAGE);

  }
}

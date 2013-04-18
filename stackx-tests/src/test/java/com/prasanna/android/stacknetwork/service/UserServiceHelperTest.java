package com.prasanna.android.stacknetwork.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.prasanna.android.http.SecureHttpHelper;
import com.prasanna.android.json.JsonUtil;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceHelperTest extends AbstractBaseServiceHelperTest
{
    private class UserServiceHelperStub extends UserServiceHelper
    {
        @Override
        protected SecureHttpHelper getHttpHelper()
        {
            return httpHelper;
        }
    }

    private UserServiceHelperStub userServiceHelper;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(UserServiceHelperTest.class);
        userServiceHelper = new UserServiceHelperStub();
        setDefaultSite("Stack Overflow", "stackoverflow");
    }

    @Test
    public void getAllSitesInNetwork() throws JSONException
    {
        ArrayList<Site> expectedSites = getSites(5);

        HashMap<String, String> queryParams = getMinimumExpectedQueryParams();
        queryParams.remove(StackUri.QueryParams.SITE);
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(100));
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(1));

        JSONObjectWrapper jsonObjectWrapper = JsonUtil.sitesToJsonObjectWrapper(expectedSites);
        mockRestCall(StringConstants.SITES, queryParams, jsonObjectWrapper);

        LinkedHashMap<String, Site> allSitesInNetwork = userServiceHelper.getAllSitesInNetwork();
        assertNotNull(allSitesInNetwork);
        assertSitesAreReturnedInOrder(expectedSites, allSitesInNetwork);
    }

    private void assertSitesAreReturnedInOrder(ArrayList<Site> expectedSites,
                    LinkedHashMap<String, Site> allSitesInNetwork)
    {
        assertEquals(expectedSites.size(), allSitesInNetwork.size());
        Iterator<String> iter = allSitesInNetwork.keySet().iterator();
        int count = 0;
        while (iter.hasNext())
            assertSiteEquals(expectedSites.get(count++), allSitesInNetwork.get(iter.next()));
    }

    @Test
    public void getMe() throws JSONException
    {
        assertUSerProfilePage(mockCallAndGetExpctedUser("stackoverflow", true),
                        userServiceHelper.getMe("stackoverflow"));
    }

    @Test
    public void getUser() throws JSONException
    {
        User expectedUser = mockCallAndGetExpctedUser("stackoverflow", false);
        assertUSerProfilePage(expectedUser, userServiceHelper.getUserById(expectedUser.id, "stackoverflow"));
    }

    private User mockCallAndGetExpctedUser(final String site, final boolean me) throws JSONException
    {
        User expectedUser = getDetailedUser();
        JSONObjectWrapper jsonObjectWrapper = JsonUtil.usertoJsonObjectWrapper(expectedUser);
        HashMap<String, String> queryParams = getMinimumExpectedQueryParams(site);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);
        String expectedUri = me ? "/me" : "/users/" + expectedUser.id;
        mockRestCall(expectedUri, queryParams, jsonObjectWrapper);
        return expectedUser;
    }

    private void assertUSerProfilePage(User expectedUser, StackXPage<User> me)
    {
        assertNotNull(me);
        assertNotNull(me.items);
        assertTrue(me.items.size() == 1);
        assertDetailUserEquals(expectedUser, me.items.get(0));
    }

    @Test
    public void getQuestionsByUser() throws JSONException
    {
        final long USER_ID = 100L;
        final int PAGE = 1;
        ArrayList<Question> expectedQuestions = getQuestions(10);
        JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);
        HashMap<String, String> queryParams = getMinimumExpectedQueryParams();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

        mockRestCall("/users/" + USER_ID + "/questions", queryParams, jsonObjectWrapper);
        StackXPage<Question> questionsPage = userServiceHelper.getQuestionsByUser(USER_ID, PAGE);
        assertNotNull(questionsPage);
        assertQuestionsEquals(expectedQuestions, questionsPage.items);
    }

    @Test
    public void getAnswersByUser() throws JSONException
    {
        final long USER_ID = 100L;
        final int PAGE = 1;
        ArrayList<Answer> expectedAnswers = getAnswers(5, true);
        JSONObjectWrapper jsonObjectWrapper = JsonUtil.answersToJsonObjectWrapper(expectedAnswers);
        HashMap<String, String> queryParams = getMinimumExpectedQueryParams();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);

        mockRestCall("/users/" + USER_ID + "/answers", queryParams, jsonObjectWrapper);
        StackXPage<Answer> answersPage = userServiceHelper.getAnswersByUser(USER_ID, PAGE);
        assertNotNull(answersPage);
        assertAnswersEquals(expectedAnswers, answersPage.items);
    }

    @Test
    public void getFavoritesByUser() throws JSONException
    {
        final long USER_ID = 100L;
        final int PAGE = 1;
        ArrayList<Question> expectedQuestions = getQuestions(10);
        JSONObjectWrapper jsonObjectWrapper = JsonUtil.questionsToJsonObjectWrapper(expectedQuestions);
        HashMap<String, String> queryParams = getMinimumExpectedQueryParams();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

        mockRestCall("/users/" + USER_ID + "/favorites", queryParams, jsonObjectWrapper);
        StackXPage<Question> questionsPage = userServiceHelper.getFavoritesByUser(USER_ID, PAGE);
        assertNotNull(questionsPage);
        assertQuestionsEquals(expectedQuestions, questionsPage.items);
    }

    @Test
    public void getAccounts() throws JSONException
    {
        final long USER_ID = 100L;
        final int PAGE = 1;

        ArrayList<Account> expectedAccounts = getAccounts(10);
        JSONObjectWrapper jsonObjectWrapper = JsonUtil.accountsToJsonObjectWrapper(expectedAccounts);
        HashMap<String, String> queryParams = getMinimumExpectedQueryParams();
        queryParams.remove(StackUri.QueryParams.SITE);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(PAGE));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(100));

        mockRestCall("/users/" + USER_ID + "/associated", queryParams, jsonObjectWrapper);
        HashMap<String, Account> accounts = userServiceHelper.getAccount(USER_ID);
        assertNotNull(accounts);
        assertEquals(expectedAccounts.size(), accounts.size());
        for (Account expectedAccount : expectedAccounts)
            assertAccountEquals(expectedAccount, accounts.get(expectedAccount.siteUrl));
    }

    @Test
    public void logoutSuccess() throws JSONException
    {
        final String ACCESS_TOKEN = "access_token";

        mockRestCall("/apps/" + ACCESS_TOKEN + "/de-authenticate", null,
                        JsonUtil.toJsonObjectWrapper(ACCESS_TOKEN, 100L));
        StackExchangeHttpError stackExchangeHttpError = userServiceHelper.logout(ACCESS_TOKEN);
        assertNotNull(stackExchangeHttpError);
        assertEquals(-1, stackExchangeHttpError.id);
    }

    @Test
    public void logoutFail() throws JSONException
    {
        final String ACCESS_TOKEN = "access_token";

        StackExchangeHttpError expectedError = new StackExchangeHttpError();
        expectedError.id = 400;
        expectedError.name = "bad_request";
        expectedError.message = "biatch!";

        JSONObjectWrapper jsonObjectWrapper = JsonUtil.toJsonObjectWrapper(expectedError);
        mockRestCall("/apps/" + ACCESS_TOKEN + "/de-authenticate", null, jsonObjectWrapper);
        StackExchangeHttpError stackExchangeHttpError = userServiceHelper.logout(ACCESS_TOKEN);
        assertNotNull(stackExchangeHttpError);
        assertEquals(expectedError.id, stackExchangeHttpError.id);
        assertEquals(expectedError.name, stackExchangeHttpError.name);
        assertEquals(expectedError.message, stackExchangeHttpError.message);
    }

}

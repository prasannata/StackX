package com.prasanna.android.stacknetwork.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import android.content.Context;

import com.prasanna.android.http.SecureHttpHelper;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.QueryParamDefaultValues;

public abstract class AbstractBaseServiceHelperTest
{
    @Mock
    protected SecureHttpHelper httpHelper;

    @Mock
    protected Context context;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected Site site;

    protected void setDefaultSite(String name, String apiSiteParameter)
    {
        site = new Site();
        site.apiSiteParameter = "stackoverflow";
        site.name = "Stack Overflow";
        OperatingSite.setSite(site);
    }

    protected void setupForAuthUser()
    {
        AppUtils.setAccessToken(context, "accessToken");
    }

    protected HashMap<String, String> getMinimumExpectedQueryParams()
    {
        HashMap<String, String> expectedQueryParams = new HashMap<String, String>();
        expectedQueryParams.put(StackUri.QueryParams.SITE, site.apiSiteParameter);
        expectedQueryParams.put(StackUri.QueryParams.CLIENT_ID, StackUri.QueryParamDefaultValues.CLIENT_ID);
        return expectedQueryParams;
    }

    protected HashMap<String, String> getMinimumExpectedQueryParamsForAuthUser()
    {
        HashMap<String, String> expectedQueryParams = getMinimumExpectedQueryParams();
        expectedQueryParams.put(StackUri.QueryParams.ACCESS_TOKEN, "accessToken");
        expectedQueryParams.put(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY);

        return expectedQueryParams;
    }

    protected List<BasicNameValuePair> getBasicNameValuePartListForWriteComment()
    {
        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.ACCESS_TOKEN, AppUtils.getAccessToken(null)));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.CLIENT_ID, QueryParamDefaultValues.CLIENT_ID));
        parameters.add(new BasicNameValuePair(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter));
        return parameters;
    }

    protected ArrayList<Site> getSites(int num)
    {
        ArrayList<Site> sites = new ArrayList<Site>();
        for (int i = 0; i < num; i++)
            sites.add(getSite(i));
        return sites;
    }

    private Site getSite(int i)
    {
        Site site = new Site();
        site.apiSiteParameter = "apiSiteParamter" + i;
        site.name = "name" + i;
        site.audience = "audience" + i;
        site.iconUrl = "iconUrl" + i;
        site.logoUrl = "logoUrl" + i;
        site.faviconUrl = "faviconUrl" + i;
        site.link = "link" + i;
        return site;
    }

    protected ArrayList<Account> getAccounts(int num)
    {
        ArrayList<Account> accounts = new ArrayList<Account>();
        for (int i = 0; i < num; i++)
            accounts.add(getAccount(i));
        return accounts;
    }

    private Account getAccount(int i)
    {
        Account account = new Account();
        account.id = i;
        account.userId = 100 + i;
        account.siteName = "siteName" + i;
        account.siteUrl = "http://siteUrl/" + i;
        account.userType = UserType.values()[i % UserType.values().length];
        return account;
    }

    protected ArrayList<Question> getQuestions(int num)
    {
        ArrayList<Question> questions = new ArrayList<Question>();
        for (int i = 0; i < num; i++)
            questions.add(getQuestion(false, i % 2 == 0));
        return questions;
    }

    protected Question getQuestion(boolean body, boolean acceptedAnswer)
    {
        Question question = new Question();
        question.id = System.currentTimeMillis();
        question.title = "question title " + question.id;
        question.answerCount = 0;
        question.hasAcceptedAnswer = acceptedAnswer;
        if (body)
            question.body = "body";
        question.creationDate = System.currentTimeMillis();
        question.link = "http://link/1";
        question.score = 1;
        question.tags = new String[] { "tag1", "tag2" };
        question.answered = false;
        question.viewCount = 10;
        question.votes = 2;
        question.owner = getShallowUser();
        return question;
    }

    protected ArrayList<Answer> getAnswers(int num, boolean body)
    {
        ArrayList<Answer> answers = new ArrayList<Answer>();
        for (int i = 0; i < num; i++)
            answers.add(getAnswer(i, body, i % 3 == 0));
        return answers;
    }

    protected Answer getAnswer(long questionId, boolean body, boolean isAccepted)
    {
        Answer answer = new Answer();
        answer.id = System.currentTimeMillis();
        if (body)
            answer.body = "body" + answer.id;
        answer.title = "question title " + System.currentTimeMillis();
        answer.creationDate = System.currentTimeMillis();
        answer.questionId = questionId;
        answer.score = 1;
        answer.owner = getShallowUser();
        return answer;
    }

    protected ArrayList<Comment> getComments(long postId)
    {
        ArrayList<Comment> comments = new ArrayList<Comment>();
        comments.add(getComment(postId, 1));
        comments.add(getComment(postId, 2));
        return comments;
    }

    protected Comment getComment(long postId, long id)
    {
        Comment comment = new Comment();
        comment.id = id;
        comment.body = "body" + id;
        comment.creationDate = System.currentTimeMillis();
        comment.post_id = postId;
        comment.score = 1;
        comment.owner = getShallowUser();
        return comment;
    }

    protected User getShallowUser()
    {
        User user = new User();
        user.id = System.currentTimeMillis();
        user.displayName = "fyodor-" + user.id;
        user.acceptRate = (int) (user.id % 51);
        user.reputation = (int) (user.id % 101);
        user.profileImageLink = "http://profile/" + user.id + "/image.png";
        return user;
    }

    protected void assertSiteEquals(Site expectedSite, Site site)
    {
        assertNotNull(site);
        assertEquals(expectedSite.name, site.name);
        assertEquals(expectedSite.apiSiteParameter, site.apiSiteParameter);
        assertEquals(expectedSite.audience, site.audience);
        assertEquals(expectedSite.link, site.link);
        assertEquals(expectedSite.iconUrl, site.iconUrl);
        assertEquals(expectedSite.logoUrl, site.logoUrl);
        assertEquals(expectedSite.faviconUrl, site.faviconUrl);
    }

    protected void assertAccountEquals(Account expectedAccount, Account account)
    {
        assertNotNull(site);
        assertEquals(expectedAccount.id, account.id);
        assertEquals(expectedAccount.siteName, account.siteName);
        assertEquals(expectedAccount.siteUrl, account.siteUrl);
        assertEquals(expectedAccount.userId, account.userId);
        assertEquals(expectedAccount.userId, account.userId);
        assertEquals(expectedAccount.userType, account.userType);
    }

    protected void assertQuestionsEquals(ArrayList<Question> expectedQuestions, ArrayList<Question> questions)
    {
        assertNotNull(questions);
        assertEquals(expectedQuestions.size(), questions.size());
        for (int i = 0; i < questions.size(); i++)
            assertQuestionEquals(expectedQuestions.get(0), questions.get(0));
    }

    protected void assertQuestionEquals(Question expectedQuestion, Question question)
    {
        assertNotNull(question);
        assertEquals(expectedQuestion.id, question.id);
        assertEquals(expectedQuestion.title, question.title);
        assertEquals(expectedQuestion.answerCount, question.answerCount);
        assertEquals(expectedQuestion.answered, question.answered);
        assertEquals(expectedQuestion.hasAcceptedAnswer, question.hasAcceptedAnswer);
        assertEquals(expectedQuestion.score, question.score);
        assertEquals(expectedQuestion.viewCount, question.viewCount);
        assertEquals(expectedQuestion.link, question.link);
        assertEquals(expectedQuestion.creationDate, question.creationDate);
        assertArrayEquals(expectedQuestion.tags, question.tags);
        assertShallowUserEquals(expectedQuestion.owner, question.owner);
    }

    protected void assertAnswersEquals(ArrayList<Answer> expectedAnswers, ArrayList<Answer> answers)
    {
        assertNotNull(answers);
        assertEquals(expectedAnswers.size(), answers.size());
        for (int i = 0; i < answers.size(); i++)
            assertAnswerEquals(expectedAnswers.get(0), answers.get(0));
    }

    protected void assertAnswerEquals(Answer expectedAnswer, Answer answer)
    {
        assertNotNull(answer);
        assertEquals(expectedAnswer.id, answer.id);
        assertEquals(expectedAnswer.title, answer.title);
        assertEquals(expectedAnswer.body, answer.body);
        assertEquals(expectedAnswer.score, answer.score);
        assertEquals(expectedAnswer.accepted, answer.accepted);
        assertEquals(expectedAnswer.creationDate, answer.creationDate);
        assertShallowUserEquals(expectedAnswer.owner, answer.owner);
    }

    protected void assertCommentsEquals(ArrayList<Comment> expectedComments, ArrayList<Comment> comments)
    {
        assertNotNull(comments);
        assertEquals(expectedComments.size(), comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertCommentEquals(expectedComments.get(0), comments.get(0));
    }

    protected void assertCommentEquals(Comment expectedComment, Comment comment)
    {
        assertEquals(expectedComment.id, comment.id);
        assertEquals(expectedComment.post_id, comment.post_id);
        assertEquals(expectedComment.body, comment.body);
        assertEquals(expectedComment.score, comment.score);
        assertEquals(expectedComment.creationDate, comment.creationDate);
        assertShallowUserEquals(expectedComment.owner, comment.owner);
    }

    protected void assertShallowUserEquals(User expectedOwner, User owner)
    {
        assertNotNull(owner);
        assertEquals(expectedOwner.id, owner.id);
        assertEquals(expectedOwner.displayName, owner.displayName);
        assertEquals(expectedOwner.reputation, owner.reputation);
        assertEquals(expectedOwner.acceptRate, owner.acceptRate);
    }

    protected void mockRestCall(String expectedRestEndpoint, HashMap<String, String> expectedQueryParams,
                    JSONObjectWrapper returnJsonObjectWrapper)
    {
        when(
                        httpHelper.executeHttpGet(StackUri.STACKX_API_HOST, expectedRestEndpoint, expectedQueryParams,
                                        SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR)).thenReturn(
                        returnJsonObjectWrapper);
    }

}

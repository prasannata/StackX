package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.http.HttpHelper;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;

public class UserService extends AbstractBaseService
{
    private final String TAG = UserService.class.getSimpleName();

    private static final UserService userService = new UserService();

    public static UserService getInstance()
    {
	return userService;
    }

    public ArrayList<Site> getAllSitesForUnauthorizedUser()
    {
	ArrayList<Site> sites = new ArrayList<Site>();

	JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding("sites", null);

	try
	{
	    if (jsonObject != null)
	    {
		JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);

		if (jsonArray != null)
		{
		    for (int i = 0; i < jsonArray.length(); i++)
		    {
			JSONObject siteJsonObject = jsonArray.getJSONObject(i);
			Site site = getSerializedSiteObject(siteJsonObject);
			if (site != null)
			{
			    sites.add(site);
			}
		    }
		}
	    }

	}
	catch (JSONException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return sites;
    }

    private Site getSerializedSiteObject(JSONObject siteJsonObject)
    {
	Site site = null;

	try
	{
	    site = new Site();
	    site.apiSiteParameter = siteJsonObject.getString(JsonFields.Site.API_SITE_PARAMETER);
	    site.logoUrl = siteJsonObject.getString(JsonFields.Site.LOGO_URL);
	    site.name = siteJsonObject.getString(JsonFields.Site.NAME);
	    site.link = siteJsonObject.getString(JsonFields.Site.SITE_URL);
	}
	catch (JSONException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return site;
    }

    public void getSitesForUser(long userId)
    {

    }

    public ArrayList<Question> getQuestionsByUser(long userId, int page)
    {
	String restEndPoint = "/users/" + userId + "/questions";

	ArrayList<Question> questions = new ArrayList<Question>();

	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, "25");

	JSONObjectWrapper questionsJsonResponse = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
	                restEndPoint, queryParams);

	if (questionsJsonResponse != null)
	{
	    questions = getQuestionModel(questionsJsonResponse);
	}

	return questions;
    }

    public void getTagsForUser(long userId)
    {

    }

    public User getUserById(long userId)
    {
	User user = null;
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);

	if (userId != -1)
	{
	    JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
		            "/users/" + userId, queryParams);
	    JSONObjectWrapper userJsonObject = jsonObject.getObjectFromArray(JsonFields.ITEMS, 0);
	    user = getSerializedUserObject(userJsonObject);
	}

	return user;
    }

    public ArrayList<Question> getAllQuestions(int page)
    {
	ArrayList<Question> questions = new ArrayList<Question>();

	String restEndPoint = "questions";
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);

	JSONObjectWrapper questionsJsonResponse = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
	                restEndPoint, queryParams);
	if (questionsJsonResponse != null)
	{
	    questions = getQuestionModel(questionsJsonResponse);

	}

	return questions;
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    public ArrayList<Answer> getAnswersByUser(long userId, int page)
    {
	String restEndPoint = "/users/" + userId + "/answers";

	ArrayList<Answer> answers = new ArrayList<Answer>();

	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.QUESTION_DETAIL_FILTER);

	JSONObjectWrapper answersJsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(restEndPoint,
	                queryParams);

	if (answersJsonObject != null)
	{
	    JSONArray jsonArray = answersJsonObject.getJSONArray(JsonFields.ITEMS);
	    if (jsonArray != null)
	    {
		try
		{
		    for (int i = 0; i < jsonArray.length(); i++)
		    {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			answers.add(getSerializedAnswerObject(jsonObject));
		    }
		}
		catch (JSONException e)
		{
		    Log.d(getLogTag(), e.getMessage());
		}
	    }
	}

	return answers;
    }
}

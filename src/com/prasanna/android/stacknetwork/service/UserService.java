package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.http.HttpHelper;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUriQueryParams;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserService
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

	JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
	        "sites", null);

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
	    site.setApiSiteParameter(siteJsonObject.getString(JsonFields.Site.API_SITE_PARAMETER));
	    site.setLogoUrl(siteJsonObject.getString(JsonFields.Site.LOGO_URL));
	    site.setName(siteJsonObject.getString(JsonFields.Site.NAME));
	    site.setLink(siteJsonObject.getString(JsonFields.Site.SITE_URL));
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
	queryParams.put(StackUriQueryParams.ORDER, "desc");
	queryParams.put(StackUriQueryParams.SORT, "activity");
	queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().getApiSiteParameter());
	queryParams.put(StackUriQueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUriQueryParams.PAGE_SIZE, "25");

	JSONObjectWrapper questionsJsonResponse = HttpHelper.getInstance()
	        .getRequestForJsonWithGzipEncoding(restEndPoint, queryParams);

	if (questionsJsonResponse != null)
	{
	    questions = getQuestionModel(restEndPoint, questionsJsonResponse);
	}

	return questions;
    }

    public void getAnswersByUser(long userId)
    {

    }

    public void getTagsForUser(long userId)
    {

    }

    public User getUserById(long userId)
    {
	User user = null;
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().getApiSiteParameter());
	queryParams
	        .put(StackUriQueryParams.FILTER, StringConstants.StackFilters.USER_DETAIL_FILTER);

	if (userId != -1)
	{
	    JSONObjectWrapper jsonObject = HttpHelper.getInstance()
		    .getRequestForJsonWithGzipEncoding("/users/" + userId, queryParams);

	    try
	    {
		JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);
		JSONObject userJsonObject = jsonArray.getJSONObject(0);
		user = getSerializedUserObject(userJsonObject);
	    }
	    catch (JSONException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	return user;
    }

    private User getSerializedUserObject(JSONObject userJsonObject)
    {
	User user = null;
	try
	{

	    user = new User();
	    user.setId(userJsonObject.getLong(JsonFields.User.USER_ID));
	    user.setAccountId(userJsonObject.getLong(JsonFields.User.ACCOUNT_ID));
	    user.setDisplayName(userJsonObject.getString(JsonFields.User.DISPLAY_NAME));
	    user.setReputation(userJsonObject.getInt(JsonFields.User.REPUTATION));
	    user.setProfileImageLink(userJsonObject.getString(JsonFields.User.PROFILE_IMAGE));
	    user.setQuestionCount(userJsonObject.getInt(JsonFields.User.QUESTION_COUNT));
	    user.setAnswerCount(userJsonObject.getInt(JsonFields.User.ANSWER_COUNT));
	    user.setUpvoteCount(userJsonObject.getInt(JsonFields.User.UP_VOTE_COUNT));
	    user.setDownvoteCount(userJsonObject.getInt(JsonFields.User.DOWN_VOTE_COUNT));
	    user.setProfileViews(userJsonObject.getInt(JsonFields.User.VIEW_COUNT));
	    user.setBadgeCounts(getBadgeCounts(userJsonObject));
	    user.setLastAccessTime(userJsonObject.getLong(JsonFields.User.LAST_ACCESS_DATE));
	    user.setAcceptRate(userJsonObject.getInt(JsonFields.User.ACCEPT_RATE));
	}
	catch (JSONException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return user;
    }

    private int[] getBadgeCounts(JSONObject userJsonObject) throws JSONException
    {
	int[] badgeCounts = new int[3];
	badgeCounts[0] = 0;
	badgeCounts[1] = 0;
	badgeCounts[2] = 0;
	JSONObject badgeCountJsonObject = userJsonObject
	        .getJSONObject(JsonFields.User.BADGE_COUNTS);
	badgeCounts[0] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.GOLD);
	badgeCounts[1] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.SILVER);
	badgeCounts[2] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.BRONZE);

	return badgeCounts;
    }

    public ArrayList<Question> getAllQuestions(int page)
    {
	ArrayList<Question> questions = new ArrayList<Question>();

	String restEndPoint = "questions";
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUriQueryParams.ORDER, "desc");
	queryParams.put(StackUriQueryParams.SORT, "activity");
	queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().getApiSiteParameter());
	queryParams.put(StackUriQueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUriQueryParams.PAGE_SIZE, "25");

	JSONObjectWrapper questionsJsonResponse = HttpHelper.getInstance()
	        .getRequestForJsonWithGzipEncoding(restEndPoint, queryParams);
	if (questionsJsonResponse != null)
	{
	    questions = getQuestionModel(restEndPoint, questionsJsonResponse);

	}

	return questions;
    }

    private ArrayList<Question> getQuestionModel(String restEndPoint,
	    JSONObjectWrapper questionsJsonResponse)
    {
	ArrayList<Question> questions = new ArrayList<Question>();
	JSONArray jsonArray = questionsJsonResponse.getJSONArray(JsonFields.ITEMS);
	if (jsonArray != null)
	{
	    try
	    {
		for (int i = 0; i < jsonArray.length(); i++)
		{
		    JSONObject jsonObject = jsonArray.getJSONObject(i);
		    questions.add(getSerializedQuestionObject(jsonObject));
		}
	    }
	    catch (JSONException e)
	    {
		Log.d(TAG, e.getMessage());
	    }
	}
	return questions;
    }

    private Question getSerializedQuestionObject(JSONObject jsonObject) throws JSONException
    {
	Question question = new Question();

	question.setTitle(jsonObject.getString(JsonFields.Question.TITLE));
	question.setId(jsonObject.getLong(JsonFields.Question.QUESTION_ID));
	question.setAnswered(jsonObject.getBoolean(JsonFields.Question.IS_ANSWERED));
	question.setScore(jsonObject.getInt(JsonFields.Question.SCORE));
	question.setAnswerCount(jsonObject.getInt(JsonFields.Question.ANSWER_COUNT));
	question.setViewCount(jsonObject.getInt(JsonFields.Question.VIEW_COUNT));
	question.setTags(getTags(jsonObject));
	question.setOwner(getOwner(jsonObject));
	question.setCreationDate(jsonObject.getLong(JsonFields.Question.CREATION_DATE));

	if (jsonObject.has(JsonFields.Question.ACCEPTED_ANSWER_ID))
	{
	    question.setHasAcceptedAnswer(true);
	}
	return question;
    }

    private User getOwner(JSONObject jsonObject)
    {
	User user = null;
	JSONObject owner = null;
	try
	{
	    owner = jsonObject.getJSONObject(JsonFields.Question.OWNER);
	    if (owner != null)
	    {
		user = new User();
		user.setId(owner.getLong(JsonFields.User.USER_ID));
		user.setDisplayName(owner.getString(JsonFields.User.DISPLAY_NAME));
		user.setReputation(owner.getInt(JsonFields.User.REPUTATION));
		user.setProfileImageLink(owner.getString(JsonFields.User.PROFILE_IMAGE));
		user.setAcceptRate(owner.getInt(JsonFields.User.ACCEPT_RATE));
	    }
	}
	catch (JSONException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return user;
    }

    private String[] getTags(JSONObject jsonObject) throws JSONException
    {
	String[] tags = null;

	JSONArray tagsJsonArray = jsonObject.getJSONArray(JsonFields.Question.TAGS);
	if (tagsJsonArray != null)
	{
	    tags = new String[tagsJsonArray.length()];

	    for (int i = 0; i < tags.length; i++)
	    {
		tags[i] = tagsJsonArray.getString(i);
	    }
	}
	return tags;
    }
}

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
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUriQueryParams;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionService
{
    private static final String TAG = QuestionService.class.getSimpleName();

    private static final QuestionService questionService = new QuestionService();

    public static QuestionService getInstance()
    {
	return questionService;
    }

    public ArrayList<Answer> getAnswersForQuestion(long id)
    {
	ArrayList<Answer> answers = new ArrayList<Answer>();
	String restEndPoint = "/questions/" + id + "/answers";
	Map<String, String> queryParams = getDefaultQueryParams();
	JSONObjectWrapper answersJson = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
	        restEndPoint, queryParams);
	if (answersJson != null)
	{
	    try
	    {
		JSONArray jsonArray = answersJson.getJSONArray(JsonFields.ITEMS);

		for (int i = 0; i < jsonArray.length(); i++)
		{
		    Answer answer = new Answer();
		    JSONObject jsonObject = jsonArray.getJSONObject(i);
		    answer.setId(jsonObject.getLong(JsonFields.Answer.ANSWER_ID));
		    answer.setQuestionId(jsonObject.getLong(JsonFields.Answer.QUESTION_ID));
		    answer.setBody(jsonObject.getString(JsonFields.Answer.BODY));
		    answer.setScore(jsonObject.getInt(JsonFields.Answer.SCORE));
		    answer.setCreationDate(jsonObject.getLong(JsonFields.Answer.CREATION_DATE));
		    answer.setAccepted(jsonObject.getBoolean(JsonFields.Answer.IS_ACCEPTED));

		    JSONObject userJsonObject = jsonObject.getJSONObject(JsonFields.Answer.OWNER);
		    answer.setOwner(getSerializableUserObject(userJsonObject));
		    answers.add(answer);
		}
	    }
	    catch (JSONException e)
	    {
		Log.d(TAG, e.getMessage());
	    }
	}
	return answers;
    }

    private User getSerializableUserObject(JSONObject userJsonObject) throws JSONException
    {
	User user = new User();
	user.setId(userJsonObject.getLong(JsonFields.User.USER_ID));
	user.setDisplayName(userJsonObject.getString(JsonFields.User.DISPLAY_NAME));
	user.setReputation(userJsonObject.getInt(JsonFields.User.REPUTATION));
	return user;
    }

    public String getQuestionBodyForId(long id)
    {
	String restEndPoint = "/questions/" + id;
	String questionBody = null;
	Map<String, String> queryParams = getDefaultQueryParams();
	JSONObjectWrapper questionJsonResponse = HttpHelper.getInstance()
	        .getRequestForJsonWithGzipEncoding(restEndPoint, queryParams);
	if (questionJsonResponse != null)
	{
	    try
	    {
		JSONArray jsonArray = questionJsonResponse.getJSONArray(JsonFields.ITEMS);

		if (jsonArray != null && jsonArray.length() == 1)
		{
		    JSONObject jsonObject = jsonArray.getJSONObject(0);
		    questionBody = jsonObject.getString(JsonFields.Question.BODY);
		}
	    }
	    catch (JSONException e)
	    {
		Log.d(TAG, e.getMessage());
	    }
	}

	return questionBody;
    }

    public ArrayList<Comment> getCommentsForQuestion(long id)
    {
	ArrayList<Comment> comments = null;
	String restEndPoint = "/questions/" + id + "/comments";
	Map<String, String> queryParams = getDefaultQueryParams();
	JSONObjectWrapper commentsJsonResponse = HttpHelper.getInstance()
	        .getRequestForJsonWithGzipEncoding(restEndPoint, queryParams);

	if (commentsJsonResponse != null)
	{
	    try
	    {
		JSONArray jsonArray = commentsJsonResponse.getJSONArray(JsonFields.ITEMS);

		for (int count = 0; count < jsonArray.length(); count++)
		{
		    Comment comment = new Comment();
		    JSONObject jsonObject = jsonArray.getJSONObject(count);
		    comment.setId(jsonObject.getLong(JsonFields.Comment.COMMENT_ID));
		    comment.setBody(jsonObject.getString(JsonFields.Comment.BODY));
		    comment.setCreationDate(jsonObject.getLong(JsonFields.Comment.CREATION_DATE));
		    comment.setScore(jsonObject.getInt(JsonFields.Comment.SCORE));
		    JSONObject userJsonObject = jsonObject.getJSONObject(JsonFields.Comment.OWNER);
		    comment.setOwner(getSerializableUserObject(userJsonObject));

		    if (comments == null)
		    {
			comments = new ArrayList<Comment>();
		    }

		    comments.add(comment);
		}
	    }
	    catch (JSONException e)
	    {
		Log.d(TAG, e.getMessage());
	    }
	}
	return comments;
    }

    private Map<String, String> getDefaultQueryParams()
    {
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().getApiSiteParameter());
	queryParams.put(StackUriQueryParams.FILTER,
	        StringConstants.StackFilters.QUESTION_DETAIL_FILTER);
	return queryParams;
    }
}

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

package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;

public class QuestionServiceHelper extends AbstractBaseServiceHelper
{
    private static final String TAG = QuestionServiceHelper.class.getSimpleName();

    private static final QuestionServiceHelper questionService = new QuestionServiceHelper();

    public static QuestionServiceHelper getInstance()
    {
	return questionService;
    }

    public ArrayList<Answer> getAnswersForQuestion(long id, int page)
    {
	ArrayList<Answer> answers = new ArrayList<Answer>();
	String restEndPoint = "questions/" + id + "/answers";
	Map<String, String> queryParams = getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE,
	                String.valueOf(StackUri.QueryParamDefaultValues.ANSWERS_PAGE_SIZE));
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.VOTES);

	JSONObjectWrapper answersJson = executeHttpRequest(restEndPoint, queryParams);
	if (answersJson != null)
	{
	    try
	    {
		JSONArray jsonArray = answersJson.getJSONArray(JsonFields.ITEMS);

		if (jsonArray != null)
		{
		    for (int i = 0; i < jsonArray.length(); i++)
		    {
			JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
			answers.add(getSerializedAnswerObject(jsonObject));
		    }
		}
	    }
	    catch (JSONException e)
	    {
		Log.d(TAG, e.getMessage());
	    }
	}
	return answers;
    }

    public String getQuestionBodyForId(long id)
    {
	String restEndPoint = "questions/" + id;
	String questionBody = null;
	Map<String, String> queryParams = getDefaultQueryParams();
	JSONObjectWrapper questionJsonResponse = executeHttpRequest(restEndPoint, queryParams);
	if (questionJsonResponse != null)
	{
	    try
	    {
		JSONArray jsonArray = questionJsonResponse.getJSONArray(JsonFields.ITEMS);

		if (jsonArray != null && jsonArray.length() == 1)
		{
		    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));
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

    public ArrayList<Comment> getComments(String parent, long parentId)
    {
	ArrayList<Comment> comments = null;
	String restEndPoint = parent + "/" + parentId + "/comments";
	Map<String, String> queryParams = getDefaultQueryParams();
	JSONObjectWrapper commentsJsonResponse = executeHttpRequest(restEndPoint, queryParams);

	if (commentsJsonResponse != null)
	{
	    try
	    {
		JSONArray jsonArray = commentsJsonResponse.getJSONArray(JsonFields.ITEMS);

		for (int count = 0; count < jsonArray.length(); count++)
		{
		    Comment comment = new Comment();
		    JSONObject jsonObject = jsonArray.getJSONObject(count);
		    comment.id = jsonObject.getLong(JsonFields.Comment.COMMENT_ID);
		    comment.body = jsonObject.getString(JsonFields.Comment.BODY);
		    comment.creationDate = jsonObject.getLong(JsonFields.Comment.CREATION_DATE);
		    comment.score = jsonObject.getInt(JsonFields.Comment.SCORE);
		    comment.owner = getSerializableUserSnippetObject(JSONObjectWrapper.wrap(jsonObject
			            .getJSONObject(JsonFields.Comment.OWNER)));

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
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.QUESTION_DETAIL_FILTER);
	return queryParams;
    }

    public ArrayList<Question> search(String query, int page)
    {
	String restEndPoint = "search";
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.IN_TITLE, query);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	JSONObjectWrapper questionsJsonResponse = executeHttpRequest(restEndPoint, queryParams);

	return getQuestionModel(questionsJsonResponse);
    }

    @Override
    protected String getLogTag()
    {
	return TAG;
    }

    public Question getQuestionFullDetails(long id)
    {
	String restEndPoint = "questions/" + id;
	Question question = null;
	Map<String, String> queryParams = getDefaultQueryParams();
	JSONObjectWrapper questionJsonResponse = executeHttpRequest(restEndPoint, queryParams);
	if (questionJsonResponse != null)
	{
	    try
	    {
		JSONArray jsonArray = questionJsonResponse.getJSONArray(JsonFields.ITEMS);

		if (jsonArray != null && jsonArray.length() == 1)
		{
		    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));
		    question = getSerializedQuestionObject(jsonObject);
		    question.body = jsonObject.getString(JsonFields.Question.BODY);
		}
	    }
	    catch (JSONException e)
	    {
		Log.d(TAG, e.getMessage());
	    }
	}

	return question;
    }

    public ArrayList<Question> getAllQuestions(int page)
    {
	ArrayList<Question> questions = null;

	String restEndPoint = "questions";
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	JSONObjectWrapper questionsJsonResponse = executeHttpRequest(restEndPoint, queryParams);
	if (questionsJsonResponse != null)
	{
	    questions = getQuestionModel(questionsJsonResponse);
	}

	return questions;
    }
    public ArrayList<Question> getRelatedQuestions(long questionId, int page)
    {
	ArrayList<Question> questions = null;

	String restEndPoint = "questions/" + questionId + "/related";
	Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
	queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
	queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
	queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	JSONObjectWrapper questionsJsonResponse = executeHttpRequest(restEndPoint, queryParams);
	if (questionsJsonResponse != null)
	{
	    questions = getQuestionModel(questionsJsonResponse);
	}

	return questions;
    }

    public ArrayList<Question> getFaqForTag(String tag, int page)
    {
	ArrayList<Question> questions = null;

	if (tag != null)
	{
	    String restEndPoint = "tags/" + tag + "/faq";
	    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();

	    queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
	    queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
	    queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

	    JSONObjectWrapper questionsJsonResponse = executeHttpRequest(restEndPoint, queryParams);

	    if (questionsJsonResponse != null)
	    {
		questions = getQuestionModel(questionsJsonResponse);
	    }
	}

	return questions;
    }
}

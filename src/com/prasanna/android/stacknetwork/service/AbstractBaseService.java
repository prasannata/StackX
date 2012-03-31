package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public abstract class AbstractBaseService
{
    protected abstract String getLogTag();

    protected User getSerializedUserObject(JSONObjectWrapper userJsonObject)
    {
	User user = null;

	user = new User();
	user.id = userJsonObject.getLong(JsonFields.User.USER_ID);
	user.accountId = userJsonObject.getLong(JsonFields.User.ACCOUNT_ID);
	user.displayName = userJsonObject.getString(JsonFields.User.DISPLAY_NAME);
	user.reputation = userJsonObject.getInt(JsonFields.User.REPUTATION);
	user.profileImageLink = userJsonObject.getString(JsonFields.User.PROFILE_IMAGE);
	user.questionCount = userJsonObject.getInt(JsonFields.User.QUESTION_COUNT);
	user.answerCount = userJsonObject.getInt(JsonFields.User.ANSWER_COUNT);
	user.upvoteCount = userJsonObject.getInt(JsonFields.User.UP_VOTE_COUNT);
	user.downvoteCount = userJsonObject.getInt(JsonFields.User.DOWN_VOTE_COUNT);
	user.profileViews = userJsonObject.getInt(JsonFields.User.VIEW_COUNT);
	user.badgeCounts = getBadgeCounts(userJsonObject);
	user.lastAccessTime = userJsonObject.getLong(JsonFields.User.LAST_ACCESS_DATE);
	user.acceptRate = userJsonObject.getInt(JsonFields.User.ACCEPT_RATE);
	return user;
    }

    protected int[] getBadgeCounts(JSONObjectWrapper userJsonObject)
    {
	int[] badgeCounts = new int[3];
	badgeCounts[0] = 0;
	badgeCounts[1] = 0;
	badgeCounts[2] = 0;
	JSONObjectWrapper badgeCountJsonObject = userJsonObject.getJSONObject(JsonFields.User.BADGE_COUNTS);
	badgeCounts[0] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.GOLD);
	badgeCounts[1] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.SILVER);
	badgeCounts[2] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.BRONZE);

	return badgeCounts;
    }

    protected ArrayList<Question> getQuestionModel(JSONObjectWrapper questionsJsonResponse)
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
		Log.d(getLogTag(), e.getMessage());
	    }
	}
	return questions;
    }

    protected Question getSerializedQuestionObject(JSONObject jsonObject) throws JSONException
    {
	Question question = new Question();

	question.title = jsonObject.getString(JsonFields.Question.TITLE);
	question.id = jsonObject.getLong(JsonFields.Question.QUESTION_ID);
	question.answered = jsonObject.getBoolean(JsonFields.Question.IS_ANSWERED);
	question.score = jsonObject.getInt(JsonFields.Question.SCORE);
	question.answerCount = jsonObject.getInt(JsonFields.Question.ANSWER_COUNT);
	question.viewCount = jsonObject.getInt(JsonFields.Question.VIEW_COUNT);
	question.tags = getTags(jsonObject);
	question.creationDate = jsonObject.getLong(JsonFields.Question.CREATION_DATE);

	if (jsonObject.has(JsonFields.Question.ACCEPTED_ANSWER_ID))
	{
	    question.hasAcceptedAnswer = true;
	}

	question.owner = getSerializableUserObject(jsonObject.getJSONObject(JsonFields.Question.OWNER));
	Log.d("AbsrtractBaseService", "User is " + question.owner);
	return question;
    }

    protected User getSerializableUserObject(JSONObject userJsonObject)
    {
	User user = null;
	try
	{
	    if (userJsonObject != null)
	    {
		user = new User();
		user.id = userJsonObject.getLong(JsonFields.User.USER_ID);
		user.displayName = userJsonObject.getString(JsonFields.User.DISPLAY_NAME);
		user.reputation = userJsonObject.getInt(JsonFields.User.REPUTATION);
		user.profileImageLink = userJsonObject.getString(JsonFields.User.PROFILE_IMAGE);
		user.acceptRate = userJsonObject.getInt(JsonFields.User.ACCEPT_RATE);
	    }
	}
	catch (JSONException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	Log.d(getLogTag(), "User fetched " + user);
	return user;
    }

    protected Answer getSerializedAnswerObject(JSONObject jsonObject) throws JSONException
    {
	Answer answer = new Answer();
	answer.id = jsonObject.getLong(JsonFields.Answer.ANSWER_ID);
	answer.questionId = jsonObject.getLong(JsonFields.Answer.QUESTION_ID);
	answer.body = jsonObject.getString(JsonFields.Answer.BODY);
	answer.title = jsonObject.getString(JsonFields.Answer.TITLE);
	answer.score = jsonObject.getInt(JsonFields.Answer.SCORE);
	answer.creationDate = jsonObject.getLong(JsonFields.Answer.CREATION_DATE);
	answer.accepted = jsonObject.getBoolean(JsonFields.Answer.IS_ACCEPTED);

	answer.owner = getSerializableUserObject(jsonObject.getJSONObject(JsonFields.Answer.OWNER));
	return answer;
    }

    protected String[] getTags(JSONObject jsonObject) throws JSONException
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

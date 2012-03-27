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
        JSONObjectWrapper answersJson = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(restEndPoint,
                queryParams);
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
                    User user = new User();
                    user.setDisplayName(userJsonObject.getString(JsonFields.User.DISPLAY_NAME));
                    user.setReputation(userJsonObject.getInt(JsonFields.User.REPUTATION));
                    answer.setOwner(user);
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

    public String getQuestionBodyForId(long id)
    {
        String restEndPoint = "/questions/" + id;
        String questionBody = null;
        Map<String, String> queryParams = getDefaultQueryParams();
        JSONObjectWrapper questionJsonResponse = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
                restEndPoint, queryParams);
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

    private Map<String, String> getDefaultQueryParams()
    {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().getApiSiteParameter());
        queryParams.put(StackUriQueryParams.FILTER, StringConstants.StackFilters.QUESTION_DETAIL_FILTER);
        return queryParams;
    }
}

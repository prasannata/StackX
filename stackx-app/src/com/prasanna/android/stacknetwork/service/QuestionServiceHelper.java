/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.Order;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class QuestionServiceHelper extends AbstractBaseServiceHelper
{
    private static final String TAG = QuestionServiceHelper.class.getSimpleName();

    private static final QuestionServiceHelper questionService = new QuestionServiceHelper();

    protected QuestionServiceHelper()
    {
    }

    public static QuestionServiceHelper getInstance()
    {
        return questionService;
    }

    public String getQuestionBodyForId(long id, String site)
    {
        String restEndPoint = "questions/" + id;
        String questionBody = null;
        Map<String, String> queryParams = getDefaultQueryParams();
        if (site != null)
            queryParams.put(StackUri.QueryParams.SITE, site);

        JSONObjectWrapper questionJsonResponse = executeHttpGetRequest(restEndPoint, queryParams);
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
                LogWrapper.d(TAG, e.getMessage());
            }
        }

        return questionBody;
    }

    public String getQuestionBodyForId(long id)
    {
        return getQuestionBodyForId(id, null);
    }

    public ArrayList<Answer> getAnswersForQuestion(long id, String site, int page)
    {
        ArrayList<Answer> answers = new ArrayList<Answer>();
        String restEndPoint = "questions/" + id + "/answers";
        Map<String, String> queryParams = getDefaultQueryParams();
        if (site != null)
            queryParams.put(StackUri.QueryParams.SITE, site);

        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE,
                        String.valueOf(StackUri.QueryParamDefaultValues.ANSWERS_PAGE_SIZE));
        queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.VOTES);

        JSONObjectWrapper answersJson = executeHttpGetRequest(restEndPoint, queryParams);
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
                        Answer answer = getSerializedAnswerObject(jsonObject);
                        if (answer.accepted && answers.size() > 0)
                            answers.add(0, answer);
                        else
                            answers.add(answer);
                    }

                    if (!answers.isEmpty())
                        getCommentsForAnswers(answers);
                }
            }
            catch (JSONException e)
            {
                LogWrapper.d(TAG, e.getMessage());
            }
        }
        return answers;
    }

    private void getCommentsForAnswers(ArrayList<Answer> answers)
    {
        StringBuilder stringBuilder = new StringBuilder();
        HashMap<Long, Answer> idAnswerMap = new HashMap<Long, Answer>();

        for (Answer answer : answers)
        {
            stringBuilder.append(answer.id).append(";");
            idAnswerMap.put(answer.id, answer);
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        getCommensAndUpdateAnswer(stringBuilder.toString(), idAnswerMap);
    }

    private void getCommensAndUpdateAnswer(String answerIds, HashMap<Long, Answer> idAnswerMap)
    {
        boolean hasMore = true;
        int page = 1;
        while (hasMore)
        {
            StackXPage<Comment> commentsPage = getComments(StringConstants.ANSWERS, answerIds, page++);

            if (commentsPage != null && commentsPage.items != null)
            {
                for (Comment comment : commentsPage.items)
                {
                    if (idAnswerMap.containsKey(comment.post_id))
                    {
                        if (idAnswerMap.get(comment.post_id).comments == null)
                            idAnswerMap.get(comment.post_id).comments = new ArrayList<Comment>();

                        idAnswerMap.get(comment.post_id).comments.add(comment);
                    }
                }

                hasMore = commentsPage.hasMore;
            }
            else
                hasMore = false;

        }
    }

    public StackXPage<Comment> getComments(String parent, String site, String parentIds, int page)
    {
        StackXPage<Comment> commentsPage = null;

        String restEndPoint = parent + "/" + parentIds + "/comments";
        Map<String, String> queryParams = getDefaultQueryParams();
        if (site != null)
            queryParams.put(StackUri.QueryParams.SITE, site);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.CREATION);
        queryParams.put(StackUri.QueryParams.ORDER, Order.ASC);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.COMMENT_FILTER);

        JSONObjectWrapper commentsJsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

        if (commentsJsonResponse != null)
        {
            try
            {
                getPageInfo(commentsJsonResponse, commentsPage);

                JSONArray jsonArray = commentsJsonResponse.getJSONArray(JsonFields.ITEMS);
                for (int count = 0; count < jsonArray.length(); count++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(count);

                    if (commentsPage == null)
                    {
                        commentsPage = new StackXPage<Comment>();
                        commentsPage.items = new ArrayList<Comment>();
                    }

                    commentsPage.items.add(getSerializedCommentObject(JSONObjectWrapper.wrap(jsonObject)));
                }
            }
            catch (JSONException e)
            {
                LogWrapper.d(TAG, e.getMessage());
            }
        }
        return commentsPage;
    }

    public StackXPage<Comment> getComments(String parent, String parentIds, int page)
    {
        return getComments(parent, null, parentIds, page);
    }

    public StackXPage<Question> search(String query, int page)
    {
        String restEndPoint = "search";
        Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.IN_TITLE, query);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

        JSONObjectWrapper questionsJsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

        return getQuestionModel(questionsJsonResponse);
    }

    public StackXPage<Question> advancedSearch(SearchCriteria searchCriteria)
    {
        String restEndPoint = "search/advanced";
        Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.putAll(searchCriteria.getMap());

        return getQuestionModel(executeHttpGetRequest(restEndPoint, queryParams));
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }

    public Question getQuestionFullDetails(long id, String site)
    {
        String restEndPoint = "questions/" + id;
        Question question = null;
        Map<String, String> queryParams = getDefaultQueryParams();
        if (site != null)
            queryParams.put(StackUri.QueryParams.SITE, site);
        JSONObjectWrapper questionJsonResponse = executeHttpGetRequest(restEndPoint, queryParams);
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
                LogWrapper.d(TAG, e.getMessage());
            }
        }

        return question;
    }

    public Question getQuestionFullDetails(long id)
    {
        return getQuestionFullDetails(id, null);
    }

    public StackXPage<Question> getAllQuestions(String sort, int page)
    {
        if (sort == null)
            sort = StackUri.Sort.ACTIVITY;

        Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, sort);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

        return getQuestionPage(StringConstants.QUESTIONS, queryParams);
    }

    public StackXPage<Question> getRelatedQuestions(long questionId, int page)
    {
        String restEndPoint = "questions/" + questionId + "/related";
        Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.ACTIVITY);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

        return getQuestionPage(restEndPoint, queryParams);
    }

    public StackXPage<Question> getFaqForTag(String tag, int page)
    {
        if (tag != null)
        {
            String restEndPoint = "tags/" + tag + "/faq";
            Map<String, String> queryParams = AppUtils.getDefaultQueryParams();

            queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
            queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
            queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

            return getQuestionPage(restEndPoint, queryParams);
        }

        return null;
    }

    public StackXPage<Question> getQuestionsForTag(String tag, String sort, int page)
    {
        if (sort == null)
            sort = StackUri.Sort.ACTIVITY;
        if (tag != null)
        {
            String restEndPoint = "search/advanced";

            Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
            queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
            queryParams.put(StackUri.QueryParams.TAGGED, tag);
            queryParams.put(StackUri.QueryParams.SORT, sort);
            queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
            queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
            queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

            return getQuestionPage(restEndPoint, queryParams);
        }

        return null;
    }

    public StackXPage<Question> getSimilar(String title, int page)
    {
        if (title != null)
        {
            String restEndPoint = "similar";
            Map<String, String> queryParams = AppUtils.getDefaultQueryParams();

            queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
            queryParams.put(StackUri.QueryParams.TITLE, title);
            queryParams.put(StackUri.QueryParams.SORT, StackUri.Sort.RELEVANCE);
            queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
            queryParams.put(StackUri.QueryParams.PAGE_SIZE, String.valueOf(StackUri.QueryParamDefaultValues.PAGE_SIZE));

            return getQuestionPage(restEndPoint, queryParams);
        }

        return null;
    }

    private StackXPage<Question> getQuestionPage(String restEndPoint, Map<String, String> queryParams)
    {
        JSONObjectWrapper questionsJsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

        if (questionsJsonResponse != null)
            return getQuestionModel(questionsJsonResponse);

        return null;
    }

    public Answer getAnswer(long id)
    {
        String restEndPoint = "answers/" + id;

        JSONObjectWrapper jsonObjectWrapper = executeHttpGetRequest(restEndPoint, getDefaultQueryParams());
        if (jsonObjectWrapper != null)
        {
            try
            {
                JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1)
                {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));
                    Answer answer = getSerializedAnswerObject(jsonObject);
                    answer.body = jsonObject.getString(JsonFields.Question.BODY);
                    return answer;
                }
            }
            catch (JSONException e)
            {
                LogWrapper.d(TAG, e.getMessage());
            }
        }
        return null;
    }
}

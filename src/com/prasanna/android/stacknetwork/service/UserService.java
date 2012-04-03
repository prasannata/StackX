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
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.InboxItem.ItemType;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
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

    public ArrayList<Site> getAllSitesInNetwork()
    {
        return getSites("sites", null);
    }

    private ArrayList<Site> getSites(String restEndPoint, Map<String, String> queryParams)
    {
        ArrayList<Site> sites = new ArrayList<Site>();

        JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(restEndPoint,
                queryParams);

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
                        Site site = getSerializedSiteObject(new JSONObjectWrapper(siteJsonObject));
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

    private Site getSerializedSiteObject(JSONObjectWrapper siteJsonObject)
    {
        Site site = new Site();
        site.apiSiteParameter = siteJsonObject.getString(JsonFields.Site.API_SITE_PARAMETER);
        site.logoUrl = siteJsonObject.getString(JsonFields.Site.LOGO_URL);
        site.name = siteJsonObject.getString(JsonFields.Site.NAME);
        site.link = siteJsonObject.getString(JsonFields.Site.SITE_URL);
        return site;
    }

    public ArrayList<Site> getSitesForUser(String accessToken)
    {
        return getSites("/me/associated", AppUtils.getAuthenticatedUserQueryParams(accessToken));
    }

    private ArrayList<Question> getQuestions(String restEndPoint, Map<String, String> queryParams)
    {
        JSONObjectWrapper questionsJsonResponse = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
                restEndPoint, queryParams);

        if (questionsJsonResponse != null)
        {
            return getQuestionModel(questionsJsonResponse);
        }

        return null;
    }

    public ArrayList<Question> getMyQuestions(String accessToken, int page)
    {
        String restEndPoint = "/me/questions";

        Map<String, String> queryParams = AppUtils.getAuthenticatedUserQueryParams(accessToken);
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);

        return getQuestions(restEndPoint, queryParams);
    }

    public ArrayList<Question> getQuestionsByUser(long userId, int page)
    {
        String restEndPoint = "/users/" + userId + "/questions";

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);

        return getQuestions(restEndPoint, queryParams);
    }

    public User getLoggedInUser(String accessToken)
    {
        Map<String, String> queryParams = AppUtils.getAuthenticatedUserQueryParams(accessToken);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_DETAIL_FILTER);

        JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding("/me", queryParams);
        JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);
        JSONObject userJsonObject = getIndexFromArray(jsonArray, 0, JSONObject.class);

        return getSerializedUserObject(new JSONObjectWrapper(userJsonObject));
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
            JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);
            JSONObject userJsonObject = getIndexFromArray(jsonArray, 0, JSONObject.class);
            user = getSerializedUserObject(new JSONObjectWrapper(userJsonObject));
        }

        return user;
    }

    public ArrayList<Question> getAllQuestions(int page)
    {
        ArrayList<Question> questions = null;

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

    private ArrayList<Answer> getAnswers(String restEndPoint, Map<String, String> queryParams)
    {
        ArrayList<Answer> answers = new ArrayList<Answer>();

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
                        JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
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

    public ArrayList<Answer> getMyAnswers(String accessToken, int page)
    {
        String restEndPoint = "/me/answers";

        Map<String, String> queryParams = AppUtils.getAuthenticatedUserQueryParams(accessToken);
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.QUESTION_DETAIL_FILTER);

        return getAnswers(restEndPoint, queryParams);
    }

    public ArrayList<Answer> getAnswersByUser(long userId, int page)
    {
        String restEndPoint = "/users/" + userId + "/answers";

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.QUESTION_DETAIL_FILTER);
        return getAnswers(restEndPoint, queryParams);
    }

    public ArrayList<InboxItem> getInbox(String accessToken, int page)
    {
        String restEndPoint = "/me/inbox";

        Map<String, String> queryParams = AppUtils.getAuthenticatedUserQueryParams(accessToken);
        queryParams.put(StackUri.QueryParams.ORDER, StackUri.QueryParamDefaultValues.ORDER);
        queryParams.put(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUri.QueryParams.SORT, StackUri.QueryParamDefaultValues.SORT);
        queryParams.put(StackUri.QueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUri.QueryParams.PAGE_SIZE, StackUri.QueryParamDefaultValues.PAGE_SIZE);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.USER_INBOX_FILTER);

        return getInboxItems(restEndPoint, queryParams);
    }

    private ArrayList<InboxItem> getInboxItems(String restEndPoint, Map<String, String> queryParams)
    {
        ArrayList<InboxItem> inboxItems = null;

        JSONObjectWrapper jsonOfInboxItems = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(restEndPoint,
                queryParams);
        if (jsonOfInboxItems != null)
        {
            JSONArray itemsArray = jsonOfInboxItems.getJSONArray(JsonFields.ITEMS);

            if (itemsArray != null)
            {
                inboxItems = new ArrayList<InboxItem>();

                for (int i = 0; i < itemsArray.length(); i++)
                {
                    try
                    {
                        JSONObjectWrapper itemJsonObject = JSONObjectWrapper.wrap(itemsArray.getJSONObject(i));
                        if (itemJsonObject != null)
                        {
                            InboxItem inboxItem = new InboxItem();
                            inboxItem.questionId = itemJsonObject.getLong(JsonFields.InboxItem.QUESTION_ID);
                            inboxItem.answerId = itemJsonObject.getLong(JsonFields.InboxItem.ANSWER_ID);
                            inboxItem.commentId = itemJsonObject.getLong(JsonFields.InboxItem.COMMENT_ID);
                            inboxItem.itemType = ItemType.getValue(itemJsonObject
                                    .getString(JsonFields.InboxItem.ITEM_TYPE));
                            inboxItem.title = itemJsonObject.getString(JsonFields.InboxItem.TITLE);
                            inboxItem.creationDate = itemJsonObject.getLong(JsonFields.InboxItem.CREATION_DATE);
                            inboxItem.body = itemJsonObject.getString(JsonFields.InboxItem.BODY);
                            inboxItems.add(inboxItem);
                        }
                    }
                    catch (JSONException e)
                    {
                        Log.d(getLogTag(), e.getMessage());
                    }
                }
            }
        }
        return inboxItems;
    }

    public StackExchangeHttpError logout(String accessToken)
    {
        StackExchangeHttpError error = new StackExchangeHttpError();
        error.id = -1;

        String restEndPoint = "/apps/" + accessToken + "/de-authenticate";
        JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(restEndPoint, null);
        boolean success = jsonObject != null && jsonObject.getJSONArray(JsonFields.ITEMS) != null
                && jsonObject.getJSONArray(JsonFields.ITEMS).length() == 0;
        if (success == false)
        {
            error.id = jsonObject.getInt(JsonFields.Error.ERROR_ID);
            error.name = jsonObject.getString(JsonFields.Error.ERROR_NAME);
            error.message = jsonObject.getString(JsonFields.Error.ERROR_MESSAGE);
        }

        return error;
    }
}

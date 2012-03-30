package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.http.HttpHelper;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUriQueryParams;
import com.prasanna.android.stacknetwork.utils.StringConstants;

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
        queryParams.put(StackUriQueryParams.ORDER, "desc");
        queryParams.put(StackUriQueryParams.SORT, "activity");
        queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUriQueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUriQueryParams.PAGE_SIZE, "25");

        JSONObjectWrapper questionsJsonResponse = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
                restEndPoint, queryParams);

        if (questionsJsonResponse != null)
        {
            questions = getQuestionModel(questionsJsonResponse);
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
        queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUriQueryParams.FILTER, StringConstants.StackFilters.USER_DETAIL_FILTER);

        if (userId != -1)
        {
            JSONObjectWrapper jsonObject = HttpHelper.getInstance().getRequestForJsonWithGzipEncoding(
                    "/users/" + userId, queryParams);

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

    public ArrayList<Question> getAllQuestions(int page)
    {
        ArrayList<Question> questions = new ArrayList<Question>();

        String restEndPoint = "questions";
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(StackUriQueryParams.ORDER, "desc");
        queryParams.put(StackUriQueryParams.SORT, "activity");
        queryParams.put(StackUriQueryParams.SITE, OperatingSite.getSite().apiSiteParameter);
        queryParams.put(StackUriQueryParams.PAGE, String.valueOf(page));
        queryParams.put(StackUriQueryParams.PAGE_SIZE, "25");

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
}

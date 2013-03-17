package com.prasanna.android.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackExchangeHttpError;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public class JsonUtil
{

    public static JSONObjectWrapper sitesToJsonObjectWrapper(ArrayList<Site> sites) throws JSONException
    {
        ArrayList<JSONObject> itemArrayList = new ArrayList<JSONObject>();

        for (Site site : sites)
            itemArrayList.add(toJSONObject(site));

        return wrapObject(itemArrayList);
    }

    public static JSONObjectWrapper toJsonObjectWrapper(Site site) throws JSONException
    {
        return addItemToArrayAndGetJSONObjectWrapper(toJSONObject(site));
    }

    private static JSONObject toJSONObject(Site site) throws JSONException
    {
        JSONObject item = new JSONObject();
        item.put(JsonFields.Site.NAME, site.name);
        item.put(JsonFields.Site.API_SITE_PARAMETER, site.apiSiteParameter);
        item.put(JsonFields.Site.AUDIENCE, site.audience);
        item.put(JsonFields.Site.LOGO_URL, site.logoUrl);
        item.put(JsonFields.Site.FAVICON_URL, site.faviconUrl);
        item.put(JsonFields.Site.ICON_URL, site.iconUrl);
        item.put(JsonFields.Site.SITE_URL, site.link);
        return item;
    }

    public static JSONObjectWrapper accountsToJsonObjectWrapper(ArrayList<Account> accounts) throws JSONException
    {
        ArrayList<JSONObject> itemArrayList = new ArrayList<JSONObject>();

        for (Account account : accounts)
            itemArrayList.add(toJSONObject(account));

        return wrapObject(itemArrayList);

    }

    public static JSONObjectWrapper toJsonObjectWrapper(Account account) throws JSONException
    {
        return addItemToArrayAndGetJSONObjectWrapper(toJSONObject(account));
    }

    private static JSONObject toJSONObject(Account account) throws JSONException
    {
        JSONObject item = new JSONObject();
        item.put(JsonFields.Account.ACCOUNT_ID, account.id);
        item.put(JsonFields.Account.USER_ID, account.userId);
        item.put(JsonFields.Account.SITE_NAME, account.siteName);
        item.put(JsonFields.Account.SITE_URL, account.siteUrl);
        item.put(JsonFields.Account.USER_TYPE, account.userType.getValue());
        return item;
    }

    public static JSONObjectWrapper toJsonObjectWrapper(Question question) throws JSONException
    {
        return addItemToArrayAndGetJSONObjectWrapper(toJSONObject(question));
    }

    public static JSONObjectWrapper questionsToJsonObjectWrapper(ArrayList<Question> questions) throws JSONException
    {
        ArrayList<JSONObject> itemArrayList = new ArrayList<JSONObject>();

        for (Question question : questions)
            itemArrayList.add(toJSONObject(question));

        return wrapObject(itemArrayList);
    }

    private static JSONObject toJSONObject(Question question) throws JSONException
    {
        JSONObject item = new JSONObject();
        item.put(JsonFields.Question.QUESTION_ID, question.id);
        item.put(JsonFields.Question.TITLE, question.title);
        item.put(JsonFields.Question.SCORE, question.score);
        item.put(JsonFields.Question.ANSWER_COUNT, question.answerCount);
        item.put(JsonFields.Question.IS_ANSWERED, question.answered);
        item.put(JsonFields.Question.VIEW_COUNT, question.viewCount);
        item.put(JsonFields.Question.TAGS, question.tags);
        item.put(JsonFields.Question.CREATION_DATE, question.creationDate);
        item.put(JsonFields.Question.LINK, question.link);
        item.put(JsonFields.Question.BODY, question.body);
        if (question.hasAcceptedAnswer)
            item.put(JsonFields.Question.ACCEPTED_ANSWER_ID, 1);
        item.put(JsonFields.Question.OWNER, toJSONObject(question.owner));
        return item;
    }

    public static JSONObjectWrapper toJsonObjectWrapper(Answer answer) throws JSONException
    {
        return addItemToArrayAndGetJSONObjectWrapper(toJSONObject(answer));
    }

    private static JSONObject toJSONObject(Answer answer) throws JSONException
    {
        JSONObject item = new JSONObject();
        item.put(JsonFields.Answer.ANSWER_ID, answer.id);
        item.put(JsonFields.Answer.QUESTION_ID, answer.questionId);
        item.put(JsonFields.Answer.TITLE, answer.title);
        item.put(JsonFields.Answer.SCORE, answer.score);
        item.put(JsonFields.Answer.BODY, answer.body);
        item.put(JsonFields.Answer.IS_ACCEPTED, answer.accepted);
        item.put(JsonFields.Answer.CREATION_DATE, answer.creationDate);
        item.put(JsonFields.Answer.OWNER, toJSONObject(answer.owner));
        return item;
    }

    public static JSONObjectWrapper commentsToJsonObjectWrapper(ArrayList<Comment> comments) throws JSONException
    {
        ArrayList<JSONObject> itemArrayList = new ArrayList<JSONObject>();

        for (Comment comment : comments)
            itemArrayList.add(toJSONObject(comment));

        return wrapObject(itemArrayList);
    }

    public static JSONObject toJSONObject(Comment comment) throws JSONException
    {
        JSONObject item = new JSONObject();
        item.put(JsonFields.Comment.COMMENT_ID, comment.id);
        item.put(JsonFields.Comment.POST_ID, comment.post_id);
        item.put(JsonFields.Comment.BODY, comment.body);
        item.put(JsonFields.Comment.SCORE, comment.score);
        item.put(JsonFields.Comment.CREATION_DATE, comment.creationDate);
        item.put(JsonFields.Comment.OWNER, toJSONObject(comment.owner));
        return item;
    }

    public static JSONObjectWrapper commentToJsonObjectWrapper(Comment comment) throws JSONException
    {
        return addItemToArrayAndGetJSONObjectWrapper(toJSONObject(comment));
    }

    public static JSONObjectWrapper toJsonObjectWrapper(User user) throws JSONException
    {
        return null;
    }

    public static JSONObjectWrapper toJsonObjectWrapper(InboxItem inboxItem) throws JSONException
    {
        return null;
    }

    public static JSONObjectWrapper toJsonObjectWrapper(HashMap<String, String> nameValueMap) throws JSONException,
                    IOException
    {
        JSONObject item = new JSONObject();
        for (String key : nameValueMap.keySet())
            item.put(key, nameValueMap.get(key));

        return addItemToArrayAndGetJSONObjectWrapper(item);
    }

    public static JSONObjectWrapper answersToJsonObjectWrapper(ArrayList<Answer> answers) throws JSONException
    {
        ArrayList<JSONObject> itemArrayList = new ArrayList<JSONObject>();

        for (Answer answer : answers)
            itemArrayList.add(toJSONObject(answer));

        return wrapObject(itemArrayList);
    }

    private static JSONObject toJSONObject(User user) throws JSONException
    {
        JSONObject owner = new JSONObject();
        owner.put(JsonFields.User.USER_ID, user.id);
        owner.put(JsonFields.User.DISPLAY_NAME, user.displayName);
        owner.put(JsonFields.User.REPUTATION, user.reputation);
        owner.put(JsonFields.User.ACCEPT_RATE, user.acceptRate);
        owner.put(JsonFields.User.PROFILE_IMAGE, user.profileImageLink);
        return owner;
    }

    public static JSONObjectWrapper toJsonObjectWrapper(StackExchangeHttpError stackExchangeHttpError) throws JSONException
    {
        JSONObject jsonObject = toJSONObject(stackExchangeHttpError);
        jsonObject.put(JsonFields.ITEMS, new ArrayList<JSONObject>());
        return new JSONObjectWrapper(jsonObject);
    }
    
    private static JSONObject toJSONObject(StackExchangeHttpError stackExchangeHttpError) throws JSONException
    {
        JSONObject error = new JSONObject();
        error.put(JsonFields.Error.ERROR_ID, stackExchangeHttpError.id);
        error.put(JsonFields.Error.ERROR_NAME, stackExchangeHttpError.name);
        error.put(JsonFields.Error.ERROR_MESSAGE, stackExchangeHttpError.message);
        return error;
    }

    private static JSONObjectWrapper addItemToArrayAndGetJSONObjectWrapper(JSONObject item) throws JSONException
    {
        ArrayList<JSONObject> itemArrayList = new ArrayList<JSONObject>();
        itemArrayList.add(item);

        return wrapObject(itemArrayList);
    }

    private static JSONObjectWrapper wrapObject(ArrayList<JSONObject> itemArrayList) throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put(JsonFields.ITEMS, itemArrayList);
        return new JSONObjectWrapper(new JSONObject(object.toString()));
    }

    public static JSONObjectWrapper toJsonObjectWrapper(String accessToken, long accountId) throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFields.AccessToken.ACCESS_TOKEN, accessToken);
        jsonObject.put(JsonFields.AccessToken.ACCOUNT_ID, accountId);
        return new JSONObjectWrapper(jsonObject);
    }

}

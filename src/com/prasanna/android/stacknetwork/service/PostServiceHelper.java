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

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Post;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public class PostServiceHelper extends AbstractBaseServiceHelper
{
    private static final String TAG = PostIntentService.class.getSimpleName();
    private static final PostServiceHelper postService = new PostServiceHelper();

    public static PostServiceHelper getInstance()
    {
        return postService;
    }

    private PostServiceHelper()
    {
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }

    public Post getPost(long id)
    {
        String restEndPoint = "/posts/" + id;
        Map<String, String> queryParams = getDefaultQueryParams();
        JSONObjectWrapper jsonResponse = executeHttpRequest(restEndPoint, queryParams);

        if (jsonResponse != null)
        {
            try
            {
                JSONArray jsonArray = jsonResponse.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1)
                {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

                    if (jsonObject != null)
                    {
                        Post post = new Post();
                        post.id = jsonObject.getLong(JsonFields.Post.POST_ID);
                        post.body = jsonObject.getString(JsonFields.Post.BODY);
                        post.creationDate = jsonObject.getLong(JsonFields.Post.CREATION_DATE);
                        post.score = jsonObject.getInt(JsonFields.Post.SCORE);
                        post.owner = getSerializableUserSnippetObject(jsonObject.getJSONObject(JsonFields.Post.OWNER));
                        return post;
                    }
                }
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }

        }

        return null;
    }

    public Post getPostComment(long id)
    {
        String restEndPoint = "/posts/" + id;
        Map<String, String> queryParams = getDefaultQueryParams();
        JSONObjectWrapper jsonResponse = executeHttpRequest(restEndPoint, queryParams);

        if (jsonResponse != null)
        {
            try
            {
                JSONArray jsonArray = jsonResponse.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1)
                {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

                    if (jsonObject != null)
                    {
                        Post post = new Post();
                        post.id = jsonObject.getLong(JsonFields.Post.POST_ID);
                        post.body = jsonObject.getString(JsonFields.Post.BODY);
                        post.creationDate = jsonObject.getLong(JsonFields.Post.CREATION_DATE);
                        post.score = jsonObject.getInt(JsonFields.Post.SCORE);
                        post.owner = getSerializableUserSnippetObject(jsonObject.getJSONObject(JsonFields.Post.OWNER));
                        return post;
                    }
                }
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }

        }

        return null;
    }

    public Comment getComment(long id)
    {
        String restEndPoint = "/comments/" + id;
        Map<String, String> queryParams = getDefaultQueryParams();
        JSONObjectWrapper jsonResponse = executeHttpRequest(restEndPoint, queryParams);

        if (jsonResponse != null)
        {
            try
            {
                JSONArray jsonArray = jsonResponse.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1)
                {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

                    if (jsonObject != null)
                    {
                        Comment comment = new Comment();
                        comment.id = jsonObject.getLong(JsonFields.Comment.COMMENT_ID);
                        comment.post_id = jsonObject.getLong(JsonFields.Comment.POST_ID);
                        comment.body = jsonObject.getString(JsonFields.Post.BODY);
                        comment.creationDate = jsonObject.getLong(JsonFields.Post.CREATION_DATE);
                        comment.score = jsonObject.getInt(JsonFields.Post.SCORE);
                        comment.owner = getSerializableUserSnippetObject(jsonObject
                                        .getJSONObject(JsonFields.Post.OWNER));
                        return comment;
                    }
                }
            }
            catch (JSONException e)
            {
                Log.d(TAG, e.getMessage());
            }

        }

        return null;
    }
}

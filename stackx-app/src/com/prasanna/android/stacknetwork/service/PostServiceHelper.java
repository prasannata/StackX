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

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Post;
import com.prasanna.android.stacknetwork.model.Post.PostType;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.utils.LogWrapper;

public class PostServiceHelper extends AbstractBaseServiceHelper {
    private static final String TAG = PostIntentService.class.getSimpleName();
    private static final PostServiceHelper postService = new PostServiceHelper();

    public static PostServiceHelper getInstance() {
        return postService;
    }

    private PostServiceHelper() {
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public Post getPost(long id, String site) {
        String restEndPoint = "/posts/" + id;
        Map<String, String> queryParams = getDefaultQueryParams(site);
        JSONObjectWrapper jsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

        if (jsonResponse != null) {
            try {
                JSONArray jsonArray = jsonResponse.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1) {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

                    if (jsonObject != null) {
                        Post post = new Post();
                        post.id = jsonObject.getLong(JsonFields.Post.POST_ID);
                        post.body = jsonObject.getString(JsonFields.Post.BODY);
                        post.creationDate = jsonObject.getLong(JsonFields.Post.CREATION_DATE);
                        String postType = jsonObject.getString(JsonFields.Post.POST_TYPE);
                        if (postType != null)
                            post.postType = PostType.getEnum(postType);
                        post.score = jsonObject.getInt(JsonFields.Post.SCORE);
                        post.owner = getSerializableUserSnippetObject(jsonObject.getJSONObject(JsonFields.Post.OWNER));
                        return post;
                    }
                }
            }
            catch (JSONException e) {
                LogWrapper.d(TAG, e.getMessage());
            }

        }

        return null;
    }

    public Post getPostComment(long id, String site) {
        String restEndPoint = "/posts/" + id;
        Map<String, String> queryParams = getDefaultQueryParams(site);
        JSONObjectWrapper jsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

        if (jsonResponse != null) {
            try {
                JSONArray jsonArray = jsonResponse.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1) {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

                    if (jsonObject != null) {
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
            catch (JSONException e) {
                LogWrapper.d(TAG, e.getMessage());
            }

        }

        return null;
    }

    public Comment getComment(long id, String site) {
        String restEndPoint = "/comments/" + id;
        Map<String, String> queryParams = getDefaultQueryParams(site);
        queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.COMMENT_FILTER);
        JSONObjectWrapper jsonResponse = executeHttpGetRequest(restEndPoint, queryParams);

        if (jsonResponse != null) {
            try {
                JSONArray jsonArray = jsonResponse.getJSONArray(JsonFields.ITEMS);

                if (jsonArray != null && jsonArray.length() == 1) {
                    JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

                    if (jsonObject != null) {
                        Comment comment = new Comment();
                        comment.id = jsonObject.getLong(JsonFields.Comment.COMMENT_ID);
                        comment.post_id = jsonObject.getLong(JsonFields.Comment.POST_ID);
                        comment.body = jsonObject.getString(JsonFields.Post.BODY);
                        comment.creationDate = jsonObject.getLong(JsonFields.Comment.CREATION_DATE);
                        comment.score = jsonObject.getInt(JsonFields.Comment.SCORE);
                        comment.owner =
                                getSerializableUserSnippetObject(jsonObject.getJSONObject(JsonFields.Post.OWNER));
                        comment.type = PostType.getEnum(jsonObject.getString(JsonFields.Comment.POST_TYPE));
                        return comment;
                    }
                }
            }
            catch (JSONException e) {
                LogWrapper.d(TAG, e.getMessage());
            }

        }

        return null;
    }
}

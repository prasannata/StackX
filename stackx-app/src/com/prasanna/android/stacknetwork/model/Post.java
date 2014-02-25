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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.utils.LogWrapper;

public class Post extends IdentifiableItem implements Serializable {
  private static final long serialVersionUID = -7850382261881073395L;
  private static final String TAG = Post.class.getSimpleName();

  public enum PostType {
    COMMENT("comment"),
    QUESTION("question"),
    ANSWER("answer");

    private final String value;

    PostType(String value) {
      this.value = value;
    }

    public static PostType getEnum(String string) {
      PostType postType = null;

      if (string != null) {
        try {
          postType = valueOf(string.toUpperCase());
        } catch (IllegalArgumentException e) {
          postType = null;
        }

      }
      return postType;
    }

    public String getValue() {
      return value;
    }
  }

  public PostType postType;

  public int score = 0;

  public String title;

  public String body;

  public User owner;

  public static Post parse(final JSONObjectWrapper jsonObjectWrapper) {

    if (jsonObjectWrapper == null) return null;

    try {
      JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);

      if (jsonArray != null && jsonArray.length() == 1) {
        JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));
        return parsePost(jsonObject);
      }
    } catch (JSONException e) {
      LogWrapper.d(TAG, e.getMessage());
    }

    return null;
  }

  private static Post parsePost(JSONObjectWrapper jsonObject) {
    if (jsonObject != null) {
      Post post = new Post();
      post.id = jsonObject.getLong(JsonFields.Post.POST_ID);
      post.title = jsonObject.getString(JsonFields.Post.TITLE);
      post.body = jsonObject.getString(JsonFields.Post.BODY);
      post.creationDate = jsonObject.getLong(JsonFields.Post.CREATION_DATE);
      post.postType = PostType.getEnum(jsonObject.getString(JsonFields.Post.POST_TYPE));
      post.score = jsonObject.getInt(JsonFields.Post.SCORE);
      post.owner = User.parseAsSnippet(jsonObject.getJSONObject(JsonFields.Post.OWNER));
      return post;
    }

    return null;
  }

  public static List<Post> parseCollection(final JSONObjectWrapper jsonObjectWrapper) {
    List<Post> posts = new ArrayList<Post>();
    if (jsonObjectWrapper != null) {
      try {
        JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);
        if (jsonArray != null) {
          for (int idx = 0; idx < jsonArray.length(); idx++) {
            JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(idx));
            Post post = parsePost(jsonObject);
            if (post != null) posts.add(post);
          }
        }
      } catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }
    }

    return posts;
  }
}

/*
    Copyright 2012 Prasanna Thirumalai
    
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

import org.json.JSONArray;
import org.json.JSONException;

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.utils.LogWrapper;

public class Comment extends Post implements Serializable {
  private static final long serialVersionUID = 4507419037482371574L;
  private static final String TAG = Comment.class.getSimpleName();

  public long post_id;

  public PostType type;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + (int) (post_id ^ (post_id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Comment other = (Comment) obj;
    if (id != other.id)
      return false;
    if (post_id != other.post_id)
      return false;
    return true;
  }

  public static Comment parseCommentItem(final JSONObjectWrapper jsonObjectWrapper) {
    if (jsonObjectWrapper != null) {
      try {
        JSONArray jsonArray = jsonObjectWrapper.getJSONArray(JsonFields.ITEMS);

        if (jsonArray != null && jsonArray.length() == 1) {
          JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(0));

          return parse(jsonObject);
        }
      }
      catch (JSONException e) {
        LogWrapper.d(TAG, e.getMessage());
      }

    }

    return null;
  }

  public static Comment parse(JSONObjectWrapper jsonObject) {
    if (jsonObject != null) {
      Comment comment = new Comment();
      comment.id = jsonObject.getLong(JsonFields.Comment.COMMENT_ID);
      comment.post_id = jsonObject.getLong(JsonFields.Comment.POST_ID);
      comment.body = jsonObject.getString(JsonFields.Post.BODY);
      comment.creationDate = jsonObject.getLong(JsonFields.Comment.CREATION_DATE);
      comment.score = jsonObject.getInt(JsonFields.Comment.SCORE, 0);
      comment.owner = User.parseAsSnippet(jsonObject.getJSONObject(JsonFields.Post.OWNER));
      comment.type = PostType.getEnum(jsonObject.getString(JsonFields.Comment.POST_TYPE));
      return comment;
    }

    return null;
  }
}

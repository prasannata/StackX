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

import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Post;
import com.prasanna.android.stacknetwork.utils.StackUri;

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
    return Post.parse(executeHttpGetRequest("/posts/" + id, getDefaultQueryParams(site)));
  }

  public Comment getComment(long id, String site) {
    Map<String, String> queryParams = getDefaultQueryParams(site);
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.COMMENT_FILTER);
    return Comment.parseCommentItem(executeHttpGetRequest("/comments/" + id, queryParams));
  }
}

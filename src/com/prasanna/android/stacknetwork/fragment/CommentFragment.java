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

package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.database.SQLException;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class CommentFragment extends ItemListFragment<Comment> implements ListItemView<Comment>
{
    private static final String TAG = CommentFragment.class.getSimpleName();
    private ArrayList<Comment> comments;
    private HashMap<ObjectType, WritePermission> writePermissions;
    private ImageView replyToComment;
    private ImageView editComment;
    private ImageView deleteComment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (itemsContainer == null)
        {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, container, false);
            itemListAdapter = new ItemListAdapter<Comment>(getActivity(), R.layout.comment, new ArrayList<Comment>(),
                            this);
        }

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        getWritePermissions();
        if (comments != null && !comments.isEmpty())
            itemListAdapter.addAll(comments);
    }

    private void getWritePermissions()
    {
        WritePermissionDAO writePermissionDAO = new WritePermissionDAO(getActivity());
        try
        {
            writePermissionDAO.open();
            writePermissions = writePermissionDAO.getPermissions(OperatingSite.getSite().apiSiteParameter);
        }
        catch (SQLException e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            writePermissionDAO.close();
        }
    }

    @Override
    protected String getReceiverExtraName()
    {
        return StringConstants.COMMENTS;
    }

    @Override
    protected void startIntentService()
    {
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }

    @Override
    public View getView(final Comment comment, View convertView, ViewGroup parent)
    {
        RelativeLayout commentLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.comment,
                        null);

        TextView textView = (TextView) commentLayout.findViewById(R.id.commentScore);
        textView.setText(AppUtils.formatNumber(comment.score));

        textView = (TextView) commentLayout.findViewById(R.id.commentTitle);
        textView.setText(Html.fromHtml(comment.body));

        textView = (TextView) commentLayout.findViewById(R.id.commentOwner);
        textView.setText(DateTimeUtils.getElapsedDurationSince(comment.creationDate) + " by "
                        + Html.fromHtml(comment.owner.displayName));

        if (AppUtils.inAuthenticatedRealm(getActivity()))
            setupCommentWriteOptions(comment, commentLayout);

        return commentLayout;
    }

    private void setupCommentWriteOptions(final Comment comment, RelativeLayout commentLayout)
    {
        boolean myComment = isMyComment(comment);

        commentLayout.findViewById(R.id.commentWriteOptions).setVisibility(View.VISIBLE);

        if (canAddComment() && !myComment)
            setupReplyToComment(comment, commentLayout);
        else
        {
            if (myComment)
                setupMyCommentOptions(commentLayout);
        }
    }

    private void setupReplyToComment(final Comment comment, RelativeLayout commentLayout)
    {
        replyToComment = (ImageView) commentLayout.findViewById(R.id.replyToComment);
        replyToComment.setVisibility(View.VISIBLE);
        replyToComment.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(), "Reply to comment by " + comment.owner.displayName, Toast.LENGTH_LONG)
                                .show();
            }
        });
    }

    private void setupMyCommentOptions(RelativeLayout commentLayout)
    {
        Log.d(TAG, "Setting up my comment edit options");

        commentLayout.findViewById(R.id.commentEditOptions).setVisibility(View.VISIBLE);

        if (canEditComment())
        {
            Log.d(TAG, "I can edit my comment");
            setupEditComment(commentLayout);
        }

        if (canDelComment())
        {
            Log.d(TAG, "I can delete my comment");
            setupDeleteComment(commentLayout);
        }
    }

    private void setupDeleteComment(RelativeLayout commentLayout)
    {
        deleteComment = (ImageView) commentLayout.findViewById(R.id.deleteComment);
        deleteComment.setVisibility(View.VISIBLE);
        deleteComment.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(), "Delete my comment", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupEditComment(RelativeLayout commentLayout)
    {
        editComment = (ImageView) commentLayout.findViewById(R.id.editComment);
        editComment.setVisibility(View.VISIBLE);
        editComment.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(), "Edit my comment", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isMyComment(Comment comment)
    {
        long myId = SharedPreferencesUtil.getLong(getActivity(), StringConstants.USER_ID, -1);
        if (myId == -1)
        {
            Log.w(TAG, "User id not set");
            return false;
        }

        return (comment.owner != null && comment.owner.id == myId);
    }

    private boolean canAddComment()
    {
        return (isValid(ObjectType.COMMENT) && writePermissions.get(ObjectType.COMMENT).canAdd);
    }

    private boolean canEditComment()
    {
        return (isValid(ObjectType.COMMENT) && writePermissions.get(ObjectType.COMMENT).canEdit);
    }

    private boolean canDelComment()
    {
        return (isValid(ObjectType.COMMENT) && writePermissions.get(ObjectType.COMMENT).canDelete);
    }

    private boolean isValid(ObjectType objectType)
    {
        return objectType != null && writePermissions != null && writePermissions.containsKey(objectType);
    }

    public void setComments(ArrayList<Comment> comments)
    {
        this.comments = comments;
    }
}

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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.DialogBuilder;
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
    private ImageView finishEditComment;
    private ImageView cancelEditComment;
    private OnCommentChangeListener onCommentChangeListener;
    private EditText editTextForTitle;
    private ProgressDialog progressDialog;

    public interface OnCommentChangeListener
    {
        void onCommentUpdate(Comment comment);

        void onCommentDelete(long commentId);
    }

    public void setOnCommentChangeListener(OnCommentChangeListener onCommentChangeListener)
    {
        this.onCommentChangeListener = onCommentChangeListener;
        Log.d(TAG, "OnCommentChangeListener: " + (onCommentChangeListener != null));
    }

    public void setComments(ArrayList<Comment> comments)
    {
        this.comments = comments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (itemsContainer == null)
        {
            if (comments == null)
                comments = new ArrayList<Comment>();
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, container, false);
            itemListAdapter = new ItemListAdapter<Comment>(getActivity(), R.layout.comment, comments, this);
        }

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        getListView().setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        getWritePermissions();
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
        RelativeLayout commentLayout = (RelativeLayout) convertView;

        if (commentLayout == null)
        {
            commentLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.comment, null);

            TextView textView = (TextView) commentLayout.findViewById(R.id.commentScore);
            textView.setText(AppUtils.formatNumber(comment.score));

            editTextForTitle = (EditText) commentLayout.findViewById(R.id.commentTitle);
            editTextForTitle.setText(Html.fromHtml(comment.body));

            textView = (TextView) commentLayout.findViewById(R.id.commentOwner);
            textView.setText(DateTimeUtils.getElapsedDurationSince(comment.creationDate) + " by "
                            + Html.fromHtml(comment.owner.displayName));

            if (AppUtils.inAuthenticatedRealm(getActivity()))
                setupCommentWriteOptions(comment, commentLayout);
        }
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
                setupMyCommentOptions(comment, commentLayout);
        }
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

    private void setupMyCommentOptions(Comment comment, RelativeLayout commentLayout)
    {
        Log.d(TAG, "Setting up my comment edit options");

        commentLayout.findViewById(R.id.commentEditOptions).setVisibility(View.VISIBLE);

        if (canEditComment())
        {
            Log.d(TAG, "I can edit my comment");
            setupEditComment(comment.id, commentLayout);
        }

        if (canDelComment())
        {
            Log.d(TAG, "I can delete my comment");
            setupDeleteComment(comment.id, commentLayout);
        }
    }

    private void setupEditComment(long commentId, RelativeLayout commentLayout)
    {
        setupFinishEditComment(commentId, commentLayout);
        setupCancelEditComment(commentLayout);

        editComment = (ImageView) commentLayout.findViewById(R.id.editComment);
        editComment.setVisibility(View.VISIBLE);
        editComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                prepareForEditText(true);

                if (editTextForTitle.getText() != null)
                    editTextForTitle.setSelection(editTextForTitle.getText().length());
                itemsContainer.clearFocus();
                editTextForTitle.requestFocus();
                AppUtils.showSoftInput(getActivity(), editTextForTitle);
            }
        });
    }

    private void setupFinishEditComment(final long commentId, RelativeLayout commentLayout)
    {
        finishEditComment = (ImageView) commentLayout.findViewById(R.id.finishEditComment);
        finishEditComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startServiceForEditComment(commentId, editTextForTitle.getText().toString());
                markCommentEnd(v);
            }
        });
    }

    private void setupCancelEditComment(RelativeLayout commentLayout)
    {
        cancelEditComment = (ImageView) commentLayout.findViewById(R.id.cancelEditComment);
        cancelEditComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                markCommentEnd(v);
            }
        });
    }

    private void setupDeleteComment(final long commentId, RelativeLayout commentLayout)
    {
        deleteComment = (ImageView) commentLayout.findViewById(R.id.deleteComment);
        deleteComment.setVisibility(View.VISIBLE);
        deleteComment.setOnClickListener(new View.OnClickListener()
        {
            private OnClickListener listener = new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                    {
                        startServiceForDelComment(commentId);
                    }
                }
            };

            @Override
            public void onClick(View v)
            {
                DialogBuilder.yesNoDialog(getActivity(), R.string.sureQuestion, listener).show();
            }
        });
    }

    private void prepareForEditText(boolean edit)
    {
        editTextForTitle.setEnabled(edit);
        editTextForTitle.setClickable(edit);
        editTextForTitle.setFocusable(edit);
        editTextForTitle.setFocusableInTouchMode(edit);

        editComment.setVisibility(edit ? View.GONE : View.VISIBLE);
        deleteComment.setVisibility(edit ? View.GONE : View.VISIBLE);
        finishEditComment.setVisibility(edit ? View.VISIBLE : View.GONE);
        cancelEditComment.setVisibility(edit ? View.VISIBLE : View.GONE);
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

    public boolean hasNoComments()
    {
        return comments == null || comments.isEmpty();
    }

    private void removeSelf()
    {
        getFragmentManager().popBackStackImmediate();
    }

    private void startServiceForEditComment(long commentId, final String editedText)
    {
        if (AppUtils.allowedToWrite(getActivity()))
        {
            progressDialog = new ProgressDialog(getActivity(), R.style.dialogNoText);
            progressDialog.show();

            Intent intent = new Intent(getActivity(), WriteIntentService.class);
            intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
            intent.putExtra(StringConstants.COMMENT_ID, commentId);
            intent.putExtra(StringConstants.BODY, editedText);
            intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_EDIT_COMMENT);
            startService(intent);
        }
        else
        {
            long minSecondsBetweenWrite = SharedPreferencesUtil.getLong(getActivity(),
                            WritePermission.PREF_MIN_SECONDS_BETWEEN_WRITE, 0);
            Toast.makeText(getActivity(), "You have to wait a minium of " + minSecondsBetweenWrite + " between writes",
                            Toast.LENGTH_LONG).show();
        }

    }

    private void startServiceForDelComment(long commentId)
    {
        progressDialog = new ProgressDialog(getActivity(), R.style.dialogNoText);
        progressDialog.show();

        Intent intent = new Intent(getActivity(), WriteIntentService.class);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
        intent.putExtra(StringConstants.COMMENT_ID, commentId);
        intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_DEL_COMMENT);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        serviceRunning = false;
        switch (resultCode)
        {
            case WriteIntentService.ACTION_EDIT_COMMENT:
                Log.d(TAG, "Receiver invoked for ACTION_EDIT_COMMENT");
                progressDialog.dismiss();
                onEditCommentComplete(resultData);
                break;

            case WriteIntentService.ACTION_DEL_COMMENT:
                Log.d(TAG, "Receiver invoked for ACTION_DEL_COMMENT");
                progressDialog.dismiss();
                onDelCommentComplete(resultData);
                break;
        }
    }

    private void onEditCommentComplete(Bundle resultData)
    {
        if (onCommentChangeListener != null)
        {
            onCommentChangeListener.onCommentUpdate((Comment) resultData.getSerializable(StringConstants.COMMENT));
        }
    }

    private void onDelCommentComplete(Bundle resultData)
    {
        if (onCommentChangeListener != null)
        {
            onCommentChangeListener.onCommentDelete(resultData.getLong(StringConstants.COMMENT_ID));

            itemListAdapter.notifyDataSetChanged();
            if (comments.isEmpty())
                removeSelf();

        }
    }

    private void markCommentEnd(View v)
    {
        AppUtils.hideSoftInput(getActivity(), v);
        prepareForEditText(false);
        editTextForTitle.clearFocus();
        itemsContainer.requestFocus();
    }
}

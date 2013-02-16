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

import android.app.FragmentTransaction;
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

import com.prasanna.android.http.AbstractHttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
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
    private ProgressDialog progressDialog;
    private OnCommentChangeListener onCommentChangeListener;
    private RestQueryResultReceiver resultReceiver;
    private LinearLayout postCommentFragmentContainer;
    private PostCommentFragment postCommentFragment;
    private CommentViewHolder selectedViewForReply;

    public interface OnCommentChangeListener
    {
        void onCommentAdd(Comment comment);

        void onCommentUpdate(Comment comment);

        void onCommentDelete(long commentId);
    }

    static class CommentViewHolder
    {
        long id;
        RelativeLayout viewGroup;
        TextView score;
        EditText title;
        TextView owner;
        ImageView editComment;
        ImageView replyToComment;
        ImageView finishEditComment;
        ImageView cancelEditComment;
        ImageView deleteComment;
        LinearLayout commentEditOptions;
        RelativeLayout commentWriteOptions;
    }

    public void setOnCommentChangeListener(OnCommentChangeListener onCommentChangeListener)
    {
        this.onCommentChangeListener = onCommentChangeListener;
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
            itemsContainer = (ViewGroup) inflater.inflate(R.layout.comment_list_view, container, false);
            postCommentFragmentContainer = (LinearLayout) itemsContainer
                            .findViewById(R.id.post_comment_fragment_container);
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
        getListView().setItemsCanFocus(true);
        getListView().setClickable(false);
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
    public View getView(final Comment comment, int position, View convertView, ViewGroup parent)
    {
        RelativeLayout commentLayout = (RelativeLayout) convertView;
        CommentViewHolder holder;
        if (commentLayout == null)
        {
            commentLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.comment, null);
            holder = new CommentViewHolder();
            holder.viewGroup = commentLayout;
            holder.score = (TextView) commentLayout.findViewById(R.id.commentScore);
            holder.title = (EditText) commentLayout.findViewById(R.id.commentTitle);
            holder.owner = (TextView) commentLayout.findViewById(R.id.commentOwner);
            holder.editComment = (ImageView) commentLayout.findViewById(R.id.editComment);
            holder.replyToComment = (ImageView) commentLayout.findViewById(R.id.replyToComment);
            holder.finishEditComment = (ImageView) commentLayout.findViewById(R.id.finishEditComment);
            holder.cancelEditComment = (ImageView) commentLayout.findViewById(R.id.cancelEditComment);
            holder.deleteComment = (ImageView) commentLayout.findViewById(R.id.deleteComment);
            holder.commentWriteOptions = (RelativeLayout) commentLayout.findViewById(R.id.commentWriteOptions);
            holder.commentEditOptions = (LinearLayout) commentLayout.findViewById(R.id.commentEditOptions);

            commentLayout.setTag(holder);
        }
        else
            holder = (CommentViewHolder) commentLayout.getTag();

        holder.id = comment.id;
        holder.score.setText(AppUtils.formatNumber(comment.score));
        holder.title.setText("");
        holder.title.append(Html.fromHtml(comment.body));
        holder.owner.setText(DateTimeUtils.getElapsedDurationSince(comment.creationDate) + " by "
                        + Html.fromHtml(comment.owner.displayName));

        if (AppUtils.inAuthenticatedRealm(getActivity()))
            setupCommentWriteOptions(comment, position, holder);

        if (selectedViewForReply != null && selectedViewForReply.id == comment.id)
        {
            selectedViewForReply.viewGroup.setBackgroundColor(getResources().getColor(R.color.lightGrey));
            selectedViewForReply.replyToComment.setVisibility(View.GONE);
        }
        else
        {
            holder.viewGroup.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
            holder.replyToComment.setVisibility(View.VISIBLE);
        }

        return commentLayout;
    }

    private void setupCommentWriteOptions(final Comment comment, final int position, final CommentViewHolder holder)
    {
        boolean myComment = isMyComment(comment);

        holder.commentWriteOptions.setVisibility(View.VISIBLE);

        if (canAddComment() && !myComment)
            setupReplyToComment(comment, position, holder);
        else
        {
            if (myComment)
                setupMyCommentOptions(comment, holder);
        }
    }

    private boolean isMyComment(Comment comment)
    {
        long myId = SharedPreferencesUtil.getLong(getActivity(), StringConstants.USER_ID, -1);
        return (comment.owner != null && comment.owner.id == myId);
    }

    private void setupReplyToComment(final Comment comment, final int position, final CommentViewHolder holder)
    {
        holder.replyToComment.setVisibility(View.VISIBLE);
        holder.replyToComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissAnotherReplyCommentEditTextIfExist();

                selectedViewForReply = holder;
                selectedViewForReply.replyToComment.setVisibility(View.GONE);
                selectedViewForReply.viewGroup.setBackgroundColor(getResources().getColor(R.color.lightGrey));
                Log.d(TAG, "Select comment for reply: " + selectedViewForReply.id);

                displayPostCommentFragment(comment.post_id, "@" + comment.owner.displayName + " ");
                getListView().smoothScrollToPositionFromTop(position, 0);
            }
        });
    }

    private void setupMyCommentOptions(Comment comment, CommentViewHolder holder)
    {
        holder.commentEditOptions.setVisibility(View.VISIBLE);

        if (canEditComment())
            setupEditComment(comment.id, holder);

        if (canDelComment())
            setupDeleteComment(comment.id, holder);
    }

    private void setupEditComment(long commentId, final CommentViewHolder holder)
    {
        setupFinishEditComment(commentId, holder);
        setupCancelEditComment(holder);

        holder.editComment.setVisibility(View.VISIBLE);
        holder.editComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                prepareForEditText(holder, true);

                itemsContainer.clearFocus();
                holder.title.requestFocus();

                AppUtils.showSoftInput(getActivity(), holder.title);

                if (holder.title.getText() != null)
                    holder.title.setSelection(holder.title.getText().length());
            }
        });
    }

    private void setupFinishEditComment(final long commentId, final CommentViewHolder holder)
    {
        holder.finishEditComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startServiceForEditComment(commentId, holder.title.getText().toString());
                markCommentEnd(v, holder);
            }
        });
    }

    private void setupCancelEditComment(final CommentViewHolder holder)
    {
        holder.cancelEditComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                markCommentEnd(v, holder);
            }
        });
    }

    private void setupDeleteComment(final long commentId, CommentViewHolder holder)
    {
        holder.deleteComment.setVisibility(View.VISIBLE);
        holder.deleteComment.setOnClickListener(new View.OnClickListener()
        {
            private OnClickListener listener = new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                        startServiceForDelComment(commentId);
                }
            };

            @Override
            public void onClick(View v)
            {
                DialogBuilder.yesNoDialog(getActivity(), R.string.sureQuestion, listener).show();
            }
        });
    }

    private void prepareForEditText(CommentViewHolder holder, boolean edit)
    {
        holder.title.setEnabled(edit);
        holder.title.setClickable(edit);
        holder.title.setFocusable(edit);
        holder.title.setFocusableInTouchMode(edit);

        holder.editComment.setVisibility(edit ? View.GONE : View.VISIBLE);
        holder.deleteComment.setVisibility(edit ? View.GONE : View.VISIBLE);
        holder.finishEditComment.setVisibility(edit ? View.VISIBLE : View.GONE);
        holder.cancelEditComment.setVisibility(edit ? View.VISIBLE : View.GONE);
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
                            WritePermission.PREF_SECS_BETWEEN_COMMENT_WRITE, 0);
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
        progressDialog.dismiss();

        switch (resultCode)
        {
            case WriteIntentService.ACTION_ADD_COMMENT:
                Log.d(TAG, "Receiver invoked for ACTION_ADD_COMMENT");
                onAddCommentComplete(resultData);
                break;
            case WriteIntentService.ACTION_EDIT_COMMENT:
                Log.d(TAG, "Receiver invoked for ACTION_EDIT_COMMENT");
                onEditCommentComplete(resultData);
                break;
            case WriteIntentService.ACTION_DEL_COMMENT:
                Log.d(TAG, "Receiver invoked for ACTION_DEL_COMMENT");
                onDelCommentComplete(resultData);
                break;
            case WriteIntentService.ERROR:
                displayErrorToast(resultData);
                break;
        }
    }

    private void displayErrorToast(Bundle resultData)
    {
        AbstractHttpException e = (AbstractHttpException) resultData.getSerializable(StringConstants.EXCEPTION);
        String errorMsg = "Request failed for unknown reason";
        if (e != null)
        {
            StackXError stackXError = StackXError.deserialize(e.getMessage());
            if (stackXError != null && stackXError.msg != null)
                errorMsg = stackXError.msg;
        }
        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
    }

    private void onAddCommentComplete(Bundle resultData)
    {
        Comment comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
        if (comment != null && onCommentChangeListener != null)
        {
            comments.add(comment);
            onCommentChangeListener.onCommentAdd(comment);
            itemListAdapter.notifyDataSetChanged();
        }
    }

    private void onEditCommentComplete(Bundle resultData)
    {
        Comment comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);

        if (comment != null && onCommentChangeListener != null)
        {
            onCommentChangeListener.onCommentUpdate(comment);

            int idx = comments.indexOf(comment);

            if (idx != -1)
            {
                comments.remove(idx);
                comments.add(idx, comment);
            }

            itemListAdapter.notifyDataSetChanged();
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

    private void markCommentEnd(View v, CommentViewHolder holder)
    {
        AppUtils.hideSoftInput(getActivity(), v);
        prepareForEditText(holder, false);
        holder.title.clearFocus();
        itemsContainer.requestFocus();
    }

    public boolean onBackPressed()
    {
        dismissAnotherReplyCommentEditTextIfExist();
        postCommentFragmentContainer.setVisibility(View.GONE);
        selectedViewForReply = null;
        return true;
    }

    private void dismissAnotherReplyCommentEditTextIfExist()
    {
        if (selectedViewForReply != null)
        {
            selectedViewForReply.viewGroup.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
            selectedViewForReply.replyToComment.setVisibility(View.VISIBLE);
        }
    }

    private void displayPostCommentFragment(long id, String draftText)
    {
        postCommentFragment = (PostCommentFragment) getFragmentManager().findFragmentByTag("postCommentFragment");

        if (postCommentFragment == null)
            addPostCommentFragment();

        postCommentFragment.setPostId(id);
        postCommentFragment.setDraftText(draftText);
        postCommentFragment.setResultReceiver(resultReceiver);
        if (postCommentFragment.isVisible())
            postCommentFragment.refreshView();
        postCommentFragmentContainer.setVisibility(View.VISIBLE);
    }

    private void addPostCommentFragment()
    {
        postCommentFragment = new PostCommentFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.post_comment_fragment_container, postCommentFragment, "postCommentFragment");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}

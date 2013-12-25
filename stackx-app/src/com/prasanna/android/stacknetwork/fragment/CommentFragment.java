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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.sqlite.WritePermissionDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.DialogBuilder;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class CommentFragment extends ItemListFragment<Comment> implements ListItemView<Comment> {
  private static final String TAG = CommentFragment.class.getSimpleName();
  private ArrayList<Comment> comments;
  private HashMap<ObjectType, WritePermission> writePermissions;
  private ProgressDialog progressDialog;
  private OnCommentChangeListener onCommentChangeListener;
  private RestQueryResultReceiver addCommentResultReceiver;
  private LinearLayout postCommentFragmentContainer;
  private PostCommentFragment postCommentFragment;
  private CommentViewHolder selectedViewForReply;

  public interface OnShowCommentsListener {
    void onShowComments();
  }

  public interface OnCommentChangeListener {
    void onCommentAdd(Comment comment);

    void onCommentUpdate(Comment comment);

    void onCommentDelete(long commentId);
  }

  static class CommentViewHolder {
    long id;
    RelativeLayout viewGroup;
    TextView score;
    TextView title;
    TextView owner;
    ImageView editComment;
    ImageView replyToComment;
    ImageView deleteComment;
    LinearLayout commentEditOptions;
    RelativeLayout commentWriteOptions;
  }

  public void setOnCommentChangeListener(OnCommentChangeListener onCommentChangeListener) {
    this.onCommentChangeListener = onCommentChangeListener;
  }

  public void setComments(ArrayList<Comment> comments) {
    this.comments = comments;
  }

  public void setResultReceiver(RestQueryResultReceiver resultReceiver) {
    this.addCommentResultReceiver = resultReceiver;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (itemsContainer == null) {
      if (comments == null)
        comments = new ArrayList<Comment>();
      itemsContainer = (ViewGroup) inflater.inflate(R.layout.comment_list_view, container, false);
      postCommentFragmentContainer = (LinearLayout) itemsContainer.findViewById(R.id.post_comment_fragment_container);
      itemListAdapter = new ItemListAdapter<Comment>(getActivity(), R.layout.comment, comments, this);
    }

    return itemsContainer;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    getListView().setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    getListView().setItemsCanFocus(true);
    getListView().setClickable(false);
    getWritePermissions();
  }

  private void getWritePermissions() {
    WritePermissionDAO writePermissionDAO = new WritePermissionDAO(getActivity());
    try {
      writePermissionDAO.open();
      writePermissions = writePermissionDAO.getPermissions(OperatingSite.getSite().apiSiteParameter);
    } catch (SQLException e) {
      LogWrapper.d(TAG, e.getMessage());
    } finally {
      writePermissionDAO.close();
    }
  }

  @Override
  protected String getReceiverExtraName() {
    return StringConstants.COMMENTS;
  }

  @Override
  protected void startIntentService() {
  }

  @Override
  protected String getLogTag() {
    return TAG;
  }

  @Override
  public View getView(final Comment comment, int position, View convertView, ViewGroup parent) {
    RelativeLayout commentLayout = (RelativeLayout) convertView;
    CommentViewHolder holder;
    if (commentLayout == null) {
      commentLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.comment, null);
      holder = new CommentViewHolder();
      holder.viewGroup = commentLayout;
      holder.score = (TextView) commentLayout.findViewById(R.id.commentScore);
      holder.title = (TextView) commentLayout.findViewById(R.id.commentTitle);
      holder.owner = (TextView) commentLayout.findViewById(R.id.commentOwner);
      holder.editComment = (ImageView) commentLayout.findViewById(R.id.editComment);
      holder.replyToComment = (ImageView) commentLayout.findViewById(R.id.replyToComment);
      holder.deleteComment = (ImageView) commentLayout.findViewById(R.id.deleteComment);
      holder.commentWriteOptions = (RelativeLayout) commentLayout.findViewById(R.id.commentWriteOptions);
      holder.commentEditOptions = (LinearLayout) commentLayout.findViewById(R.id.commentEditOptions);

      commentLayout.setTag(holder);
    } else
      holder = (CommentViewHolder) commentLayout.getTag();

    holder.id = comment.id;
    holder.score.setText(AppUtils.formatNumber(comment.score));
    holder.title.setText("");
    holder.title.append(Html.fromHtml(comment.body));
    holder.owner.setText(DateTimeUtils.getElapsedDurationSince(comment.creationDate) + " by "
        + Html.fromHtml(comment.owner.getDisplayName()));

    if (AppUtils.inAuthenticatedRealm(getActivity()))
      setupCommentWriteOptions(comment, position, holder);

    if (selectedViewForReply != null && selectedViewForReply.id == comment.id) {
      selectedViewForReply.viewGroup.setBackgroundColor(getResources().getColor(R.color.lightGrey));
      selectedViewForReply.replyToComment.setVisibility(View.GONE);
    } else {
      holder.viewGroup.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
      if (!isMyComment(comment))
        holder.replyToComment.setVisibility(View.VISIBLE);
    }

    return commentLayout;
  }

  private void setupCommentWriteOptions(final Comment comment, final int position, final CommentViewHolder holder) {
    boolean myComment = isMyComment(comment);

    holder.commentWriteOptions.setVisibility(View.VISIBLE);

    if (canAddComment() && !myComment)
      setupReplyToComment(comment, position, holder);
    else {
      if (myComment)
        setupMyCommentOptions(comment, position, holder);
    }
  }

  private boolean isMyComment(Comment comment) {
    long userId = OperatingSite.getSite().userId;
    if (userId > 0)
      return (comment.owner != null && comment.owner.id == userId);

    return false;
  }

  private void setupReplyToComment(final Comment comment, final int position, final CommentViewHolder holder) {
    holder.replyToComment.setVisibility(View.VISIBLE);
    holder.replyToComment.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismissAnotherReplyCommentEditTextIfExist();

        selectedViewForReply = holder;
        selectedViewForReply.replyToComment.setVisibility(View.GONE);
        selectedViewForReply.viewGroup.setBackgroundColor(getResources().getColor(R.color.lightGrey));
        LogWrapper.d(TAG, "Select comment for reply: " + selectedViewForReply.id);

        String draftText =
            comment.owner.isRegistered() ? "@" + comment.owner.getDisplayName().replaceAll("\\s", "") : "";
        displayPostCommentFragment(comment.post_id, comment.id, draftText, false, addCommentResultReceiver);
        getListView().smoothScrollToPositionFromTop(position, 0);
      }
    });
  }

  private void setupMyCommentOptions(Comment comment, int position, CommentViewHolder holder) {
    holder.commentEditOptions.setVisibility(View.VISIBLE);

    if (canEditComment())
      setupEditComment(comment, position, holder);

    if (canDelComment())
      setupDeleteComment(comment.id, holder);
  }

  private void setupEditComment(final Comment comment, final int position, final CommentViewHolder holder) {
    holder.editComment.setVisibility(View.VISIBLE);
    holder.editComment.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        displayPostCommentFragment(comment.post_id, comment.id, comment.body, true, resultReceiver);
        getListView().smoothScrollToPositionFromTop(position, 0);
        AppUtils.showSoftInput(getActivity(), holder.title);
      }
    });
  }

  private void setupDeleteComment(final long commentId, CommentViewHolder holder) {
    holder.deleteComment.setVisibility(View.VISIBLE);
    holder.deleteComment.setOnClickListener(new View.OnClickListener() {
      private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (which == DialogInterface.BUTTON_POSITIVE)
            startServiceForDelComment(commentId);
        }
      };

      @Override
      public void onClick(View v) {
        DialogBuilder.yesNoDialog(getActivity(), R.string.sureQuestion, listener).show();
      }
    });
  }

  private boolean canAddComment() {
    return (isValid(ObjectType.COMMENT) && writePermissions.get(ObjectType.COMMENT).canAdd);
  }

  private boolean canEditComment() {
    return (isValid(ObjectType.COMMENT) && writePermissions.get(ObjectType.COMMENT).canEdit);
  }

  private boolean canDelComment() {
    return (isValid(ObjectType.COMMENT) && writePermissions.get(ObjectType.COMMENT).canDelete);
  }

  private boolean isValid(ObjectType objectType) {
    return objectType != null && writePermissions != null && writePermissions.containsKey(objectType);
  }

  public boolean hasNoComments() {
    return comments == null || comments.isEmpty();
  }

  private void removeSelf() {
    getFragmentManager().popBackStackImmediate();
  }

  private void startServiceForDelComment(long commentId) {
    progressDialog = new ProgressDialog(getActivity(), R.style.dialogNoText);
    progressDialog.show();

    Intent intent = new Intent(getActivity(), WriteIntentService.class);
    intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
    intent.putExtra(StringConstants.COMMENT_ID, commentId);
    intent.putExtra(StringConstants.ACTION, WriteIntentService.ACTION_DEL_COMMENT);
    startService(intent);
  }

  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    serviceRunning = false;
    if (progressDialog != null && progressDialog.isShowing())
      progressDialog.dismiss();

    switch (resultCode) {
      case WriteIntentService.ACTION_ADD_COMMENT:
        LogWrapper.d(TAG, "Receiver invoked for ACTION_ADD_COMMENT");
        onAddCommentComplete(resultData);
        break;
      case WriteIntentService.ACTION_EDIT_COMMENT:
        LogWrapper.d(TAG, "Receiver invoked for ACTION_EDIT_COMMENT");
        onEditCommentComplete(resultData);
        dismissPostCommentFragment();
        break;
      case WriteIntentService.ACTION_DEL_COMMENT:
        LogWrapper.d(TAG, "Receiver invoked for ACTION_DEL_COMMENT");
        onDelCommentComplete(resultData);
        break;
      case WriteIntentService.ERROR:
        displayError(resultData);
        break;
    }
  }

  private void displayError(Bundle resultData) {
    int requestCode = resultData.getInt(StringConstants.REQUEST_CODE, -1);
    if (requestCode == WriteIntentService.ACTION_ADD_COMMENT) {
      HttpException e = (HttpException) resultData.getSerializable(StringConstants.EXCEPTION);
      String errorMsg = "Request failed for unknown reason";
      if (e != null)
        postCommentFragment.setSendError(e.getErrorResponse());
      else
        postCommentFragment.setSendError(errorMsg);
    }
  }

  private void onAddCommentComplete(Bundle resultData) {
    Comment comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
    if (comment != null && onCommentChangeListener != null) {
      comments.add(comment);
      onCommentChangeListener.onCommentAdd(comment);
      itemListAdapter.notifyDataSetChanged();
    }
  }

  private void onEditCommentComplete(Bundle resultData) {
    Comment comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);

    if (comment != null && onCommentChangeListener != null) {
      onCommentChangeListener.onCommentUpdate(comment);

      int idx = comments.indexOf(comment);

      if (idx != -1) {
        comments.remove(idx);
        comments.add(idx, comment);
      }

      itemListAdapter.notifyDataSetChanged();
    }
  }

  private void onDelCommentComplete(Bundle resultData) {
    if (onCommentChangeListener != null) {
      onCommentChangeListener.onCommentDelete(resultData.getLong(StringConstants.COMMENT_ID));

      itemListAdapter.notifyDataSetChanged();
      if (comments.isEmpty())
        removeSelf();

    }
  }

  public boolean onBackPressed() {
    dismissAnotherReplyCommentEditTextIfExist();
    postCommentFragmentContainer.setVisibility(View.GONE);
    selectedViewForReply = null;
    return true;
  }

  private void dismissAnotherReplyCommentEditTextIfExist() {
    if (selectedViewForReply != null) {
      selectedViewForReply.viewGroup.setBackgroundResource(R.drawable.rounded_border_grey_min_padding);
      selectedViewForReply.replyToComment.setVisibility(View.VISIBLE);
    }
  }

  private void displayPostCommentFragment(long postId, long commenId, String draftText, boolean myEdit,
      RestQueryResultReceiver receiver) {
    postCommentFragment = (PostCommentFragment) getFragmentManager().findFragmentByTag("postCommentFragment");

    if (postCommentFragment == null)
      addPostCommentFragment();

    postCommentFragment.setPostId(postId);
    postCommentFragment.setCommentId(commenId);
    postCommentFragment.setDraftText(draftText);
    postCommentFragment.setResultReceiver(receiver);
    postCommentFragment.setMyEdit(myEdit);
    if (myEdit)
      postCommentFragment.setTitle("Editing");
    else
      postCommentFragment.setTitle("Replying to comment");

    if (postCommentFragment.isVisible())
      postCommentFragment.refreshView();

    postCommentFragmentContainer.setVisibility(View.VISIBLE);
  }

  private void addPostCommentFragment() {
    postCommentFragment = new PostCommentFragment();
    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.post_comment_fragment_container, postCommentFragment, "postCommentFragment");
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  private void dismissPostCommentFragment() {
    if (postCommentFragment != null) {
      postCommentFragment.hideSoftKeyboard();
      getFragmentManager().popBackStackImmediate();
      postCommentFragment = null;
    }
  }

  @Override
  protected void loadNextPage() {

  }

}

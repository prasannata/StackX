/*
    Copyright (C) 2014 Prasanna Thirumalai
    
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

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.VotablePost;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.AbstractIntentService;
import com.prasanna.android.stacknetwork.service.AbstractIntentService.VotingActions;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractVotableFragment extends Fragment implements StackXRestQueryResultReceiver {
  private boolean serviceRunningForUpDownVote = false;
  private ImageView upvoteIv;
  private ImageView downVoteIv;
  private VotablePost votablePost;

  protected RestQueryResultReceiver resultReceiver;

  protected abstract void onScoreChange(int newScore);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (resultReceiver == null) resultReceiver = new RestQueryResultReceiver(new Handler());
    resultReceiver.setReceiver(this);
  }

  protected void prepareUpDownVote(final VotablePost votablePost, final ViewGroup parentView,
      final Class<? extends AbstractIntentService> intentServiceClass) {
    this.votablePost = votablePost;

    upvoteIv = (ImageView) parentView.findViewById(R.id.upVote);
    AppUtils.prepareUpvote(votablePost.upvoted, upvoteIv);
    upvoteIv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startUpvoteOrDownvoteService(votablePost, true, intentServiceClass);
      }
    });

    downVoteIv = (ImageView) parentView.findViewById(R.id.downVote);
    AppUtils.prepareDownvote(votablePost.downvoted, downVoteIv);
    downVoteIv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startUpvoteOrDownvoteService(votablePost, false, intentServiceClass);
      }
    });
  }

  private void startUpvoteOrDownvoteService(final VotablePost votablePost, final boolean upvote,
      Class<? extends AbstractIntentService> intentServiceClass) {
    if (!serviceRunningForUpDownVote) {
      Intent intent = new Intent(getActivity().getApplicationContext(), intentServiceClass);

      if (upvote && votablePost.upvoted && !votablePost.downvoted) {
        intent.putExtra(StringConstants.ACTION, VotingActions.UPVOTE_UNDO);
      } else if (!upvote && votablePost.downvoted && !votablePost.upvoted) {
        intent.putExtra(StringConstants.ACTION, VotingActions.DOWNVOTE_UNDO);
      } else if (upvote && votablePost.downvoted && upvote) {
        intent.putExtra(StringConstants.ACTION, VotingActions.DOWNVOTE_UNDO_UPVOTE);
      } else if (!upvote && votablePost.upvoted) {
        intent.putExtra(StringConstants.ACTION, VotingActions.UPVOTE_UNDO_DOWNVOTE);
      } else if (upvote) {
        intent.putExtra(StringConstants.ACTION, VotingActions.UPVOTE);
      } else if (!upvote) {
        intent.putExtra(StringConstants.ACTION, VotingActions.DOWNVOTE);
      }

      intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
      intent.putExtra(StringConstants.SITE, getActivity().getIntent().getStringExtra(StringConstants.SITE));
      intent.putExtra(StringConstants.ID, votablePost.id);

      getActivity().setProgressBarIndeterminateVisibility(true);
      getActivity().startService(intent);
      serviceRunningForUpDownVote = true;
    }
  }

  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    serviceRunningForUpDownVote = false;
    getActivity().setProgressBarIndeterminateVisibility(false);

    switch (resultCode) {
      case VotingActions.RESULT_CODE_UPVOTE_SUCCESS:
        votablePost.upvoted = true;
        upvoteIv.setImageResource(R.drawable.arrow_up_orange);
        onScoreChange(resultData.getInt(StringConstants.SCORE));
        break;
      case VotingActions.RESULT_CODE_UPVOTE_UNDO_SUCCESS:
        votablePost.upvoted = false;
        upvoteIv.setImageResource(R.drawable.arrow_up);
        onScoreChange(resultData.getInt(StringConstants.SCORE));
        break;
      case VotingActions.RESULT_CODE_DOWNVOTE_SUCCESS:
        votablePost.downvoted = true;
        downVoteIv.setImageResource(R.drawable.arrow_down_orange);
        onScoreChange(resultData.getInt(StringConstants.SCORE));
        break;
      case VotingActions.RESULT_CODE_DOWNVOTE_UNDO_SUCCESS:
        votablePost.downvoted = false;
        downVoteIv.setImageResource(R.drawable.arrow_down);
        onScoreChange(resultData.getInt(StringConstants.SCORE));
        break;
      case VotingActions.RESULT_CODE_UPVOTE_UNDO_DOWNVOTE_SUCCESS:
        votablePost.downvoted = true;
        votablePost.upvoted = false;
        upvoteIv.setImageResource(R.drawable.arrow_up);
        downVoteIv.setImageResource(R.drawable.arrow_down_orange);
        onScoreChange(resultData.getInt(StringConstants.SCORE));
        break;
      case VotingActions.RESULT_CODE_DOWNVOTE_UNDO_UPVOTE_SUCCESS:
        votablePost.upvoted = true;
        votablePost.downvoted = false;
        upvoteIv.setImageResource(R.drawable.arrow_up_orange);
        downVoteIv.setImageResource(R.drawable.arrow_down);
        onScoreChange(resultData.getInt(StringConstants.SCORE));
        break;
      default:
        break;
    }
  }
}

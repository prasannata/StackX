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

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Reputation;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class ReputationHistoryFragment extends ItemListFragment<Reputation> implements ListItemView<Reputation> {
  private static final String TAG = ReputationHistoryFragment.class.getSimpleName();
  private int page = 1;
  private final ArrayList<Reputation> reptuationHistory = new ArrayList<Reputation>();

  static class ReputationViewHolder {
    TextView repChangeTv;
    TextView repChangeTypeTv;
    TextView postTitleTv;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (itemsContainer == null) {
      itemsContainer = (ViewGroup) inflater.inflate(R.layout.reputation_history, container, false);
      itemListAdapter = new ItemListAdapter<Reputation>(getActivity(), R.layout.reputation, reptuationHistory, this);
    }

    return itemsContainer;
  }

  @Override
  public void onResume() {
    super.onResume();

    if (itemListAdapter != null && itemListAdapter.getCount() == 0) startIntentService();
  }

  @Override
  protected String getReceiverExtraName() {
    return StringConstants.REP_HISTORY;
  }

  @Override
  protected void loadNextPage() {
    startIntentService();
  }

  @Override
  protected void startIntentService() {
    showProgressBar();

    Intent intent = getIntentForService(UserIntentService.class, null);
    if (intent != null) {
      intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_REP_HISTORY_FULL);
      intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
      intent.putExtra(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
      intent.putExtra(StringConstants.PAGE, page++);
      intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

      startService(intent);
    }
  }

  @Override
  protected String getLogTag() {
    return TAG;
  }

  @Override
  public View getView(Reputation item, int position, View convertView, ViewGroup parent) {
    ReputationViewHolder holder;
    if (convertView == null) {
      convertView = getActivity().getLayoutInflater().inflate(R.layout.reputation, null);
      holder = new ReputationViewHolder();
      holder.repChangeTv = (TextView) convertView.findViewById(R.id.repChange);
      holder.repChangeTypeTv = (TextView) convertView.findViewById(R.id.repChangeType);
      holder.postTitleTv = (TextView) convertView.findViewById(R.id.postTitle);
      convertView.setTag(holder);
    } else holder = (ReputationViewHolder) convertView.getTag();

    holder.repChangeTypeTv.setText(item.reputationHistoryType.getDisplayText());

    if (item.reputationChange > 0) {
      holder.repChangeTv.setTextColor(R.color.positiveRepChange);
      holder.repChangeTypeTv.setText("+" + item.reputationHistoryType.getDisplayText());
    } else {
      holder.repChangeTv.setTextColor(R.color.negativeRepChange);
      holder.repChangeTypeTv.setText("-" + item.reputationHistoryType.getDisplayText());
    }

    holder.repChangeTv.setText(String.valueOf(item.reputationChange));

    return convertView;
  }

}

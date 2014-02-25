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
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Reputation;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.sqlite.ProfileDAO;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.AsyncTaskExecutor;
import com.prasanna.android.task.GetImageAsyncTask;
import com.prasanna.android.utils.LogWrapper;

public class UserProfileFragment extends Fragment implements StackXRestQueryResultReceiver, ListItemView<Reputation> {
  private static final String TAG = UserProfileFragment.class.getSimpleName();
  private ViewGroup profileHomeLayout;
  private Intent userProfileIntent;
  private User user;
  private boolean me = false;
  private RestQueryResultReceiver resultReceiver;
  private ArrayList<Reputation> repHistory = new ArrayList<Reputation>();
  private ItemListAdapter<Reputation> itemListAdapter;
  private long userId;
  private boolean forceRefresh;
  private Site site;
  private Context appContext;
  private ListView repHistoryView;
  private int repHistoryPage = 1;
  private ProgressBar progressBar;
  private StackXPage<Reputation> repHistPage;
  private AtomicBoolean repHistoryServiceRunning = new AtomicBoolean(false);

  static class ReputationViewHolder {
    TextView repChangeTv;
    TextView repChangeTypeTv;
    TextView postTitleTv;
  }

  static class PersistMyAvatarAsyncTask extends AsyncTask<Bitmap, Void, Void> {
    private Context context;

    public PersistMyAvatarAsyncTask(Context context) {
      this.context = context;
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
      ProfileDAO profileDAO = new ProfileDAO(context);
      try {
        profileDAO.open();
        profileDAO.updateMyAvatar(OperatingSite.getSite().apiSiteParameter, params[0]);
      } catch (SQLException e) {
        LogWrapper.e(TAG, e.getMessage());
      } finally {
        profileDAO.close();
      }
      return null;
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    appContext = activity.getApplicationContext();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    resultReceiver = new RestQueryResultReceiver(new Handler());
    resultReceiver.setReceiver(this);

    if (savedInstanceState != null) {
      me = savedInstanceState.getBoolean(StringConstants.ME);
      userId = savedInstanceState.getLong(StringConstants.USER_ID);
      site = (Site) savedInstanceState.getSerializable(StringConstants.SITE);
      forceRefresh = false;
    } else {
      me = getActivity().getIntent().getBooleanExtra(StringConstants.ME, false);
      userId = getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L);
      site = (Site) getActivity().getIntent().getSerializableExtra(StringConstants.SITE);
      forceRefresh = getActivity().getIntent().getBooleanExtra(StringConstants.REFRESH, false);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (profileHomeLayout == null) {
      profileHomeLayout = (ViewGroup) inflater.inflate(R.layout.user_proile_layout, container, false);
      repHistoryView = (ListView) profileHomeLayout.findViewById(android.R.id.list);
      repHistoryView.addFooterView(getProgressBar());
      repHistoryView.setOnScrollListener(new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
          if (!repHistoryServiceRunning.get() && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1)) {
            if (repHistPage != null && repHistPage.hasMore) startRepHistoryService();
          }
        }
      });

      itemListAdapter = new ItemListAdapter<Reputation>(getActivity(), R.layout.reputation, repHistory, this);
      repHistoryView.setAdapter(itemListAdapter);
    }

    return profileHomeLayout;
  }

  @Override
  public void onResume() {
    super.onResume();

    if (user == null) {
      startUserProfileService();
      if (me) startRepHistoryService();
    } else {
      showUserDetail();
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(StringConstants.ME, me);
    outState.putSerializable(StringConstants.SITE, site);
    outState.putLong(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
    super.onSaveInstanceState(outState);
  }

  private void startUserProfileService() {
    getActivity().setProgressBarIndeterminateVisibility(true);
    userProfileIntent = new Intent(getActivity(), UserIntentService.class);
    userProfileIntent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_PROFILE);
    userProfileIntent.putExtra(StringConstants.ME, me);
    userProfileIntent.putExtra(StringConstants.USER_ID, userId);
    userProfileIntent.putExtra(StringConstants.REFRESH, forceRefresh);
    userProfileIntent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
    userProfileIntent.putExtra(StringConstants.SITE, site.apiSiteParameter);
    getActivity().startService(userProfileIntent);
  }

  private void startRepHistoryService() {
    Intent intent = new Intent(getActivity(), UserIntentService.class);
    if (intent != null) {
      intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_REP_HISTORY_FULL);
      intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
      intent.putExtra(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
      intent.putExtra(StringConstants.PAGE, repHistoryPage++);
      intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
      progressBar.setVisibility(View.VISIBLE);
      repHistoryServiceRunning.getAndSet(true);
      getActivity().startService(intent);
    }
  }

  private void showUserDetail() {
    if (user != null && profileHomeLayout != null) {
      if (isAdded()) getActivity().getActionBar().setTitle(Html.fromHtml(user.getDisplayName()) + "'s profile");

      profileHomeLayout.findViewById(R.id.userProfile).setVisibility(View.VISIBLE);

      showProfileInfo();
      showItemsCount();
      getAndDisplayUserAvatar();
    }
  }

  private void showProfileInfo() {
    TextView textView = (TextView) profileHomeLayout.findViewById(R.id.profileDisplayName);
    textView.setText(Html.fromHtml(user.getDisplayName()));

    textView = (TextView) profileHomeLayout.findViewById(R.id.registerDate);
    textView.setText(getString(R.string.registered) + " " + DateTimeUtils.getElapsedDurationSince(user.creationDate));

    textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserReputation);
    textView.setText(AppUtils.formatReputation(user.reputation));

    textView = (TextView) profileHomeLayout.findViewById(R.id.profileViews);
    textView.setText(getString(R.string.views) + " " + user.profileViews);

    textView = (TextView) profileHomeLayout.findViewById(R.id.profileAcceptRate);
    if (user.acceptRate > -1) textView.setText(getString(R.string.acceptRate) + " " + user.acceptRate + "%");
    else textView.setVisibility(View.GONE);

    textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserLastSeen);
    textView.setText(getString(R.string.lastSeen) + " " + DateTimeUtils.getElapsedDurationSince(user.lastAccessTime));

    showBadgesCount();
  }

  private void showBadgesCount() {
    if (user.badgeCounts != null && user.badgeCounts.length == 3) {
      TextView textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserGoldNum);
      textView.setText(String.valueOf(user.badgeCounts[0]));

      textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserSilverNum);
      textView.setText(String.valueOf(user.badgeCounts[1]));

      textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserBronzeNum);
      textView.setText(String.valueOf(user.badgeCounts[2]));
    }
  }

  private void showItemsCount() {
    TextView textView = (TextView) profileHomeLayout.findViewById(R.id.questionCount);
    textView.setText(getString(R.string.questions) + " " + String.valueOf(user.questionCount));

    textView = (TextView) profileHomeLayout.findViewById(R.id.answerCount);
    textView.setText(getString(R.string.answers) + " " + String.valueOf(user.answerCount));

    textView = (TextView) profileHomeLayout.findViewById(R.id.upvoteCount);
    textView.setText(getString(R.string.upvotes) + " " + String.valueOf(user.upvoteCount));

    textView = (TextView) profileHomeLayout.findViewById(R.id.downvoteCount);
    textView.setText(getString(R.string.downvotes) + " " + String.valueOf(user.downvoteCount));
  }

  private void getAndDisplayUserAvatar() {
    if (user.avatar == null) runAsyncTaskToGetAvatar();
    else displayAvatar(user.avatar);
  }

  private void runAsyncTaskToGetAvatar() {
    final ProgressBar avatarProgressBar = (ProgressBar) profileHomeLayout.findViewById(R.id.getAvatarProgressBar);
    avatarProgressBar.setVisibility(View.VISIBLE);

    AsyncTaskCompletionNotifier<Bitmap> imageFetchAsyncTaskCompleteNotiferImpl =
        new AsyncTaskCompletionNotifier<Bitmap>() {
          @Override
          public void notifyOnCompletion(Bitmap result) {
            displayAvatar(result);
            avatarProgressBar.setVisibility(View.GONE);

            if (me) new PersistMyAvatarAsyncTask(appContext).execute(result);
          }

        };

    AsyncTaskExecutor.getInstance().executeInThreadPoolExecutor(
        new GetImageAsyncTask(imageFetchAsyncTaskCompleteNotiferImpl), user.profileImageLink);
  }

  private void displayAvatar(Bitmap result) {
    ImageView userProfileImage = (ImageView) profileHomeLayout.findViewById(R.id.profileUserImage);
    userProfileImage.setVisibility(View.VISIBLE);
    userProfileImage.setImageBitmap(result);
  }

  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    if (isAdded() && isVisible()) getActivity().setProgressBarIndeterminateVisibility(false);

    switch (resultCode) {
      case UserIntentService.GET_USER_PROFILE:
        showUserProfile(resultData);
        break;
      case UserIntentService.GET_USER_REP_HISTORY_FULL:
        showRepHistory(resultData);
        break;
      case UserIntentService.ERROR:
        ViewGroup errorView =
            AppUtils.getErrorView(getActivity(), (HttpException) resultData.getSerializable(StringConstants.EXCEPTION));
        profileHomeLayout.removeAllViews();
        profileHomeLayout.addView(errorView);
        break;
    }
  }

  @SuppressWarnings("unchecked")
  private void showUserProfile(Bundle resultData) {
    StackXPage<User> userPage = (StackXPage<User>) resultData.getSerializable(StringConstants.USER);
    if (isVisible() && userPage != null && userPage.items != null && !userPage.items.isEmpty()) {
      user = userPage.items.get(0);
      showUserDetail();
    }
  }

  @SuppressWarnings("unchecked")
  private void showRepHistory(Bundle resultData) {
    repHistoryServiceRunning.getAndSet(false);
    progressBar.setVisibility(View.GONE);
    repHistPage = (StackXPage<Reputation>) resultData.getSerializable(StringConstants.REP_HISTORY);
    if (repHistPage != null) {
      repHistory.addAll(repHistPage.items);
      itemListAdapter.notifyDataSetChanged();
    }
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
      holder.repChangeTv.setTextColor(getActivity().getResources().getColor(R.color.positiveRepChange));
      holder.repChangeTv.setText("+" + item.reputationChange);
    } else {
      holder.repChangeTv.setTextColor(getActivity().getResources().getColor(R.color.negativeRepChange));
      holder.repChangeTv.setText(String.valueOf(item.reputationChange));
    }

    if (item.postTitle != null) holder.postTitleTv.setText(Html.fromHtml(item.postTitle));
    else holder.postTitleTv.setText("Unknown post");

    return convertView;
  }

  private ProgressBar getProgressBar() {
    if (progressBar == null)
      progressBar =
          (ProgressBar) LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.progress_bar, null);

    return progressBar;
  }

}

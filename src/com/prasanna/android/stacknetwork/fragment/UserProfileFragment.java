/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.GetImageAsyncTask;
import com.prasanna.android.task.GetImageAsyncTaskCompleteNotifierImpl;

public class UserProfileFragment extends Fragment implements StackXRestQueryResultReceiver
{
    private static final String TAG = UserProfileFragment.class.getSimpleName();
    private RelativeLayout profileHomeLayout;
    private LinearLayout userAccountList;
    private ProgressDialog fetchProfileProgress;
    private int usersAccountCursor = 0;
    private Intent userProfileIntent;
    private User user;
    private boolean me = false;
    private RestQueryResultReceiver resultReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	Log.d(TAG, "onCreate");

	super.onCreate(savedInstanceState);

	resultReceiver = new RestQueryResultReceiver(new Handler());
	resultReceiver.setReceiver(this);

	if (user != null)
	{
	    me = getActivity().getIntent().getBooleanExtra(StringConstants.ME, false);
	    if (me)
		user = SharedPreferencesUtil.getMe(getActivity().getCacheDir());
	    user.accounts = SharedPreferencesUtil.getMeAccounts(getActivity().getCacheDir());
	}
	else
	{
	    startUserProfileService();
	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	if (profileHomeLayout == null)
	{
	    usersAccountCursor = 0;

	    fetchProfileProgress = ProgressDialog.show(getActivity(), "", "Fetching profile");

	    profileHomeLayout = (RelativeLayout) inflater.inflate(R.layout.user_proile_layout, container, false);
	    userAccountList = (LinearLayout) profileHomeLayout.findViewById(R.id.accountsList);
	}
	else
	{
	    Log.d(TAG, "Not fetching, display existing user");
	    displayUserDetail();
	    displayUserAccounts();
	}

	return profileHomeLayout;
    }

    @Override
    public void onStop()
    {
	super.onStop();

	stopServiceAndUnregsiterReceivers();
    }

    @Override
    public void onDestroy()
    {
	super.onDestroy();

	stopServiceAndUnregsiterReceivers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
	if (user != null)
	{
	    outState.putSerializable(StringConstants.USER, user);
	}
	super.onSaveInstanceState(outState);
    }

    private void stopServiceAndUnregsiterReceivers()
    {
	if (userProfileIntent != null)
	{
	    getActivity().stopService(userProfileIntent);
	}
    }

    private void displayUserDetail()
    {
	if (user != null)
	{
	    updateProfileInfo();

	    TextView textView = (TextView) profileHomeLayout.findViewById(R.id.questionCount);
	    textView.setText(getString(R.string.questions) + " " + String.valueOf(user.questionCount));

	    textView = (TextView) profileHomeLayout.findViewById(R.id.answerCount);
	    textView.setText(getString(R.string.answers) + " " + String.valueOf(user.answerCount));

	    textView = (TextView) profileHomeLayout.findViewById(R.id.upvoteCount);
	    textView.setText(getString(R.string.upvotes) + " " + String.valueOf(user.upvoteCount));

	    textView = (TextView) profileHomeLayout.findViewById(R.id.downvoteCount);
	    textView.setText(getString(R.string.downvotes) + " " + String.valueOf(user.downvoteCount));
	}
    }

    private void updateProfileInfo()
    {
	ImageView userProfileImage = (ImageView) profileHomeLayout.findViewById(R.id.profileUserImage);
	if (userProfileImage.getDrawable() == null)
	{
	    GetImageAsyncTask fetchImageAsyncTask = new GetImageAsyncTask(new GetImageAsyncTaskCompleteNotifierImpl(
		            userProfileImage));
	    fetchImageAsyncTask.execute(user.profileImageLink);
	}

	TextView textView = (TextView) profileHomeLayout.findViewById(R.id.profileDisplayName);
	textView.setText(user.displayName);

	textView = (TextView) profileHomeLayout.findViewById(R.id.registerDate);
	textView.setText(getString(R.string.registered) + " "
	                + DateTimeUtils.getElapsedDurationSince(user.creationDate));

	textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserReputation);
	textView.setText(AppUtils.formatReputation(user.reputation));

	if (user.badgeCounts != null && user.badgeCounts.length == 3)
	{
	    textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserGoldNum);
	    textView.setText(String.valueOf(user.badgeCounts[0]));

	    textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserSilverNum);
	    textView.setText(String.valueOf(user.badgeCounts[1]));

	    textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserBronzeNum);
	    textView.setText(String.valueOf(user.badgeCounts[2]));
	}

	textView = (TextView) profileHomeLayout.findViewById(R.id.profileViews);
	textView.setText(getString(R.string.views) + " " + user.profileViews);

	if (user.acceptRate > 0)
	{
	    textView = (TextView) profileHomeLayout.findViewById(R.id.profileAcceptRate);
	    textView.setText(getString(R.string.acceptRate) + " " + user.acceptRate + "%");
	    textView.setVisibility(View.VISIBLE);
	}

	textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserLastSeen);
	textView.setText(getString(R.string.lastSeen) + " "
	                + DateTimeUtils.getElapsedDurationSince(user.lastAccessTime));
    }

    private void startUserProfileService()
    {
	userProfileIntent = new Intent(getActivity(), UserIntentService.class);
	userProfileIntent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_PROFILE);
	userProfileIntent.setAction(UserIntentAction.USER_DETAIL.getAction());
	userProfileIntent.putExtra(StringConstants.ME,
	                getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
	userProfileIntent.putExtra(StringConstants.USER_ID,
	                getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
	userProfileIntent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
	getActivity().startService(userProfileIntent);
    }

    private void displayUserAccounts()
    {
	if (user != null && user.accounts != null)
	{
	    for (; usersAccountCursor < user.accounts.size(); usersAccountCursor++)
	    {
		TextView textView = (TextView) getActivity().getLayoutInflater().inflate(
		                R.layout.textview_black_textcolor, null);
		textView.setText(user.accounts.get(usersAccountCursor).siteName);
		userAccountList.addView(textView);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
	if (fetchProfileProgress != null)
	    fetchProfileProgress.dismiss();

	StackXPage<User> userPage = (StackXPage<User>) resultData.getSerializable(StringConstants.USER);
	if (userPage != null && userPage.items != null && !userPage.items.isEmpty())
	{
	    user = userPage.items.get(0);
	    displayUserDetail();
	}

	HashMap<String, Account> accounts = (HashMap<String, Account>) resultData
	                .getSerializable(StringConstants.USER_ACCOUNTS);
	if (accounts != null)
	{
	    if (user.accounts == null)
		user.accounts = new ArrayList<Account>();

	    user.accounts.addAll(accounts.values());
	    displayUserAccounts();
	}
    }
}

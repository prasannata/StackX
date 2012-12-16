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

package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserDetailsIntentService;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.FetchImageAsyncTask;
import com.prasanna.android.task.ImageFetchAsyncTaskCompleteNotifierImpl;

public class UserProfileFragment extends Fragment
{
    private static final String TAG = UserProfileFragment.class.getSimpleName();
    private RelativeLayout profileHomeLayout;
    private LinearLayout userAccountList;
    private ProgressDialog fetchProfileProgress;
    private int usersAccountCursor = 0;
    private Intent userProfileIntent;
    private User user;

    private BroadcastReceiver userAccountsReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            HashMap<String, Account> accounts = (HashMap<String, Account>) intent
                    .getSerializableExtra(IntentActionEnum.UserIntentAction.USER_ACCOUNTS.getExtra());

            if (user != null && userAccountList != null && accounts != null)
            {
                if (user.accounts == null)
                {
                    user.accounts = new ArrayList<Account>();
                }

                user.accounts.addAll(accounts.values());

                displayUserAccounts();
            }
        }
    };

    private BroadcastReceiver userProfileReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            user = (User) intent.getSerializableExtra(IntentActionEnum.UserIntentAction.USER_DETAIL.getExtra());

            if (profileHomeLayout != null)
            {
                fetchProfileProgress.dismiss();
                displayUserDetail();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (user == null)
        {
            User intentForUser = (User) getActivity().getIntent().getSerializableExtra(StringConstants.USER);

            registerUserProfileReceiver();
            registerUserAccountsReceiver();

            startUserProfileService(intentForUser.id, intentForUser.accessToken);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        usersAccountCursor = 0;

        profileHomeLayout = (RelativeLayout) inflater.inflate(R.layout.user_proile_layout, container, false);
        userAccountList = (LinearLayout) profileHomeLayout.findViewById(R.id.accountsList);

        if (user == null)
        {
            fetchProfileProgress = ProgressDialog.show(getActivity(), "", "Fetching profile");
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

        try
        {
            getActivity().unregisterReceiver(userProfileReceiver);
            getActivity().unregisterReceiver(userAccountsReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    private void displayUserDetail()
    {
        if (user != null)
        {
            updateProfileInfo();

            TextView textView = (TextView) profileHomeLayout.findViewById(R.id.questionCount);
            textView.append(" " + String.valueOf(user.questionCount));

            textView = (TextView) profileHomeLayout.findViewById(R.id.answerCount);
            textView.append(" " + String.valueOf(user.answerCount));

            textView = (TextView) profileHomeLayout.findViewById(R.id.upvoteCount);
            textView.append(" " + String.valueOf(user.upvoteCount));

            textView = (TextView) profileHomeLayout.findViewById(R.id.downvoteCount);
            textView.append(" " + String.valueOf(user.downvoteCount));
        }
    }

    private void updateProfileInfo()
    {
        ImageView userProfileImage = (ImageView) profileHomeLayout.findViewById(R.id.profileUserImage);
        if (userProfileImage.getDrawable() == null)
        {
            FetchImageAsyncTask fetchImageAsyncTask = new FetchImageAsyncTask(
                    new ImageFetchAsyncTaskCompleteNotifierImpl(userProfileImage));
            fetchImageAsyncTask.execute(user.profileImageLink);
        }

        TextView textView = (TextView) profileHomeLayout.findViewById(R.id.profileDisplayName);
        textView.setText(user.displayName);

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
        textView.append(" " + user.profileViews);

        textView = (TextView) profileHomeLayout.findViewById(R.id.profileUserLastSeen);
        textView.append(" " + DateTimeUtils.getElapsedDurationSince(user.lastAccessTime));
    }

    private void startUserProfileService(long userId, String accessToken)
    {
        userProfileIntent = new Intent(getActivity(), UserDetailsIntentService.class);
        userProfileIntent.setAction(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        userProfileIntent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
        getActivity().startService(userProfileIntent);
    }

    private void registerUserAccountsReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.USER_ACCOUNTS.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(userAccountsReceiver, filter);
    }

    private void registerUserProfileReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(userProfileReceiver, filter);
    }

    private void displayUserAccounts()
    {
        for (; usersAccountCursor < user.accounts.size(); usersAccountCursor++)
        {
            TextView textView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.textview_black_textcolor,
                    null);
            textView.setText(user.accounts.get(usersAccountCursor).siteName);
            userAccountList.addView(textView);
        }
    }
}

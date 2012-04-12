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

                for (; usersAccountCursor < user.accounts.size(); usersAccountCursor++)
                {
                    TextView textView = (TextView) getActivity().getLayoutInflater().inflate(
                            R.layout.textview_black_textcolor, null);
                    textView.setText(user.accounts.get(usersAccountCursor).siteName);
                    userAccountList.addView(textView);
                }
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
                displayUserDetail(profileHomeLayout);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        User intentForUser = (User) getActivity().getIntent().getSerializableExtra(StringConstants.USER);

        registerUserProfileReceiver();
        registerUserAccountsReceiver();

        startUserProfileService(intentForUser.id, intentForUser.accessToken);
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
            displayUserDetail(profileHomeLayout);
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

    private void displayUserDetail(RelativeLayout relativeLayout)
    {
        if (user != null)
        {
            updateProfileInfo(relativeLayout);

            TextView textView = (TextView) relativeLayout.findViewById(R.id.questionCount);
            textView.append(" " + String.valueOf(user.questionCount));

            textView = (TextView) relativeLayout.findViewById(R.id.answerCount);
            textView.append(" " + String.valueOf(user.answerCount));

            textView = (TextView) relativeLayout.findViewById(R.id.upvoteCount);
            textView.append(" " + String.valueOf(user.upvoteCount));

            textView = (TextView) relativeLayout.findViewById(R.id.downvoteCount);
            textView.append(" " + String.valueOf(user.downvoteCount));
        }
    }

    private void updateProfileInfo(RelativeLayout relativeLayout)
    {
        ImageView userProfileImage = (ImageView) relativeLayout.findViewById(R.id.profileUserImage);
        if (userProfileImage.getDrawable() == null)
        {
            FetchImageAsyncTask fetchImageAsyncTask = new FetchImageAsyncTask(
                    new ImageFetchAsyncTaskCompleteNotifierImpl(userProfileImage));
            fetchImageAsyncTask.execute(user.profileImageLink);
        }

        TextView textView = (TextView) relativeLayout.findViewById(R.id.profileDisplayName);
        textView.setText(user.displayName);

        textView = (TextView) relativeLayout.findViewById(R.id.profileUserReputation);
        textView.setText(AppUtils.formatReputation(user.reputation));

        if (user.badgeCounts != null && user.badgeCounts.length == 3)
        {
            textView = (TextView) relativeLayout.findViewById(R.id.profileUserGoldNum);
            textView.setText(String.valueOf(user.badgeCounts[0]));

            textView = (TextView) relativeLayout.findViewById(R.id.profileUserSilverNum);
            textView.setText(String.valueOf(user.badgeCounts[1]));

            textView = (TextView) relativeLayout.findViewById(R.id.profileUserBronzeNum);
            textView.setText(String.valueOf(user.badgeCounts[2]));
        }

        textView = (TextView) relativeLayout.findViewById(R.id.profileViews);
        textView.append(" " + user.profileViews);

        textView = (TextView) relativeLayout.findViewById(R.id.profileUserLastSeen);
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
}

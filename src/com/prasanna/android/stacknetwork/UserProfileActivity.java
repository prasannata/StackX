package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.intent.UserDetailsIntentService;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.FetchImageAsyncTask;
import com.prasanna.android.task.ImageFetchAsyncTaskCompleteNotifierImpl;

public class UserProfileActivity extends Activity
{
    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private Intent userIntent;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    User user = (User) intent
		    .getSerializableExtra(IntentActionEnum.UserIntentAction.USER_DETAIL.getExtra());
	    displayUserDetail(user);
	}
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.user_proile_layout);
	registerForUserServiceReceiver();
	startUserService();
    }

    @Override
    protected void onStop()
    {
	super.onStop();

	stopServiceAndUnregsiterReceiver();
    }

    @Override
    protected void onDestroy()
    {
	super.onDestroy();

	stopServiceAndUnregsiterReceiver();
    }

    private void stopServiceAndUnregsiterReceiver()
    {
	if (userIntent != null)
	{
	    stopService(userIntent);
	}

	try
	{
	    unregisterReceiver(broadcastReceiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    private void startUserService()
    {
	long userId = (long) getIntent().getLongExtra(StringConstants.USER_ID, -1);
	userIntent = new Intent(this, UserDetailsIntentService.class);
	userIntent.setAction(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
	userIntent.putExtra(StringConstants.USER_ID, userId);
	startService(userIntent);
    }

    private void registerForUserServiceReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(broadcastReceiver, filter);
    }

    private void displayUserDetail(User user)
    {
	if (user != null)
	{
	    updateProfileInfo(user);

	    TextView textView = (TextView) findViewById(R.id.questionCount);
	    textView.append(" " + String.valueOf(user.getQuestionCount()));

	    textView = (TextView) findViewById(R.id.answerCount);
	    textView.append(" " + String.valueOf(user.getAnswerCount()));

	    textView = (TextView) findViewById(R.id.upvoteCount);
	    textView.append(" " + String.valueOf(user.getUpvoteCount()));

	    textView = (TextView) findViewById(R.id.downvoteCount);
	    textView.append(" " + String.valueOf(user.getDownvoteCount()));
	}
    }

    private void updateProfileInfo(User user)
    {
	ImageView userProfileImage = (ImageView) findViewById(R.id.profileUserImage);
	FetchImageAsyncTask fetchImageAsyncTask = new FetchImageAsyncTask(
	        new ImageFetchAsyncTaskCompleteNotifierImpl(userProfileImage));
	fetchImageAsyncTask.execute(user.getProfileImageLink());

	TextView textView = (TextView) findViewById(R.id.profileDisplayName);
	textView.setText(user.getDisplayName());

	textView = (TextView) findViewById(R.id.profileUserReputation);
	if (user.getReputation() > 10000)
	{
	    double reputation = ((double) user.getReputation()) / 1000;
	    textView.setText(String.format("%.1fk", reputation));
	}
	else
	{
	    textView.setText(String.valueOf(user.getReputation()));
	}

	if (user.getBadgeCounts() != null && user.getBadgeCounts().length == 3)
	{
	    textView = (TextView) findViewById(R.id.profileUserGoldNum);
	    textView.setText(String.valueOf(user.getBadgeCounts()[0]));

	    textView = (TextView) findViewById(R.id.profileUserSilverNum);
	    textView.setText(String.valueOf(user.getBadgeCounts()[1]));

	    textView = (TextView) findViewById(R.id.profileUserBronzeNum);
	    textView.setText(String.valueOf(user.getBadgeCounts()[2]));
	}

	textView = (TextView) findViewById(R.id.profileViews);
	textView.append(" " + user.getProfileViews());

	textView = (TextView) findViewById(R.id.profileUserLastSeen);
	textView.append(" " + DateTimeUtils.getElapsedDurationSince(user.getLastAccessTime()));

    }
}

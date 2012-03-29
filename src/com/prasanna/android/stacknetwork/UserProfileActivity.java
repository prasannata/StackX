package com.prasanna.android.stacknetwork;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.intent.UserDetailsIntentService;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.FetchImageAsyncTask;
import com.prasanna.android.task.ImageFetchAsyncTaskCompleteNotifierImpl;

public class UserProfileActivity extends Activity
{
    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private Intent userIntent;

    private User user;

    private ProgressDialog fetchProfileProgress;

    private RelativeLayout relativeLayout;

    private class TabListener implements ActionBar.TabListener
    {
	private Fragment mFragment;

	public TabListener(Fragment fragment)
	{
	    mFragment = fragment;
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
	    ft.add(R.id.fragmentContainer, mFragment, null);
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
	    ft.remove(mFragment);
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
	    Toast.makeText(UserProfileActivity.this, "Reselected!", Toast.LENGTH_SHORT).show();
	}

    }

    private class ProfileFragment extends Fragment
    {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState)
	{
	    relativeLayout = (RelativeLayout) inflater.inflate(R.layout.user_proile_layout,
		    container, false);

	    if (user == null)
	    {
		fetchProfileProgress = ProgressDialog.show(UserProfileActivity.this, "",
		        "Fetching profile");
	    }
	    else
	    {
		displayUserDetail(user, relativeLayout);
	    }
	    return relativeLayout;
	}
    }

    private class QuestionsFragment extends Fragment
    {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState)
	{
	    TextView textView = new TextView(getApplicationContext());
	    textView.setText("Questions go here");
	    textView.setTextColor(R.color.black);
	    return textView;
	}
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    user = (User) intent.getSerializableExtra(IntentActionEnum.UserIntentAction.USER_DETAIL
		    .getExtra());

	    if (relativeLayout != null)
	    {
		fetchProfileProgress.dismiss();
		displayUserDetail(user, relativeLayout);
	    }
	}
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.fragment_container);
	registerForUserServiceReceiver();
	startUserService();
	setupActionBarTabs();
    }

    private void setupActionBarTabs()
    {
	ActionBar actionBar = getActionBar();
	getActionBar().setTitle(OperatingSite.getSite().getName());
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	Tab profileTab = actionBar.newTab();
	profileTab.setText("Profile").setTabListener(new TabListener(new ProfileFragment()));
	actionBar.addTab(profileTab);

	Tab questionsTab = actionBar.newTab();
	questionsTab.setText("Questions").setTabListener(new TabListener(new QuestionsFragment()));
	actionBar.addTab(questionsTab);

	Tab answersTab = actionBar.newTab();
	answersTab.setText("Answers").setTabListener(new TabListener(new ProfileFragment()));
	actionBar.addTab(answersTab);

	Tab tagsTab = actionBar.newTab();
	tagsTab.setText("Tags").setTabListener(new TabListener(new ProfileFragment()));
	actionBar.addTab(tagsTab);
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

    private void displayUserDetail(User user, RelativeLayout relativeLayout)
    {
	if (user != null)
	{
	    updateProfileInfo(user, relativeLayout);

	    TextView textView = (TextView) relativeLayout.findViewById(R.id.questionCount);
	    textView.append(" " + String.valueOf(user.getQuestionCount()));

	    textView = (TextView) relativeLayout.findViewById(R.id.answerCount);
	    textView.append(" " + String.valueOf(user.getAnswerCount()));

	    textView = (TextView) relativeLayout.findViewById(R.id.upvoteCount);
	    textView.append(" " + String.valueOf(user.getUpvoteCount()));

	    textView = (TextView) relativeLayout.findViewById(R.id.downvoteCount);
	    textView.append(" " + String.valueOf(user.getDownvoteCount()));
	}
    }

    private void updateProfileInfo(User user, RelativeLayout relativeLayout)
    {
	ImageView userProfileImage = (ImageView) relativeLayout.findViewById(R.id.profileUserImage);
	FetchImageAsyncTask fetchImageAsyncTask = new FetchImageAsyncTask(
	        new ImageFetchAsyncTaskCompleteNotifierImpl(userProfileImage));
	fetchImageAsyncTask.execute(user.getProfileImageLink());

	TextView textView = (TextView) relativeLayout.findViewById(R.id.profileDisplayName);
	textView.setText(user.getDisplayName());

	textView = (TextView) relativeLayout.findViewById(R.id.profileUserReputation);
	textView.setText(AppUtils.formatUserReputation(user.getReputation()));

	if (user.getBadgeCounts() != null && user.getBadgeCounts().length == 3)
	{
	    textView = (TextView) relativeLayout.findViewById(R.id.profileUserGoldNum);
	    textView.setText(String.valueOf(user.getBadgeCounts()[0]));

	    textView = (TextView) relativeLayout.findViewById(R.id.profileUserSilverNum);
	    textView.setText(String.valueOf(user.getBadgeCounts()[1]));

	    textView = (TextView) relativeLayout.findViewById(R.id.profileUserBronzeNum);
	    textView.setText(String.valueOf(user.getBadgeCounts()[2]));
	}

	textView = (TextView) relativeLayout.findViewById(R.id.profileViews);
	textView.append(" " + user.getProfileViews());

	textView = (TextView) relativeLayout.findViewById(R.id.profileUserLastSeen);
	textView.append(" " + DateTimeUtils.getElapsedDurationSince(user.getLastAccessTime()));

    }
}

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.intent.UserAnswersIntentService;
import com.prasanna.android.stacknetwork.intent.UserDetailsIntentService;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.FetchImageAsyncTask;
import com.prasanna.android.task.ImageFetchAsyncTaskCompleteNotifierImpl;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserProfileActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private Intent userProfileIntent;

    private Intent questionsByUserIntent;

    private Intent anwersByUserIntent;

    private User user;

    private ProgressDialog fetchProfileProgress;

    private ProgressDialog fetchUserQuestionsProgress;

    private RelativeLayout profileHomeLayout;

    private LinearLayout questionsLayout;

    private ScrollViewWithNotifier questionsScroll;

    private ArrayList<Question> questionsByUser = new ArrayList<Question>();

    private ArrayList<Answer> answersByUser = new ArrayList<Answer>();

    private LinearLayout loadingProgressView;

    private LinearLayout questionsDisplayList;

    private LinearLayout userAccountList;

    private int questionsPage = 0;

    private int answersPage = 0;

    private int questionDisplayCursor = 0;

    private int answerDisplayCursor = 0;

    private int usersAccountCursor = 0;

    public class TabListener implements ActionBar.TabListener
    {
	private Fragment fragment;

	public TabListener(Fragment fragment)
	{
	    this.fragment = fragment;
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
	    ft.add(R.id.fragmentContainer, fragment, null);
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
	    ft.remove(fragment);
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
	}

    }

    public class ProfileFragment extends Fragment
    {
	public ProfileFragment()
	{
	    super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	    usersAccountCursor = 0;

	    profileHomeLayout = (RelativeLayout) inflater.inflate(R.layout.user_proile_layout, container, false);
	    userAccountList = (LinearLayout) profileHomeLayout.findViewById(R.id.accountsList);

	    if (user == null)
	    {
		fetchProfileProgress = ProgressDialog.show(UserProfileActivity.this, "", "Fetching profile");
	    }
	    else
	    {
		displayUserDetail(user, profileHomeLayout);
	    }

	    return profileHomeLayout;
	}
    }

    public class QuestionsFragment extends Fragment
    {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	    questionDisplayCursor = 0;

	    Log.d(TAG, "Creating question fragment");

	    fetchUserQuestionsProgress = ProgressDialog.show(UserProfileActivity.this, "", "Loading questions");

	    questionsLayout = (LinearLayout) inflater.inflate(R.layout.questions_layout, null);
	    questionsScroll = (ScrollViewWithNotifier) questionsLayout.findViewById(R.id.questionsScroll);
	    questionsDisplayList = (LinearLayout) getLayoutInflater().inflate(R.layout.fragment_questions, null);
	    questionsScroll.addView(questionsDisplayList);
	    questionsScroll.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
	    {
		@Override
		public void onScrollToBottom(View view)
		{
		    if (loadingProgressView == null)
		    {
			loadingProgressView = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress,
			                null);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 15, 0, 15);
			questionsDisplayList.addView(loadingProgressView, layoutParams);
		    }

		    startUserQuestionsService(user.id, user.accessToken);
		}
	    });

	    displayQuestions();

	    return questionsLayout;
	}
    }

    public class AnswersFragment extends Fragment
    {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	    answerDisplayCursor = 0;

	    Log.d(TAG, "Creating answer fragment");

	    fetchUserQuestionsProgress = ProgressDialog.show(UserProfileActivity.this, "", "Loading answers");

	    questionsLayout = (LinearLayout) inflater.inflate(R.layout.questions_layout, null);

	    questionsScroll = (ScrollViewWithNotifier) questionsLayout.findViewById(R.id.questionsScroll);
	    questionsDisplayList = (LinearLayout) getLayoutInflater().inflate(R.layout.fragment_questions, null);
	    questionsScroll.addView(questionsDisplayList);

	    questionsScroll.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
	    {
		@Override
		public void onScrollToBottom(View view)
		{
		    if (loadingProgressView == null)
		    {
			loadingProgressView = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress,
			                null);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 15, 0, 15);
			questionsDisplayList.addView(loadingProgressView, layoutParams);
		    }

		    startUserAnswersService(user.id, user.accessToken);
		}
	    });

	    displayAnswers();

	    return questionsLayout;
	}
    }

    private void displayAnswers()
    {
	if (fetchUserQuestionsProgress != null)
	{
	    fetchUserQuestionsProgress.dismiss();
	    fetchUserQuestionsProgress = null;
	}

	if (loadingProgressView != null)
	{
	    loadingProgressView.setVisibility(View.GONE);
	    loadingProgressView = null;
	}

	if (answersByUser != null && questionsLayout != null && questionsDisplayList != null)
	{
	    if (answersByUser.isEmpty())
	    {
		TextView textView = (TextView) getLayoutInflater().inflate(R.layout.textview_black_textcolor, null);
		textView.setText("No answers by " + user.displayName);
		questionsDisplayList.addView(textView);
	    }
	    else
	    {
		addAnswersToView();
	    }
	}
    }

    private void addAnswersToView()
    {
	for (; answerDisplayCursor < answersByUser.size(); answerDisplayCursor++)
	{
	    final RelativeLayout answerRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.user_item_row, null);
	    final Answer answer = answersByUser.get(answerDisplayCursor);
	    TextView textView = (TextView) answerRow.findViewById(R.id.userItemTitle);
	    textView.setText(Html.fromHtml(answer.title));

	    textView = (TextView) answerRow.findViewById(R.id.viewItem);
	    textView.setClickable(true);
	    textView.setOnClickListener(new View.OnClickListener()
	    {
		@Override
		public void onClick(View v)
		{
		    Point size = new Point();
		    getWindowManager().getDefaultDisplay().getSize(size);

		    PopupBuilder.build(getLayoutInflater(), answerRow, answer, size);
		}
	    });

	    textView = (TextView) answerRow.findViewById(R.id.viewQuestion);
	    textView.setClickable(true);
	    textView.setOnClickListener(new View.OnClickListener()
	    {
		@Override
		public void onClick(View v)
		{
		    Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
		    Question question = new Question();
		    question.id = answer.questionId;
		    question.title = answer.title;
		    intent.putExtra(StringConstants.QUESTION, question);
		    intent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name(), true);
		    startActivity(intent);
		}
	    });
	    questionsDisplayList.addView(answerRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		            LayoutParams.WRAP_CONTENT));
	}
    }

    private void displayQuestions()
    {
	Log.d(TAG, "Displaying questions");

	if (fetchUserQuestionsProgress != null)
	{
	    fetchUserQuestionsProgress.dismiss();
	    fetchUserQuestionsProgress = null;
	}

	if (loadingProgressView != null)
	{
	    loadingProgressView.setVisibility(View.GONE);
	    loadingProgressView = null;
	}

	if (questionsByUser != null && questionsLayout != null && questionsDisplayList != null)
	{
	    if (questionsByUser.isEmpty())
	    {
		TextView textView = (TextView) getLayoutInflater().inflate(R.layout.textview_black_textcolor, null);
		textView.setText("No questions by " + user.displayName);
		questionsDisplayList.addView(textView);
	    }
	    else
	    {
		for (; questionDisplayCursor < questionsByUser.size(); questionDisplayCursor++)
		{
		    LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(getLayoutInflater(),
			            questionsLayout.getContext(), questionsByUser.get(questionDisplayCursor));
		    questionsDisplayList.addView(questionLayout, new LinearLayout.LayoutParams(
			            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	    }
	}
    }

    private BroadcastReceiver userProfileReceiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    user = (User) intent.getSerializableExtra(IntentActionEnum.UserIntentAction.USER_DETAIL.getExtra());

	    if (profileHomeLayout != null)
	    {
		fetchProfileProgress.dismiss();
		displayUserDetail(user, profileHomeLayout);
	    }
	}
    };

    private BroadcastReceiver questionsByUserReceiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    questionsByUser.addAll((ArrayList<Question>) intent
		            .getSerializableExtra(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.getExtra()));

	    displayQuestions();

	    Log.d(TAG, "Number of questions by user: " + questionsByUser.size());
	}
    };

    private BroadcastReceiver answersByUserReceiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    answersByUser.addAll((ArrayList<Answer>) intent
		            .getSerializableExtra(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.getExtra()));

	    displayAnswers();

	    Log.d(TAG, "Number of answers by user: " + answersByUser.size());
	}
    };

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
		    TextView textView = (TextView) getLayoutInflater().inflate(R.layout.textview_black_textcolor, null);
		    textView.setText(user.accounts.get(usersAccountCursor).siteName);
		    userAccountList.addView(textView);
		}
	    }
	}
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.ll_whitebg_vertical);

	User intentForUser = (User) getIntent().getSerializableExtra(StringConstants.USER);
	registerForUserProfileReceiver();
	registerForQuestionsByUserReceiver();
	registerForAnwersByUserReceiver();
	registerForUserAccountsReceiver();

	startUserProfileService(intentForUser.id, intentForUser.accessToken);
	startUserQuestionsService(intentForUser.id, intentForUser.accessToken);
	startUserAnswersService(intentForUser.id, intentForUser.accessToken);

	setupActionBarTabs();
    }

    private void setupActionBarTabs()
    {
	ActionBar actionBar = getActionBar();
	getActionBar().setTitle(OperatingSite.getSite().name);
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	Tab profileTab = actionBar.newTab();
	profileTab.setIcon(R.drawable.person).setTabListener(new TabListener(new ProfileFragment()));
	actionBar.addTab(profileTab);

	Tab questionsTab = actionBar.newTab();
	questionsTab.setIcon(R.drawable.question_mark).setTabListener(new TabListener(new QuestionsFragment()));
	actionBar.addTab(questionsTab);

	Tab answersTab = actionBar.newTab();
	answersTab.setIcon(R.drawable.answers).setTabListener(new TabListener(new AnswersFragment()));
	actionBar.addTab(answersTab);
    }

    @Override
    protected void onStop()
    {
	super.onStop();

	stopServiceAndUnregsiterReceivers();
    }

    @Override
    protected void onDestroy()
    {
	super.onDestroy();

	stopServiceAndUnregsiterReceivers();
    }

    private void stopServiceAndUnregsiterReceivers()
    {
	if (userProfileIntent != null)
	{
	    stopService(userProfileIntent);
	}

	if (questionsByUserIntent != null)
	{
	    stopService(questionsByUserIntent);
	}

	if (anwersByUserIntent != null)
	{
	    stopService(anwersByUserIntent);
	}

	try
	{
	    unregisterReceiver(userProfileReceiver);
	    unregisterReceiver(questionsByUserReceiver);
	    unregisterReceiver(answersByUserReceiver);
	    unregisterReceiver(userAccountsReceiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    private void startUserProfileService(long userId, String accessToken)
    {
	userProfileIntent = new Intent(this, UserDetailsIntentService.class);
	userProfileIntent.setAction(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
	userProfileIntent.putExtra(StringConstants.USER_ID, userId);
	userProfileIntent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
	startService(userProfileIntent);
    }

    private void startUserQuestionsService(long userId, String accessToken)
    {
	questionsByUserIntent = new Intent(this, UserQuestionsIntentService.class);
	questionsByUserIntent.setAction(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
	questionsByUserIntent.putExtra(StringConstants.USER_ID, userId);
	questionsByUserIntent.putExtra(StringConstants.PAGE, ++questionsPage);
	questionsByUserIntent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
	startService(questionsByUserIntent);
    }

    private void startUserAnswersService(long userId, String accessToken)
    {
	anwersByUserIntent = new Intent(this, UserAnswersIntentService.class);
	anwersByUserIntent.setAction(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
	anwersByUserIntent.putExtra(StringConstants.USER_ID, userId);
	anwersByUserIntent.putExtra(StringConstants.PAGE, ++answersPage);
	anwersByUserIntent.putExtra(StringConstants.ACCESS_TOKEN, accessToken);
	startService(anwersByUserIntent);
    }

    private void registerForUserProfileReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(userProfileReceiver, filter);
    }

    private void registerForQuestionsByUserReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(questionsByUserReceiver, filter);
    }

    private void registerForAnwersByUserReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.ANSWERS_BY_USER.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(answersByUserReceiver, filter);
    }

    private void registerForUserAccountsReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.USER_ACCOUNTS.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(userAccountsReceiver, filter);
    }

    private void displayUserDetail(User user, RelativeLayout relativeLayout)
    {
	if (user != null)
	{
	    updateProfileInfo(user, relativeLayout);

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

    private void updateProfileInfo(User user, RelativeLayout relativeLayout)
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

    @Override
    public void refresh()
    {
	// TODO Auto-generated method stub
    }

    @Override
    public Context getCurrentAppContext()
    {
	return getApplicationContext();
    }
}

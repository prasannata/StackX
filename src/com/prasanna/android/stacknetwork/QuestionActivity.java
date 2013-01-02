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

package com.prasanna.android.stacknetwork;

import java.io.File;
import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.fragment.AnswerFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionFragment;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.WriteObjectAsyncTask;
import com.viewpagerindicator.TitlePageIndicator;

public class QuestionActivity extends AbstractUserActionBarActivity implements OnPageChangeListener
{
    private static final String TAG = QuestionActivity.class.getSimpleName();

    private Question question;
    private Intent activeIntentForService;
    private QuestionFragment questionFragment;
    private QuestionViewPageAdapter questionViewPageAdapter;
    private ViewPager viewPager;
    private TitlePageIndicator indicator;
    boolean serviceRunningForAnswers = false;

    public class QuestionViewPageAdapter extends FragmentPagerAdapter
    {
	public QuestionViewPageAdapter(FragmentManager fm)
	{
	    super(fm);
	}

	@Override
	public int getCount()
	{
	    if (question == null)
		return 1;
	    else
		return question.answers == null ? 1 : 1 + question.answers.size();
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
	    if (position == 0)
		return QuestionActivity.this.getString(R.string.question);
	    else
	    {
		if (question.answers.get(position - 1).accepted)
		{
		    return QuestionActivity.this.getString(R.string.accepted) + " "
			            + QuestionActivity.this.getString(R.string.answer);
		}
		else
		{
		    return QuestionActivity.this.getString(R.string.answer) + " " + position;
		}
	    }

	}

	@Override
	public Fragment getItem(int position)
	{
	    if (position == 0)
	    {
		return QuestionActivity.this.questionFragment;
	    }
	    else
	    {
		AnswerFragment answerFragment = new AnswerFragment();
		answerFragment.setRetainInstance(true);
		answerFragment.setAnswer(question.answers.get(position - 1));
		return answerFragment;
	    }
	}
    }

    private BroadcastReceiver questionBodyReceiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    question = (Question) intent
		            .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS
		                            .getExtra());

	    if (StringConstants.QUESTION_ID.equals(QuestionActivity.this.getIntent().getAction()))
		questionFragment.setAndDisplay(question);
	    else
		questionFragment.displayBody(question.body);

	    // Happens if question was retrieved from LRU cache.
	    if (question.answers != null && !question.answers.isEmpty())
	    {
		hideRefreshActionAnimation();
		indicator.notifyDataSetChanged();
	    }
	}
    };

    private BroadcastReceiver questionCommentsReceiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    question.comments = (ArrayList<Comment>) intent
		            .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.getExtra());

	    questionFragment.setComments(question.comments);
	}
    };

    private BroadcastReceiver questionAnswersReceiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    Log.d(TAG, "Answers received for question");

	    ArrayList<Answer> answers = (ArrayList<Answer>) intent
		            .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra());

	    serviceRunningForAnswers = false;

	    if (answers != null && !answers.isEmpty())
	    {
		if (question.answers == null)
		    question.answers = new ArrayList<Answer>();

		question.answers.addAll(answers);
		indicator.notifyDataSetChanged();
		questionViewPageAdapter.notifyDataSetChanged();
	    }
	    else
	    {
		Log.d(TAG, "No answers for question");
	    }

	    hideRefreshActionAnimation();
	}
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.viewpager_title_indicator);

	questionViewPageAdapter = new QuestionViewPageAdapter(getFragmentManager());

	viewPager = (ViewPager) findViewById(R.id.viewPager);
	viewPager.setAdapter(questionViewPageAdapter);

	indicator = (TitlePageIndicator) findViewById(R.id.indicator);
	indicator.setViewPager(viewPager);
	indicator.setOnPageChangeListener(this);

	questionFragment = new QuestionFragment();
	questionFragment.setRetainInstance(true);

	registerReceivers();
	prepareIntentAndStartService();
    }

    private void prepareIntentAndStartService()
    {
	activeIntentForService = new Intent(this, QuestionDetailsIntentService.class);
	if (StringConstants.QUESTION_ID.equals(getIntent().getAction()))
	{
	    activeIntentForService.setAction(StringConstants.QUESTION_ID);
	    activeIntentForService.putExtra(StringConstants.QUESTION_ID,
		            getIntent().getLongExtra(StringConstants.QUESTION_ID, 0));
	}
	else
	{
	    activeIntentForService.setAction(StringConstants.QUESTION);
	    question = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);
	    questionFragment.setQuestion(question);
	    activeIntentForService.putExtra(StringConstants.QUESTION, question);
	}

	startQuestionService(activeIntentForService);
    }

    @Override
    public void onResume()
    {
	super.onResume();

	hideRefreshActionAnimation();
    }

    @Override
    protected void onDestroy()
    {
	super.onDestroy();
	stopServiceAndUnregsiterReceiver();
    }

    @Override
    protected void onStop()
    {
	super.onStop();

	stopServiceAndUnregsiterReceiver();
    }

    private void stopServiceAndUnregsiterReceiver()
    {
	if (activeIntentForService != null)
	    stopService(activeIntentForService);

	try
	{
	    if (questionBodyReceiver != null)
		unregisterReceiver(questionBodyReceiver);

	    if (questionAnswersReceiver != null)
		unregisterReceiver(questionAnswersReceiver);

	    if (questionCommentsReceiver != null)
		unregisterReceiver(questionCommentsReceiver);
	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    private void startQuestionService(Intent intent)
    {
	activeIntentForService.putExtra(StringConstants.CACHED,
	                getIntent().getBooleanExtra(StringConstants.CACHED, false));
	startService(activeIntentForService);
    }

    private void registerReceivers()
    {
	registerForQuestionBodyReceiver();
	registerForQuestionCommentsReceiver();
	registerForQuestionAnswersReceiver();
    }

    private void registerForQuestionBodyReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(questionBodyReceiver, filter);
    }

    private void registerForQuestionCommentsReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(questionCommentsReceiver, filter);
    }

    private void registerForQuestionAnswersReceiver()
    {
	IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.name());
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	registerReceiver(questionAnswersReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	boolean ret = super.onCreateOptionsMenu(menu);

	if (ret)
	{
	    if (viewPager.getCurrentItem() == 0 && question != null && question.answerCount > 0
		            && question.answers == null)
	    {
		Log.d(TAG, "Starting refresh action animation");
		startRefreshAnimation();
	    }
	    return true;
	}

	return false;
    }

    @Override
    protected void refresh()
    {
	Intent intent = getIntent();
	finish();
	intent.putExtra(StringConstants.CACHED, false);
	startActivity(intent);
    }

    @Override
    protected Context getCurrentContext()
    {
	return this;
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {
	Log.v(TAG, "onPageScrollStateChanged N/A");
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
	Log.v(TAG, "onPageScrolled N/A");
    }

    @Override
    public void onPageSelected(int position)
    {
	Log.d(TAG, "Selected page: " + position);

	int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;

	if (numAnswersDisplayed < question.answerCount && numAnswersDisplayed - position < 2)
	{
	    if (question.answers != null && question.answers.size() == numAnswersDisplayed && !serviceRunningForAnswers)
	    {
		Log.d(TAG, "Fetch next page of answers");

		startRefreshAnimation();

		activeIntentForService = new Intent(this, QuestionDetailsIntentService.class);
		activeIntentForService.setAction(QuestionIntentAction.QUESTION_ANSWERS.name());
		activeIntentForService.putExtra(StringConstants.QUESTION_ID, question.id);
		activeIntentForService.putExtra(StringConstants.PAGE, getNextPageNumber());
		startService(activeIntentForService);
		serviceRunningForAnswers = true;
	    }
	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
	if (viewPager.getCurrentItem() == 0)
	    return onContextItemSelectedForQuestion(item);
	else
	    return onContextItemSelectedForAnswer(item, viewPager.getCurrentItem() - 1);
    }

    private boolean onContextItemSelectedForQuestion(MenuItem item)
    {
	Log.d(TAG, "onContextItemSelectedForQuestion");

	if (item.getGroupId() == R.id.qContextMenuGroup)
	{
	    Log.d(TAG, "Context item selected: " + item.getTitle());

	    switch (item.getItemId())
	    {
		case R.id.q_ctx_comments:
		    Toast.makeText(this, "Fetch comments", Toast.LENGTH_LONG).show();
		    return true;
		case R.id.q_ctx_related:
		    Intent questionsIntent = new Intent(this, QuestionsActivity.class);
		    questionsIntent.setAction(StringConstants.RELATED);
		    questionsIntent.putExtra(StringConstants.QUESTION_ID, question.id);
		    startActivity(questionsIntent);
		    return true;
		case R.id.q_ctx_menu_user_profile:
		    viewUserProfile(question.owner.id);
		    return true;
		case R.id.q_ctx_menu_archive:
		    archiveQuestion();
		    return true;
		case R.id.q_ctx_menu_email:
		    emailQuestion();
		    return true;
		default:
		    return false;
	    }
	}
	else if (item.getGroupId() == R.id.qContextTagsMenuGroup)
	{
	    Log.d(TAG, "Tag selected: " + item.getTitle());

	    Intent questionsIntent = new Intent(this, QuestionsActivity.class);
	    questionsIntent.setAction(StringConstants.TAG);
	    questionsIntent.putExtra(StringConstants.TAG, item.getTitle());
	    startActivity(questionsIntent);

	    return true;
	}

	return false;
    }

    private boolean onContextItemSelectedForAnswer(MenuItem item, int answerPosition)
    {
	Log.d(TAG, "onContextItemSelectedForAnswer");

	if (item.getGroupId() == R.id.qContextMenuGroup)
	{
	    Log.d(TAG, "Context item selected: " + item.getTitle());

	    switch (item.getItemId())
	    {
		case R.id.q_ctx_comments:
		    Toast.makeText(this, "Fetch comments for answer", Toast.LENGTH_LONG).show();
		    return true;
		case R.id.q_ctx_menu_user_profile:
		    viewUserProfile(question.answers.get(answerPosition).owner.id);
		    return true;
		default:
		    return false;
	    }
	}

	return false;
    }

    private void viewUserProfile(long userId)
    {
	Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
	userProfileIntent.putExtra(StringConstants.USER_ID, userId);
	startActivity(userProfileIntent);
    }

    private void emailQuestion()
    {
	Intent emailIntent = IntentUtils.createEmailIntent(question.title, question.link);
	startActivity(Intent.createChooser(emailIntent, ""));
    }

    private void archiveQuestion()
    {
	File directory = new File(this.getCacheDir(), StringConstants.QUESTIONS);
	WriteObjectAsyncTask cacheTask = new WriteObjectAsyncTask(directory, String.valueOf(question.id));
	cacheTask.execute(question);

	Toast.makeText(this, "Question saved", Toast.LENGTH_SHORT).show();
    }

    private int getNextPageNumber()
    {
	int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;
	return (numAnswersDisplayed / 10) + 1;
    }
}

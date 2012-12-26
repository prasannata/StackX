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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.listener.FlingActionListener;
import com.prasanna.android.stacknetwork.intent.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.DialogBuilder;
import com.prasanna.android.stacknetwork.utils.HtmlTagFragmenter;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.PopupBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.DeleteObjectAsyncTask;
import com.prasanna.android.task.WriteObjectAsyncTask;
import com.prasanna.android.views.FlingScrollView;

/* TODO: Refactor and simplify this class */
public class QuestionDetailActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = QuestionDetailActivity.class.getSimpleName();
    private static final String COMMENTS = "Comments";
    private static final String ZERO_COMMENTS_LABEL = COMMENTS + " (0)";

    private Intent questionIntent;
    private LinearLayout detailLinearLayout;
    private TextView answersOrQuestion;
    private TextView commentsCickableTextView;
    private TextView currentAnswerOfTotalTextView;
    private TextView currentAnswerScore;
    private TextView questionTitle;
    private Question question;
    private FlingScrollView flingScrollView;
    private Button currentAnswerAuthor;
    private LinearLayout answerHeader;
    private View hrInQuestionTitle;
    private int currentAnswerCount = -1;
    private boolean viewingAnswer = false;
    private boolean fetchFullDetails;
    private LinearLayout questionOptionsLayout;
    private Button questionOwnerButton;
    private LinearLayout questionBodyProgress;

    private BroadcastReceiver questionBodyReceiver = new BroadcastReceiver()
    {
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    question = (Question) intent
		            .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS
		                            .getExtra());

	    if (fetchFullDetails == true)
	    {
		displayQuestionMetaData(question);
	    }

	    displayBody(question.body);

	    setupAnswersOnClick();
	}
    };

    private BroadcastReceiver questionCommentsReceiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    int numComments = 0;

	    question.comments = (ArrayList<Comment>) intent
		            .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_COMMENTS.getExtra());

	    if (question.comments != null)
	    {
		numComments = question.comments.size();
	    }

	    commentsCickableTextView.setText(COMMENTS + " (" + numComments + ")");

	}
    };

    private BroadcastReceiver questionAnswersReceiver = new BroadcastReceiver()
    {
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    if (question.answers == null)
	    {
		question.answers = new ArrayList<Answer>();
	    }

	    question.answers.addAll((ArrayList<Answer>) intent
		            .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra()));

	    Log.d(TAG, "num received answers:" + question.answers.size());

	    enableAnswersView();
	}
    };
    private RelativeLayout questionTitleLayout;

    private class QuestionDetailActivityFlingActionListenerImpl implements FlingActionListener
    {
	public void flingedToLeft()
	{
	    Log.d(TAG, "Flinged to left");

	    if (viewingAnswer && question.answers != null && currentAnswerCount < question.answers.size() - 1)
	    {
		++currentAnswerCount;
		updateViewForAnswer();
	    }
	}

	public void flingedToRight()
	{
	    Log.d(TAG, "Flinged to right");

	    if (currentAnswerCount > 0)
	    {
		--currentAnswerCount;
		updateViewForAnswer();
	    }
	}
    };

    private void updateViewForAnswer()
    {
	displayBody(question.answers.get(currentAnswerCount).body);

	if (question.answers.get(currentAnswerCount).comments == null)
	{
	    commentsCickableTextView.setText(ZERO_COMMENTS_LABEL);
	    commentsCickableTextView.setClickable(false);
	}
	else
	{
	    commentsCickableTextView.setText(COMMENTS + " (" + question.answers.get(currentAnswerCount).comments.size()
		            + ")");
	    commentsCickableTextView.setClickable(true);
	}

	currentAnswerOfTotalTextView.setText((currentAnswerCount + 1) + " of " + question.answerCount);
	User answerAuthor = question.answers.get(currentAnswerCount).owner;
	currentAnswerAuthor.setText(Html.fromHtml(answerAuthor.displayName) + " ("
	                + AppUtils.formatReputation(answerAuthor.reputation) + ")");
	currentAnswerScore.setText(getString(R.string.score) + ": " + question.answers.get(currentAnswerCount).score);
	if (question.answers.get(currentAnswerCount).accepted == true)
	{
	    answerHeader.setBackgroundResource(R.color.lichen);
	}
	else
	{
	    answerHeader.setBackgroundResource(R.color.lightGrey);
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.question_detail_layout);

	Uri clickedUri = getIntent().getData();

	if (clickedUri != null)
	{
	    Toast.makeText(this, "You clicked " + clickedUri, Toast.LENGTH_LONG).show();
	}
	else
	{
	    questionOptionsLayout = (LinearLayout) findViewById(R.id.questionOptions);
	    flingScrollView = (FlingScrollView) findViewById(R.id.questionDisplayFlingScrollView);
	    flingScrollView.flingActionListener = new QuestionDetailActivityFlingActionListenerImpl();
	    detailLinearLayout = (LinearLayout) findViewById(R.id.questionAnswerDetail);
	    questionTitleLayout = (RelativeLayout)findViewById(R.id.questionTitleLayout);
	    answerHeader = (LinearLayout) findViewById(R.id.answerHeader);
	    currentAnswerOfTotalTextView = (TextView) findViewById(R.id.currentAnswerOfTotal);
	    currentAnswerAuthor = (Button) findViewById(R.id.currentAnswerAuthor);
	    currentAnswerScore = (TextView) findViewById(R.id.currentAnswerScore);
	    hrInQuestionTitle = findViewById(R.id.hrInQuestionTitle);
	    currentAnswerAuthor.setOnClickListener(new View.OnClickListener()
	    {
		public void onClick(View view)
		{
		    startActivity(IntentUtils.createUserProfileIntent(view.getContext(),
			            question.answers.get(currentAnswerCount).owner.id));
		}
	    });

	    setupQuestionOptions();

	    setupCommentsPopup();

	    registerReceivers();

	    boolean cached = getIntent().getBooleanExtra(StringConstants.CACHED, false);

	    if (cached == false)
	    {
		fetchQuestionDetail();
	    }
	    else
	    {
		displayCachedQuestion();
	    }
	}
    }

    private void displayCachedQuestion()
    {
	question = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

	Button deleteQuestionButton = (Button) questionOptionsLayout.findViewById(R.id.deleteSavedQuestion);
	deleteQuestionButton.setVisibility(View.VISIBLE);
	deleteQuestionButton.setOnClickListener(new View.OnClickListener()
	{
	    private AlertDialog yesNoDialog;

	    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    switch (which)
		    {
			case DialogInterface.BUTTON_POSITIVE:
			    File directory = new File(getCacheDir(), StringConstants.QUESTIONS);
			    DeleteObjectAsyncTask asyncTask = new DeleteObjectAsyncTask(directory, String
				            .valueOf(question.id), null);
			    asyncTask.execute((Void) null);

			    Toast.makeText(QuestionDetailActivity.this, "Question deleted", Toast.LENGTH_SHORT).show();
			    Intent archiveIntent = new Intent(QuestionDetailActivity.this, ArchiveDisplayActivity.class);
			    archiveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(archiveIntent);
			    break;

			case DialogInterface.BUTTON_NEGATIVE:
			    dialog.dismiss();
			    break;
		    }
		}
	    };

	    @Override
	    public void onClick(View paramView)
	    {
		yesNoDialog = DialogBuilder.yesNoDialog(QuestionDetailActivity.this, R.string.sureQuestion,
		                dialogClickListener);
		yesNoDialog.show();
	    }
	});

	displayQuestionMetaData(question);

	displayBody(question.body);

	setupAnswersOnClick();

	enableAnswersView();

	if (question.comments != null)
	{
	    commentsCickableTextView.setText(COMMENTS + " (" + question.comments.size() + ")");
	}
	else
	{
	    commentsCickableTextView.setText(ZERO_COMMENTS_LABEL);
	}
    }

    private void setupQuestionOptions()
    {
	ImageView imageView = (ImageView) questionOptionsLayout.findViewById(R.id.returnToTitle);
	imageView.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View paramView)
	    {
		showQuestionTitleOptions(false);
	    }
	});

	imageView = (ImageView) questionOptionsLayout.findViewById(R.id.saveQuestion);
	imageView.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View paramView)
	    {
		File directory = new File(getCacheDir(), StringConstants.QUESTIONS);
		WriteObjectAsyncTask cacheTask = new WriteObjectAsyncTask(directory, String.valueOf(question.id));
		cacheTask.execute(question);

		Toast.makeText(QuestionDetailActivity.this, "Question saved", Toast.LENGTH_SHORT).show();
	    }
	});

	questionTitle = (TextView) findViewById(R.id.questionTitle);
	questionTitleLayout.setOnLongClickListener(new View.OnLongClickListener()
	{
	    @Override
	    public boolean onLongClick(View paramView)
	    {
		showQuestionTitleOptions(true);
		return true;
	    }
	});
    }

    private void showQuestionTitleOptions(boolean show)
    {
	int expectedTitleVisibility = show ? View.VISIBLE : View.INVISIBLE;

	if (questionTitle.getVisibility() == expectedTitleVisibility)
	{
	    int titleVisibility = View.INVISIBLE;
	    int optionsVisibility = View.VISIBLE;
	    int titleAnimation = android.R.anim.slide_out_right;
	    int optionsAnimation = android.R.anim.slide_in_left;

	    if (show == false)
	    {
		titleVisibility = View.VISIBLE;
		optionsVisibility = View.INVISIBLE;
		titleAnimation = android.R.anim.slide_in_left;
		optionsAnimation = android.R.anim.slide_out_right;
	    }

	    questionOptionsLayout.startAnimation(AnimationUtils.loadAnimation(QuestionDetailActivity.this,
		            optionsAnimation));
	    questionOptionsLayout.setVisibility(optionsVisibility);

	    questionTitle.startAnimation(AnimationUtils.loadAnimation(QuestionDetailActivity.this, titleAnimation));
	    questionTitle.setVisibility(titleVisibility);

	    questionOwnerButton.startAnimation(AnimationUtils
		            .loadAnimation(QuestionDetailActivity.this, titleAnimation));
	    questionOwnerButton.setVisibility(titleVisibility);
	}
    }

    private void fetchQuestionDetail()
    {
	fetchFullDetails = getIntent().getBooleanExtra(
	                IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name(), false);

	questionBodyProgress = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress, null);
	detailLinearLayout.addView(questionBodyProgress);

	if (fetchFullDetails == false)
	{
	    displayQuestionMetaData((Question) getIntent().getSerializableExtra(StringConstants.QUESTION));
	    startQuestionService(IntentActionEnum.QuestionIntentAction.QUESTION_BODY.name());
	}
	else
	{
	    startQuestionService(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.name());
	}
    }

    private void setupCommentsPopup()
    {
	commentsCickableTextView = (TextView) findViewById(R.id.comments);
	commentsCickableTextView.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		Log.d(TAG, "Comments clicked");

		ArrayList<Comment> comments = (viewingAnswer && currentAnswerCount != -1) ? question.answers
		                .get(currentAnswerCount).comments : question.comments;

		if (comments != null && comments.isEmpty() == false)
		{
		    Point size = new Point();
		    getWindowManager().getDefaultDisplay().getSize(size);
		    PopupBuilder.build(getLayoutInflater(), hrInQuestionTitle, comments, size);
		}
	    }
	});
    }

    private void registerReceivers()
    {
	registerForQuestionBodyReceiver();

	registerForQuestionCommentsReceiver();

	registerForQuestionAnswersReceiver();
    }

    private void startQuestionService(String intentAction)
    {
	questionIntent = new Intent(this, QuestionDetailsIntentService.class);
	questionIntent.setAction(intentAction);
	questionIntent.putExtra(StringConstants.QUESTION,
	                (Question) getIntent().getSerializableExtra(StringConstants.QUESTION));
	startService(questionIntent);
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
	if (questionIntent != null)
	{
	    stopService(questionIntent);
	}

	// If I do not unregister, it is leaked. Good, that is right. If I
	// unregister, it throws
	// IllegalArgumentException saying receiver not registered. WTF!
	try
	{
	    if (questionBodyReceiver != null)
	    {
		unregisterReceiver(questionBodyReceiver);
	    }

	    if (questionAnswersReceiver != null)
	    {
		unregisterReceiver(questionAnswersReceiver);
	    }

	    if (questionCommentsReceiver != null)
	    {
		unregisterReceiver(questionCommentsReceiver);
	    }

	}
	catch (IllegalArgumentException e)
	{
	    Log.d(TAG, e.getMessage());
	}
    }

    private void displayQuestionMetaData(final Question question)
    {
	updateAnswerCount(question);

	TextView textView = (TextView) findViewById(R.id.questionScore);
	textView.setText("Score: " + String.valueOf(question.score));

	questionTitle.setText(Html.fromHtml(question.title));

	ImageView imageView = (ImageView) questionOptionsLayout.findViewById(R.id.emailQuestion);
	imageView.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		Intent emailIntent = IntentUtils.createEmailIntent(question.title, question.link);
		startActivity(Intent.createChooser(emailIntent, ""));
	    }
	});

	questionOwnerButton = (Button) findViewById(R.id.questionOwner);
	questionOwnerButton.setText(getOwnerString(question.owner));
	questionOwnerButton.setOnClickListener(new View.OnClickListener()
	{
	    public void onClick(View view)
	    {
		startActivity(IntentUtils.createUserProfileIntent(view.getContext(), question.owner.id));
	    }
	});

	textView = (TextView) findViewById(R.id.questionTime);
	textView.setText(DateTimeUtils.getElapsedDurationSince(question.creationDate));

	textView = (TextView) findViewById(R.id.questionViews);
	textView.append(String.valueOf(question.viewCount));
    }

    private static String getOwnerString(User user)
    {
	String userDetails = "" + Html.fromHtml(user.displayName);

	userDetails += " (" + AppUtils.formatReputation(user.reputation);

	if (user.acceptRate != -1)
	{
	    userDetails += "," + user.acceptRate + "%";
	}

	userDetails += ")";
	return userDetails;
    }

    private void updateAnswerCount(final Question question)
    {
	answersOrQuestion = (TextView) findViewById(R.id.answers);
	answersOrQuestion.append(" (" + question.answerCount + ")");
	answersOrQuestion.setClickable(false);
	answersOrQuestion.setEnabled(false);
    }

    private void displayBody(String text)
    {
	detailLinearLayout.removeAllViews();
	ArrayList<TextView> questionBodyTextViews = HtmlTagFragmenter.parse(getApplicationContext(), text);
	for (TextView questionBodyTextView : questionBodyTextViews)
	{
	    detailLinearLayout.addView(questionBodyTextView);
	}
    }

    @Override
    public void refresh()
    {
	stopServiceAndUnregsiterReceiver();
	registerReceivers();
    }

    @Override
    public Context getCurrentContext()
    {
	return QuestionDetailActivity.this;
    }

    private void setupAnswersOnClick()
    {
	if (answersOrQuestion != null)
	{
	    answersOrQuestion.setOnClickListener(new View.OnClickListener()
	    {
		public void onClick(View v)
		{
		    String label = null;
		    String body = null;

		    showQuestionTitleOptions(false);

		    if (viewingAnswer == false)
		    {
			viewingAnswer = true;
			label = getString(R.string.question);

			if (currentAnswerCount == -1)
			{
			    currentAnswerCount = 0;
			}

			if (question.answers.isEmpty() == false)
			{
			    answerHeader.setVisibility(View.VISIBLE);
			    updateViewForAnswer();
			}
		    }
		    else
		    {
			label = getString(R.string.answers) + " (" + question.answerCount + ")";
			body = question.body;
			viewingAnswer = false;

			if (currentAnswerOfTotalTextView != null)
			{
			    answerHeader.setVisibility(View.GONE);
			}

			if (question.comments == null)
			{
			    commentsCickableTextView.setClickable(false);
			    commentsCickableTextView.setText(ZERO_COMMENTS_LABEL);
			}
			else
			{
			    commentsCickableTextView.setText(COMMENTS + " (" + question.comments.size() + ")");
			    commentsCickableTextView.setClickable(true);
			}

			displayBody(body);
		    }

		    answersOrQuestion.setText(label);
		}
	    });
	}
    }

    private void enableAnswersView()
    {
	if (answersOrQuestion.isEnabled() == false)
	{
	    answersOrQuestion.setBackgroundResource(R.drawable.square_bottom_edges);
	    answersOrQuestion.setTextColor(Color.WHITE);
	    answersOrQuestion.setEnabled(true);

	    if (question.answers != null)
	    {
		answersOrQuestion.setClickable(true);
	    }
	}
    }
}

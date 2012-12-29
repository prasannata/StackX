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

import java.io.File;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.UserProfileActivity;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.WriteObjectAsyncTask;

public class QuestionFragment extends Fragment
{
    private static final String TAG = QuestionFragment.class.getSimpleName();

    private LinearLayout parentLayout;
    private RelativeLayout titleLayout;
    private Question question;
    private ContextMenu menu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	if (parentLayout == null)
	{
	    createView(inflater);
	}

	return parentLayout;
    }

    private void createView(LayoutInflater inflater)
    {
	parentLayout = (LinearLayout) inflater.inflate(R.layout.question, null);
	titleLayout = (RelativeLayout) parentLayout.findViewById(R.id.questionTitleLayout);
	titleLayout.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		QuestionFragment.this.getActivity().openContextMenu(v);
	    }
	});

	displayQuestionMetaData();
	registerForContextMenu(titleLayout);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	Log.d(TAG, "onCreate");

	super.onCreate(savedInstanceState);
	setHasOptionsMenu(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
	Log.d(TAG, "onCreateContextMenu");

	super.onCreateContextMenu(menu, v, menuInfo);

	MenuInflater inflater = getActivity().getMenuInflater();
	inflater.inflate(R.menu.question_context_menu, menu);
	MenuItem userProfileMenuItem = menu.findItem(R.id.q_ctx_menu_user_profile);

	if (userProfileMenuItem != null)
	{
	    userProfileMenuItem.setTitle(Html.fromHtml(question.owner.displayName) + "'s profile");
	}

	this.menu = menu;

	enableCommentsInContextMenu();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
	Log.d(TAG, "onContextItemSelected");

	if (item.getGroupId() == R.id.qContextMenuGroup)
	{
	    Log.d(TAG, "Context item selected: " + item.getItemId());

	    switch (item.getItemId())
	    {
		case R.id.q_ctx_comments:
		    Toast.makeText(getActivity(), "Fetch comments", Toast.LENGTH_LONG).show();
		    return true;
		case R.id.q_ctx_similar:
		    Toast.makeText(getActivity(), "Fetch similar questions", Toast.LENGTH_LONG).show();
		    return true;
		case R.id.q_ctx_menu_user_profile:
		    Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);
		    userProfileIntent.putExtra(StringConstants.USER_ID, question.owner.id);
		    startActivity(userProfileIntent);
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

	return false;
    }

    private void emailQuestion()
    {
	Intent emailIntent = IntentUtils.createEmailIntent(question.title, question.link);
	startActivity(Intent.createChooser(emailIntent, ""));
    }

    private void archiveQuestion()
    {
	File directory = new File(getActivity().getCacheDir(), StringConstants.QUESTIONS);
	WriteObjectAsyncTask cacheTask = new WriteObjectAsyncTask(directory, String.valueOf(question.id));
	cacheTask.execute(question);

	Toast.makeText(getActivity(), "Question saved", Toast.LENGTH_SHORT).show();
    }

    private void displayQuestionMetaData()
    {
	if (question != null)
	{
	    TextView textView = (TextView) parentLayout.findViewById(R.id.questionScore);
	    textView.setText(AppUtils.formatNumber(question.score));

	    textView = (TextView) parentLayout.findViewById(R.id.questionTitle);
	    textView.setText(Html.fromHtml(question.title));

	    String acceptRate = question.owner.acceptRate > 0 ? (question.owner.acceptRate + "%, ") : "";
	    textView = (TextView) parentLayout.findViewById(R.id.questionOwner);
	    textView.setText(getTimeAndOwnerDisplay(acceptRate));

	    textView = (TextView) parentLayout.findViewById(R.id.questionViews);
	    textView.append(AppUtils.formatNumber(question.viewCount));
	}
    }

    private String getTimeAndOwnerDisplay(String acceptRate)
    {
	return DateTimeUtils.getElapsedDurationSince(question.creationDate) + " by "
	                + Html.fromHtml(question.owner.displayName) + " [" + acceptRate + question.owner.reputation
	                + "]";
    }

    private void showNumComments()
    {
	if (question.comments != null && !question.comments.isEmpty())
	{
	    TextView textView = (TextView) parentLayout.findViewById(R.id.questionComments);
	    textView.append(String.valueOf(question.comments.size()));
	    textView.setVisibility(View.VISIBLE);

	    enableCommentsInContextMenu();
	}
    }

    private void enableCommentsInContextMenu()
    {
	if (menu != null)
	{
	    MenuItem item = menu.findItem(R.id.q_ctx_comments);
	    item.setEnabled(true);
	    item.setVisible(true);
	}
    }

    public void displayBody(String text)
    {
	LinearLayout questionBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.questionBody);
	ArrayList<TextView> questionBodyTextViews = MarkdownFormatter.format(getActivity(), text);
	for (TextView questionBodyTextView : questionBodyTextViews)
	{
	    questionBodyLayout.addView(questionBodyTextView);
	}
    }

    public void setQuestion(Question question)
    {
	this.question = question;
    }

    public void setComments(ArrayList<Comment> comments)
    {
	question.comments = comments;

	showNumComments();
    }
}

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

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;

public class QuestionFragment extends Fragment
{
    private static final String TAG = QuestionFragment.class.getSimpleName();

    private LinearLayout parentLayout;
    private Question question;
    private ContextMenu menu;
    private boolean ctxMenuSetup = false;

    public static QuestionFragment newFragment()
    {
	QuestionFragment questionFragment = new QuestionFragment();
	questionFragment.setRetainInstance(true);
	return questionFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	if (parentLayout == null)
	    createView(inflater);

	return parentLayout;
    }

    private void createView(LayoutInflater inflater)
    {
	parentLayout = (LinearLayout) inflater.inflate(R.layout.question, null);
	ImageView imageView = (ImageView) parentLayout.findViewById(R.id.questionOptionsContextMenu);
	imageView.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		QuestionFragment.this.getActivity().openContextMenu(v);
	    }
	});

	displayQuestion();
	registerForContextMenu(imageView);
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
	this.menu = menu;

	setupUserProfileInContextMenu();
	setupTagsInContextMenu();
	enableCommentsInContextMenu();
    }

    private void setupUserProfileInContextMenu()
    {
	if (question != null && question.owner != null)
	{
	    MenuItem userProfileMenuItem = menu.findItem(R.id.q_ctx_menu_user_profile);

	    if (userProfileMenuItem != null)
		userProfileMenuItem.setTitle(Html.fromHtml(question.owner.displayName) + "'s profile");

	    ctxMenuSetup = true;
	}
    }

    private void setupTagsInContextMenu()
    {
	if (question != null && question.tags != null)
	{
	    MenuItem menuItem = menu.findItem(R.id.q_ctx_menu_tags);
	    SubMenu subMenu = menuItem.getSubMenu();
	    for (int idx = 0; idx < question.tags.length; idx++)
		subMenu.add(R.id.qContextTagsMenuGroup, Menu.NONE, idx, question.tags[idx]);

	    ctxMenuSetup = ctxMenuSetup & true;
	}
    }

    private void displayQuestion()
    {
	if (question != null)
	{
	    if (!ctxMenuSetup && menu != null)
	    {
		setupTagsInContextMenu();
		setupUserProfileInContextMenu();
	    }

	    TextView textView = (TextView) parentLayout.findViewById(R.id.questionScore);
	    textView.setText(AppUtils.formatNumber(question.score));

	    textView = (TextView) parentLayout.findViewById(R.id.questionTitle);
	    textView.setText(Html.fromHtml(question.title));

	    String acceptRate = question.owner.acceptRate > 0 ? (question.owner.acceptRate + "%, ") : "";
	    textView = (TextView) parentLayout.findViewById(R.id.questionOwner);
	    textView.setText(getTimeAndOwnerDisplay(acceptRate));

	    textView = (TextView) parentLayout.findViewById(R.id.questionViews);
	    textView.append(AppUtils.formatNumber(question.viewCount));

	    if (question.body != null)
	    {
		displayBody(question.body);
	    }
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
	if (text != null)
	{
	    final LinearLayout questionBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.questionBody);
	    ArrayList<View> questionBodyTextViews = MarkdownFormatter.parse(getActivity(), text);
	    for (final View questionBodyTextView : questionBodyTextViews)
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

    public void setAndDisplay(Question question)
    {
	setQuestion(question);

	if (parentLayout != null)
	{
	    displayQuestion();
	    displayBody(question.body);
	}
    }
}

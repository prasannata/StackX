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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.MarkdownFormatter;

public class AnswerFragment extends Fragment
{
    private static final String TAG = AnswerFragment.class.getSimpleName();
    private LinearLayout parentLayout;
    private LinearLayout answerBodyLayout;
    private Answer answer;
    private RelativeLayout answerMetaInfoLayout;
    private ImageView iv;

    public static AnswerFragment newFragment(Answer answer)
    {
	AnswerFragment answerFragment = new AnswerFragment();
	answerFragment.setRetainInstance(true);
	answerFragment.setAnswer(answer);
	return answerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	if (savedInstanceState == null)
	{
	    parentLayout = (LinearLayout) inflater.inflate(R.layout.answer, null);
	    answerBodyLayout = (LinearLayout) parentLayout.findViewById(R.id.answerBody);
	    answerMetaInfoLayout = (RelativeLayout) parentLayout.findViewById(R.id.answerMetaInfo);
	    iv = (ImageView) answerMetaInfoLayout.findViewById(R.id.answerOptionsContextMenu);

	    if (answer != null)
		displayAnswer();
	}

	return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	Log.d(TAG, "onActivityCreated");

	super.onActivityCreated(savedInstanceState);

	registerForContextMenu(iv);
    }

    private void displayAnswer()
    {
	TextView textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerScore);
	textView.setText(AppUtils.formatNumber(answer.score));

	String acceptRate = answer.owner.acceptRate > 0 ? (answer.owner.acceptRate + "%, ") : "";
	textView = (TextView) answerMetaInfoLayout.findViewById(R.id.answerAuthor);
	textView.setText(getAutherDisplayText(acceptRate));

	iv.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		AnswerFragment.this.getActivity().openContextMenu(v);
	    }
	});

	ArrayList<View> answerBodyTextViews = MarkdownFormatter.parse(getActivity(), answer.body);
	for (View answer : answerBodyTextViews)
	    answerBodyLayout.addView(answer);
    }

    private String getAutherDisplayText(String acceptRate)
    {
	return DateTimeUtils.getElapsedDurationSince(answer.creationDate) + " by "
	                + Html.fromHtml(answer.owner.displayName) + " [" + acceptRate
	                + AppUtils.formatReputation(answer.owner.reputation) + "]";
    }

    private void setAnswer(Answer answer)
    {
	Log.d(TAG, "answer set");

	this.answer = answer;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
	Log.d(TAG, "onCreateContextMenu");

	super.onCreateContextMenu(menu, v, menuInfo);

	MenuInflater inflater = getActivity().getMenuInflater();
	inflater.inflate(R.menu.question_context_menu, menu);
	menu.removeItem(R.id.q_ctx_menu_archive);
	menu.removeItem(R.id.q_ctx_related);
	menu.removeItem(R.id.q_ctx_menu_email);
	menu.removeItem(R.id.q_ctx_menu_tags);

	enableCommentsInContextMenu(menu);

	MenuItem menuItem = menu.findItem(R.id.q_ctx_menu_user_profile);
	menuItem.setTitle(Html.fromHtml(answer.owner.displayName) + "'s profile");
    }

    private void enableCommentsInContextMenu(ContextMenu menu)
    {
	MenuItem item = menu.findItem(R.id.q_ctx_comments);
	item.setEnabled(true);
	item.setVisible(true);
    }
}

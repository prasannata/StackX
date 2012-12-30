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

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.UserProfileActivity;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractListQuestionFragment extends ItemListFragment<Question> implements OnScrollListener,
                ListItemView<Question>
{
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	super.onActivityCreated(savedInstanceState);

	registerForContextMenu(getListView());

	getListView().setOnScrollListener(this);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
	if (item.getGroupId() == R.id.qContextMenuGroup)
	{
	    Log.d(getLogTag(), "Context item selected: " + item.getItemId());

	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId())
	    {
		case R.id.q_ctx_comments:
		    Toast.makeText(getActivity(), "Fetch comments", Toast.LENGTH_LONG).show();
		    return true;
		case R.id.q_ctx_menu_user_profile:
		    Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);
		    userProfileIntent
			            .putExtra(StringConstants.USER_ID, itemListAdapter.getItem(info.position).owner.id);
		    startActivity(userProfileIntent);
		    return true;
		case R.id.q_ctx_menu_email:
		    emailQuestion(itemListAdapter.getItem(info.position).title,
			            itemListAdapter.getItem(info.position).link);
		    return true;
		default:
		    return false;
	    }
	}

	return false;
    }

    private void emailQuestion(String subject, String body)
    {
	Intent emailIntent = IntentUtils.createEmailIntent(subject, body);
	startActivity(Intent.createChooser(emailIntent, ""));
    }

    @Override
    public View getView(Question item, View convertView, ViewGroup parent)
    {
	LinearLayout layout = QuestionRowLayoutBuilder.getInstance().build(getActivity().getLayoutInflater(),
	                getActivity(), false, item);
	TextView textView = (TextView) layout.findViewById(R.id.questionSnippetTitle);
	RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
	layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	ImageView imageView = (ImageView) layout.findViewById(R.id.questionOptionsContextMenu);
	imageView.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		getActivity().openContextMenu(v);
	    }
	});
	return layout;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
	Intent displayQuestionIntent = new Intent(getActivity(), QuestionActivity.class);
	displayQuestionIntent.putExtra(StringConstants.QUESTION, itemListAdapter.getItem(position));
	displayQuestionIntent.putExtra(StringConstants.CACHED, false);
	// if (cached == true)
	// {
	// displayQuestionIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	// }
	startActivity(displayQuestionIntent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
	Log.d(getLogTag(), "onCreateContextMenu");

	super.onCreateContextMenu(menu, v, menuInfo);

	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	MenuInflater inflater = getActivity().getMenuInflater();
	inflater.inflate(R.menu.question_context_menu, menu);
	menu.removeItem(R.id.q_ctx_menu_archive);
	MenuItem userProfileMenuItem = menu.findItem(R.id.q_ctx_menu_user_profile);

	if (userProfileMenuItem != null)
	{
	    userProfileMenuItem.setTitle(Html.fromHtml(itemListAdapter.getItem(info.position).owner.displayName)
		            + "'s profile");
	}
    }

    @Override
    public String getReceiverExtraName()
    {
	return QuestionIntentAction.QUESTIONS.getExtra();
    }
}

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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractQuestionListFragment extends ItemListFragment<Question> implements ListItemView<Question>
{
    private static final String TAG = AbstractQuestionListFragment.class.getSimpleName();

    private OnContextItemSelectedListener<Question> onContextItemSelectedListener;
    private final Bundle bundle = new Bundle();
    private int position;

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Activity activity)
    {
	Log.d(TAG, "onAttach");

	super.onAttach(activity);

	if (!(activity instanceof OnContextItemSelectedListener))
	    throw new IllegalArgumentException(activity.getLocalClassName()
		            + " must implement OnContextItemSelectedListener");

	onContextItemSelectedListener = (OnContextItemSelectedListener<Question>) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
	Log.d(TAG, "onActivityCreated");

	registerForContextMenu(getListView());

	super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
	Log.d(TAG, "onContextItemSelected");

	return onContextItemSelectedListener.onContextItemSelected(item, itemListAdapter.getItem(position));
    }

    @Override
    public View getView(Question item, View convertView, ViewGroup parent)
    {
	LinearLayout layout = QuestionRowLayoutBuilder.getInstance().build(getActivity().getLayoutInflater(),
	                getActivity(), item);
	ImageView imageView = (ImageView) layout.findViewById(R.id.itemContextMenu);
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
	displayQuestionIntent.setAction(StringConstants.QUESTION);
	displayQuestionIntent.putExtra(StringConstants.QUESTION, itemListAdapter.getItem(position));
	displayQuestionIntent.putExtra(StringConstants.CACHED, false);
	startActivity(displayQuestionIntent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
	Log.d(TAG, "onCreateContextMenu");

	super.onCreateContextMenu(menu, v, menuInfo);

	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	position = info.position;

	MenuInflater inflater = getActivity().getMenuInflater();
	inflater.inflate(R.menu.question_context_menu, menu);
	menu.removeItem(R.id.q_ctx_menu_archive);

	MenuItem menuItem = menu.findItem(R.id.q_ctx_menu_user_profile);
	menuItem.setTitle(Html.fromHtml(itemListAdapter.getItem(position).owner.displayName) + "'s profile");

	menuItem = menu.findItem(R.id.q_ctx_menu_tags);
	SubMenu subMenu = menuItem.getSubMenu();

	if (itemListAdapter.getItem(position).tags != null)
	{
	    for (int idx = 0; idx < itemListAdapter.getItem(position).tags.length; idx++)
	    {
		subMenu.add(R.id.qContextTagsMenuGroup, Menu.NONE, idx, itemListAdapter.getItem(position).tags[idx]);
	    }
	}
    }

    @Override
    public String getReceiverExtraName()
    {
	return StringConstants.QUESTIONS;
    }

    public Bundle getBundle()
    {
	return bundle;
    }
}

/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.QuestionsActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.UserProfileActivity;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public abstract class AbstractQuestionListFragment extends ItemListFragment<Question> implements ListItemView<Question>
{
    private static final String TAG = AbstractQuestionListFragment.class.getSimpleName();

    private final Bundle bundle = new Bundle();
    private int position;

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

        Question question = itemListAdapter.getItem(position);
        if (item.getGroupId() == R.id.qContextMenuGroup)
        {
            Log.d(TAG, "Context item selected: " + item.getTitle());
            switch (item.getItemId())
            {
                case R.id.q_ctx_menu_user_profile:
                    showUserProfile(question.owner.id);
                    break;
                case R.id.q_ctx_similar:
                    startSimirarQuestionsActivity(question.title);
                    return true;
                case R.id.q_ctx_related:
                    startRelatedQuestionsActivity(question.id);
                    return true;
                case R.id.q_ctx_menu_email:
                    emailQuestion(question.title, question.link);
                    return true;
                default:
                    Log.d(TAG, "Unknown item in context menu: " + item.getTitle());
                    return false;
            }
        }
        else if (item.getGroupId() == R.id.qContextTagsMenuGroup)
        {
            Log.d(TAG, "Tag selected: " + item.getTitle());
            startTagQuestionsActivity((String) item.getTitle());
            return true;
        }

        return false;
    }

    @Override
    public View getView(Question item, View convertView, ViewGroup parent)
    {
        convertView = QuestionRowLayoutBuilder.getInstance().build(getActivity().getLayoutInflater(), getActivity(),
                        item);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.itemContextMenu);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().openContextMenu(v);
            }
        });
        return convertView;
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

    private void startSimirarQuestionsActivity(String title)
    {
        Intent questionsIntent = new Intent(getActivity(), QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.SIMILAR);
        questionsIntent.putExtra(StringConstants.TITLE, title);
        startActivity(questionsIntent);
    }

    private void startRelatedQuestionsActivity(long questionId)
    {
        Intent questionsIntent = new Intent(getActivity(), QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.RELATED);
        questionsIntent.putExtra(StringConstants.QUESTION_ID, questionId);
        startActivity(questionsIntent);
    }

    private void startTagQuestionsActivity(String tag)
    {
        Intent questionsIntent = new Intent(getActivity(), QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.TAG);
        questionsIntent.putExtra(StringConstants.TAG, tag);
        startActivity(questionsIntent);
    }

    private void showUserProfile(long userId)
    {
        Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        startActivity(userProfileIntent);
    }

    private void emailQuestion(String subject, String body)
    {
        Intent emailIntent = IntentUtils.createEmailIntent(subject, body);
        startActivity(Intent.createChooser(emailIntent, ""));
    }
}

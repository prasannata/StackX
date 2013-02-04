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

import java.util.ArrayList;

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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.QuestionActivity;
import com.prasanna.android.stacknetwork.QuestionsActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserAnswerListFragment extends ItemListFragment<Answer> implements ListItemView<Answer>
{
    private static final String TAG = UserAnswerListFragment.class.getSimpleName();
    private int page = 1;
    private Intent intent;
    private static final int ANSWER_PREVIEW_LEN = 200;
    private static final String ANS_CONTNUES = "...";
    private static final String MULTIPLE_NEW_LINES_AT_END = "[\\n]+$";

    private int position;
    private boolean activityCreated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "Creating answer fragment");

        if (itemsContainer == null)
        {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.list_view, null);
            itemListAdapter = new ItemListAdapter<Answer>(getActivity(), R.layout.answer_snippet,
                            new ArrayList<Answer>(), this);

            itemsContainer.removeView(itemsContainer.findViewById(R.id.scoreAndAns));
        }
        return itemsContainer;
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
        menu.removeItem(R.id.q_ctx_comments);
        menu.removeItem(R.id.q_ctx_menu_tags);
        menu.removeItem(R.id.q_ctx_similar);
        menu.removeItem(R.id.q_ctx_menu_user_profile);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        Log.d(TAG, "onContextItemSelected");

        if (item.getGroupId() == R.id.qContextMenuGroup)
        {

            Log.d(TAG, "Context item selected: " + item.getTitle());

            switch (item.getItemId())
            {
                case R.id.q_ctx_related:
                    Intent questionsIntent = new Intent(getActivity(), QuestionsActivity.class);
                    questionsIntent.setAction(StringConstants.RELATED);
                    questionsIntent.putExtra(StringConstants.QUESTION_ID, itemListAdapter.getItem(position).questionId);
                    startActivity(questionsIntent);
                    return true;
                case R.id.q_ctx_menu_email:
                    Intent emailIntent = IntentUtils.createEmailIntent(itemListAdapter.getItem(position).title,
                                    itemListAdapter.getItem(position).link);
                    startActivity(Intent.createChooser(emailIntent, ""));
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }

    @Override
    protected void startIntentService()
    {
        showProgressBar();

        intent = getIntentForService(UserIntentService.class, UserIntentAction.ANSWERS_BY_USER.getAction());
        intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_ANSWERS);
        intent.putExtra(StringConstants.ME, getActivity().getIntent().getBooleanExtra(StringConstants.ME, false));
        intent.putExtra(StringConstants.USER_ID, getActivity().getIntent().getLongExtra(StringConstants.USER_ID, 0L));
        intent.putExtra(StringConstants.PAGE, page++);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

        startService(intent);
    }

    @Override
    public String getReceiverExtraName()
    {
        return StringConstants.ANSWERS;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        registerForContextMenu(getListView());

        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());

        if (!activityCreated)
        {
            startIntentService();
            activityCreated = true;
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(getLogTag(), "onDestroy");

        super.onDestroy();

        stopService(intent);
    }

    @Override
    public void onStop()
    {
        Log.d(getLogTag(), "onStop");

        super.onStop();

        stopService(intent);
    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }

    @Override
    protected ViewGroup getParentLayout()
    {
        return itemsContainer;
    }

    @Override
    public View getView(final Answer answer, View convertView, ViewGroup parent)
    {
        LinearLayout answerRow = (LinearLayout) convertView;

        if (convertView == null)
            answerRow = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.answer_snippet, null);

        if (answer.accepted)
            answerRow.findViewById(R.id.acceptedAnswer).setVisibility(View.VISIBLE);

        TextView textView = (TextView) answerRow.findViewById(R.id.itemTitle);
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        textView.setLayoutParams(layoutParams);
        textView.setText(Html.fromHtml(answer.title));

        textView = (TextView) answerRow.findViewById(R.id.answerScore);
        textView.setText("Answer Score: " + answer.score);

        textView = (TextView) answerRow.findViewById(R.id.answerTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(answer.creationDate));

        textView = (TextView) answerRow.findViewById(R.id.answerBodyPreview);

        if (answer.body != null)
        {
            String answerBody = answer.body.replaceAll(MULTIPLE_NEW_LINES_AT_END, "\n");

            if (answerBody.length() > ANSWER_PREVIEW_LEN)
            {
                answerBody = answerBody.substring(0, ANSWER_PREVIEW_LEN);
                textView.setText(Html.fromHtml(answerBody + ANS_CONTNUES));
            }
            else
                textView.setText(Html.fromHtml(answerBody));
        }

        ImageView imageView = (ImageView) answerRow.findViewById(R.id.itemContextMenu);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().openContextMenu(v);
            }
        });
        return answerRow;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent intent = new Intent(getActivity(), QuestionActivity.class);
        intent.setAction(StringConstants.QUESTION_ID);
        intent.putExtra(StringConstants.QUESTION_ID, itemListAdapter.getItem(position).questionId);
        startActivity(intent);
    }
}

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
import android.util.Log;

import com.prasanna.android.stacknetwork.fragment.AnswerFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionFragment;
import com.prasanna.android.stacknetwork.intent.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.viewpagerindicator.TitlePageIndicator;

public class QuestionActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = QuestionActivity.class.getSimpleName();

    private Question question;
    private Intent questionIntent;
    private QuestionFragment questionFragment;
    private QuestionViewPageAdapter questionViewPageAdapter;
    private ViewPager viewPager;
    private TitlePageIndicator indicator;

    public class QuestionViewPageAdapter extends FragmentPagerAdapter
    {
        public QuestionViewPageAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public int getCount()
        {
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
                    return QuestionActivity.this.getString(R.string.accepted) + " "
                            + QuestionActivity.this.getString(R.string.answer);
                else
                    return QuestionActivity.this.getString(R.string.answer) + " " + position;
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
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_FULL_DETAILS.getExtra());

            questionFragment.displayBody(question.body);
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

        }
    };

    private BroadcastReceiver questionAnswersReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ArrayList<Answer> answers = (ArrayList<Answer>) intent
                    .getSerializableExtra(IntentActionEnum.QuestionIntentAction.QUESTION_ANSWERS.getExtra());

            if (answers != null && !answers.isEmpty())
            {
                if (question.answers == null)
                {
                    question.answers = new ArrayList<Answer>();

                }
                question.answers.addAll(answers);
                indicator.notifyDataSetChanged();
                questionViewPageAdapter.notifyDataSetChanged();
            }
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

        question = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

        questionFragment = new QuestionFragment();
        questionFragment.setQuestion(question);

        boolean cached = getIntent().getBooleanExtra(StringConstants.CACHED, false);

        if (cached == false)
        {
            registerReceivers();
            startQuestionService(IntentActionEnum.QuestionIntentAction.QUESTION_BODY.name());
        }
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

    private void startQuestionService(String intentAction)
    {
        questionIntent = new Intent(this, QuestionDetailsIntentService.class);
        questionIntent.setAction(intentAction);
        questionIntent.putExtra(StringConstants.QUESTION, question);
        startService(questionIntent);
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
    protected void refresh()
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected Context getCurrentContext()
    {
        return this;
    }

}

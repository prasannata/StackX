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
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.fragment.AnswerFragment;
import com.prasanna.android.stacknetwork.fragment.CommentFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionFragment;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.WriteObjectAsyncTask;
import com.viewpagerindicator.TitlePageIndicator;

public class QuestionActivity extends AbstractUserActionBarActivity implements
        OnPageChangeListener, StackXRestQueryResultReceiver
{
    private static final String TAG = QuestionActivity.class.getSimpleName();

    private Question question;
    private Intent intent;
    private QuestionFragment questionFragment;
    private QuestionViewPageAdapter questionViewPageAdapter;
    private ViewPager viewPager;
    private TitlePageIndicator titlePageIndicator;
    private RestQueryResultReceiver resultReceiver;

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
                return QuestionActivity.this.questionFragment;
            else
                return AnswerFragment.newFragment(question.answers.get(position - 1));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_title_indicator);

        resultReceiver = new RestQueryResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        if (questionFragment == null)
            questionFragment = QuestionFragment.newFragment();

        setupViewPager();

        prepareIntentAndStartService();
    }

    private void setupViewPager()
    {
        questionViewPageAdapter = new QuestionViewPageAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(questionViewPageAdapter);

        titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titlePageIndicator.setViewPager(viewPager);
        titlePageIndicator.setOnPageChangeListener(this);
    }

    private void prepareIntentAndStartService()
    {
        intent = new Intent(this, QuestionDetailsIntentService.class);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

        if (StringConstants.QUESTION_ID.equals(getIntent().getAction()))
        {
            intent.setAction(StringConstants.QUESTION_ID);
            intent.putExtra(StringConstants.QUESTION_ID,
                    getIntent().getLongExtra(StringConstants.QUESTION_ID, 0));
        }
        else
        {
            question = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

            if (question != null)
            {
                intent.setAction(StringConstants.QUESTION);
                questionFragment.setQuestion(question);
                intent.putExtra(StringConstants.QUESTION_ID, question.id);
                intent.putExtra(StringConstants.ANSWER_COUNT, question.answerCount);
            }
        }

        startService(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        hideRefreshActionAnimation();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (intent != null)
            stopService(intent);

        resultReceiver.setReceiver(null);
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
            if (question.answers != null && question.answers.size() == numAnswersDisplayed)
            {
                Log.d(TAG, "Fetch next page of answers");

                startRefreshAnimation();

                startServiceForAnswers();
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
                    if (question.comments != null && !question.comments.isEmpty())
                        displayCommentFragment();
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
                    Log.d(TAG, "Unknown item selected: " + item.getTitle());
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
                    Log.d(TAG, "Unknown item selected: " + item.getTitle());
                    return false;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        switch (resultCode)
        {
            case QuestionDetailsIntentService.RESULT_CODE_Q:
                displayQuestion(resultData);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_BODY:
                questionFragment.displayBody(resultData.getString(StringConstants.BODY));
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_COMMENTS:
                question.comments = (ArrayList<Comment>) resultData
                        .getSerializable(StringConstants.COMMENTS);
                questionFragment.setComments(question.comments);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_ANSWERS:
                displayAnswers((ArrayList<Answer>) resultData
                        .getSerializable(StringConstants.ANSWERS));
                hideRefreshActionAnimation();
                break;
            default:
                Log.d(TAG, "Unknown result code in receiver: " + resultCode);
                break;
        }
    }

    private void startServiceForAnswers()
    {
        Log.d(TAG, "Start service to get answers");

        intent = new Intent(this, QuestionDetailsIntentService.class);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
        intent.setAction(StringConstants.ANSWERS);
        intent.putExtra(StringConstants.QUESTION_ID, question.id);
        intent.putExtra(StringConstants.PAGE, getNextPageNumber());
        startService(intent);
    }

    private void displayQuestion(Bundle resultData)
    {
        question = (Question) resultData.getSerializable(StringConstants.QUESTION);

        if (questionFragment == null)
            questionFragment = QuestionFragment.newFragment();

        questionFragment.setAndDisplay(question);

        if (question.answerCount > 0 && (question.answers == null || question.answers.isEmpty()))
            startServiceForAnswers();
        else
            displayAnswers(question.answers);
    }

    private void displayCommentFragment()
    {
        CommentFragment fragment = new CommentFragment();
        fragment.setComments(question.comments);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment, question.id + "-"
                + StringConstants.COMMENTS);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayAnswers(ArrayList<Answer> answers)
    {
        if (answers != null && !answers.isEmpty())
        {
            if (question.answers == null)
                question.answers = new ArrayList<Answer>();

            question.answers.addAll(answers);
            titlePageIndicator.notifyDataSetChanged();
            questionViewPageAdapter.notifyDataSetChanged();
        }
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
        WriteObjectAsyncTask cacheTask = new WriteObjectAsyncTask(directory,
                String.valueOf(question.id));
        cacheTask.execute(question);

        Toast.makeText(this, "Question saved", Toast.LENGTH_SHORT).show();
    }

    private int getNextPageNumber()
    {
        int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;
        return (numAnswersDisplayed / 10) + 1;
    }
}

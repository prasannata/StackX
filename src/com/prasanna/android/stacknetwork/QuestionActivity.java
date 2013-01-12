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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

public class QuestionActivity extends AbstractUserActionBarActivity implements OnPageChangeListener,
                StackXRestQueryResultReceiver, PageSelectAdapter
{
    private static final String TAG = QuestionActivity.class.getSimpleName();

    private Question question;
    private Intent intent;
    private QuestionFragment questionFragment;
    private QuestionViewPageAdapter questionViewPageAdapter;
    private ViewPager viewPager;
    private TitlePageIndicator titlePageIndicator;
    private RestQueryResultReceiver resultReceiver;
    private boolean serviceRunningForAnswers = false;

    private CommentFragment commentFragment;

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
                return AnswerFragment.newFragment(question.answers.get(position - 1), QuestionActivity.this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_title_indicator);

        resultReceiver = new RestQueryResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        if (questionFragment == null)
            questionFragment = QuestionFragment.newFragment();

        setupViewPager();
        
        prepareIntentAndStartService();
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean ret = super.onPrepareOptionsMenu(menu);

        if (menu != null)
            menu.removeItem(R.id.menu_search);

        return ret;
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

        setProgressBarIndeterminateVisibility(true);

        if (StringConstants.QUESTION_ID.equals(getIntent().getAction()))
        {
            long questionId = getIntent().getLongExtra(StringConstants.QUESTION_ID, 0);
            intent.setAction(StringConstants.QUESTION_ID);
            intent.putExtra(StringConstants.QUESTION_ID, questionId);
            startService(intent);
        }
        else
        {
            question = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

            if (question != null)
            {
                questionFragment.setQuestion(question);

                intent.setAction(StringConstants.QUESTION);
                intent.putExtra(StringConstants.QUESTION_ID, question.id);
                intent.putExtra(StringConstants.QUESTION, question);
                startService(intent);
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (intent != null)
            stopService(intent);
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

        if (commentFragment != null && commentFragment.isVisible())
        {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.remove(commentFragment);
            transaction.commit();
        }

        if (numAnswersDisplayed < question.answerCount && numAnswersDisplayed - position < 2)
        {
            if (question.answers != null && question.answers.size() == numAnswersDisplayed && !serviceRunningForAnswers)
            {
                Log.d(TAG, "Fetch next page of answers");

                setProgressBarIndeterminateVisibility(true);
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
                    displayQuestionComments();
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

            Answer answer = question.answers.get(answerPosition);

            switch (item.getItemId())
            {
                case R.id.q_ctx_comments:
                    displayAnswerComments(answer);
                    return true;
                case R.id.q_ctx_menu_user_profile:
                    viewUserProfile(answer.owner.id);
                    return true;
                default:
                    Log.d(TAG, "Unknown item selected: " + item.getTitle());
                    return false;
            }
        }

        return false;
    }

    private void displayQuestionComments()
    {
        if (question.comments != null && !question.comments.isEmpty())
            displayCommentFragment(question.id + "-" + StringConstants.COMMENTS, question.comments);
        else
            Toast.makeText(this, "No comments for question", Toast.LENGTH_SHORT).show();
    }

    private void displayAnswerComments(Answer answer)
    {
        if (answer.comments != null && !answer.comments.isEmpty())
            displayCommentFragment(answer.id + "-" + StringConstants.COMMENTS, answer.comments);
        else
            Toast.makeText(this, "No comments for answer", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        switch (resultCode)
        {
            case QuestionDetailsIntentService.RESULT_CODE_Q:
                question = (Question) resultData.getSerializable(StringConstants.QUESTION);
                setProgressBarIndeterminateVisibility(false);
                displayQuestion();
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_BODY:
                questionFragment.displayBody(resultData.getString(StringConstants.BODY));
                if (question.answerCount == 0)
                    setProgressBarIndeterminateVisibility(false);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_COMMENTS:
                question.comments = (ArrayList<Comment>) resultData.getSerializable(StringConstants.COMMENTS);
                questionFragment.setComments(question.comments);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_ANSWERS:
                serviceRunningForAnswers = false;
                setProgressBarIndeterminateVisibility(false);
                displayAnswers((ArrayList<Answer>) resultData.getSerializable(StringConstants.ANSWERS));
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
        serviceRunningForAnswers = true;
    }

    private void displayQuestion()
    {
        if (questionFragment == null)
            questionFragment = QuestionFragment.newFragment();

        questionFragment.setAndDisplay(question);

        if (question.answerCount > 0 && (question.answers == null || question.answers.isEmpty()))
            startServiceForAnswers();

        titlePageIndicator.notifyDataSetChanged();
        questionViewPageAdapter.notifyDataSetChanged();
    }

    private void displayCommentFragment(String fragmentTag, ArrayList<Comment> comments)
    {
        commentFragment = (CommentFragment) getFragmentManager().findFragmentByTag(fragmentTag);

        if (commentFragment == null)
        {
            Log.d(TAG, "Creating comment fragment for question: " + question.id);
            commentFragment = new CommentFragment();
            commentFragment.setComments(comments);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, commentFragment, fragmentTag);
        transaction.commit();
    }

    private void displayAnswers(ArrayList<Answer> answers)
    {
        if (answers != null && !answers.isEmpty())
        {
            if (question.answers == null)
                question.answers = new ArrayList<Answer>();

            if (!StringConstants.QUESTION_ID.equals(getIntent().getAction()))
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
        WriteObjectAsyncTask cacheTask = new WriteObjectAsyncTask(directory, String.valueOf(question.id));
        cacheTask.execute(question);

        Toast.makeText(this, "Question saved", Toast.LENGTH_SHORT).show();
    }

    private int getNextPageNumber()
    {
        int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;
        return (numAnswersDisplayed / 10) + 1;
    }

    @Override
    public void selectQuestionPage()
    {
        if (viewPager != null && viewPager.getAdapter().getCount() > 0)
        {
            final int currentItem = viewPager.getCurrentItem();

            questionFragment.enableNavigationBack(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    viewPager.setCurrentItem(currentItem);
                }
            });

            viewPager.setCurrentItem(0);
        }
    }
}

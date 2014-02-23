/*
 Copyright (C) 2014 Prasanna Thirumalai

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
import java.util.HashMap;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.fragment.AnswerFragment;
import com.prasanna.android.stacknetwork.fragment.CommentFragment;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnCommentChangeListener;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnShowCommentsListener;
import com.prasanna.android.stacknetwork.fragment.PostCommentFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionFragment;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.viewpagerindicator.TitlePageIndicator;

public class QuestionActivity extends AbstractUserActionBarActivity implements OnPageChangeListener,
    StackXRestQueryResultReceiver, PageSelectAdapter, OnShowCommentsListener {
  private boolean serviceRunningForAnswers = false;
  private boolean serviceRunningForFavorite = false;
  private Question question;
  private QuestionViewPageAdapter questionViewPageAdapter;
  private ViewPager viewPager;
  private TitlePageIndicator titlePageIndicator;
  private RestQueryResultReceiver resultReceiver;
  private QuestionFragment questionFragment;
  private CommentFragment commentFragment;
  private PostCommentFragment postCommentFragment;
  private HashMap<String, String> commentsDraft = new HashMap<String, String>();
  private Menu menu;

  public class QuestionViewPageAdapter extends FragmentPagerAdapter {
    public QuestionViewPageAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public int getCount() {
      if (question == null) return 1;
      else return question.answers == null ? 1 : 1 + question.answers.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position == 0) return QuestionActivity.this.getString(R.string.question);
      else {
        if (question.answers.get(position - 1).accepted) return QuestionActivity.this.getString(R.string.accepted)
            + " " + QuestionActivity.this.getString(R.string.answer);
        else return QuestionActivity.this.getString(R.string.answer) + " " + position;
      }
    }

    @Override
    public Fragment getItem(int position) {
      if (position == 0) return QuestionActivity.this.questionFragment;
      else {
        Fragment fragment = getFragmentManager().findFragmentByTag(getViewPagerFragmentTag(position));

        if (fragment == null)
          return AnswerFragment.newFragment(question.answers.get(position - 1), QuestionActivity.this,
              question.owner.id == OperatingSite.getSite().userId, question.hasAcceptedAnswer);

        return fragment;
      }
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.viewpager_title_indicator);

    resultReceiver = new RestQueryResultReceiver(new Handler());
    resultReceiver.setReceiver(this);

    if (questionFragment == null) questionFragment = QuestionFragment.newFragment();

    setupViewPager();

    if (savedInstanceState != null && savedInstanceState.getSerializable(StringConstants.QUESTION) != null) question =
        (Question) savedInstanceState.getSerializable(StringConstants.QUESTION);
    else prepareIntentAndStartService();
  }

  @Override
  protected boolean shouldSearchViewBeEnabled() {
    return false;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean ret = super.onPrepareOptionsMenu(menu);

    if (menu != null) {
      this.menu = menu;
      setupMenuIfAuthenticatedUser();
    }

    return ret;
  }

  private MenuItem setupFavoriteActionBarIcon() {
    MenuItem favorite = menu.findItem(R.id.menu_favorite);
    if (question != null && question.favorited) {
      favorite.setIcon(R.drawable.favorited);
    } else {
      favorite.setIcon(R.drawable.favorite);
    }
    return favorite;
  }

  private void setupMenuIfAuthenticatedUser() {
    if (AppUtils.inAuthenticatedRealm(this)) {
      MenuItem favorite = setupFavoriteActionBarIcon();
      favorite.setVisible(true);
      menu.findItem(R.id.menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      setupAddComment(menu);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_favorite:
        startServiceToFavoriteQuestion();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void startServiceToFavoriteQuestion() {
    if (!serviceRunningForFavorite) {
      Intent intent = new Intent(this, QuestionDetailsIntentService.class);
      if (question.favorited) intent.putExtra(StringConstants.ACTION, QuestionDetailsIntentService.FAVORITE_UNDO);
      else intent.putExtra(StringConstants.ACTION, QuestionDetailsIntentService.FAVORITE);
      intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
      intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
      intent.putExtra(StringConstants.ID, question.id);
      startService(intent);
      serviceRunningForFavorite = true;
    }
  }

  private void setupAddComment(Menu menu) {
    MenuItem newCommentMenuItem = menu.findItem(R.id.menu_new_comment);
    newCommentMenuItem.setVisible(true);
    newCommentMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setupPostCommentFragment(null);
      }
    });
  }

  private void setupPostCommentFragment(String defaultText) {
    if (viewPager.getCurrentItem() == 0) {
      displayPostCommentFragment("Comment on question by " + question.owner.displayName, question.id, 0, question.id
          + "-comment", defaultText);
    } else {
      Answer answer = question.answers.get(viewPager.getCurrentItem() - 1);
      displayPostCommentFragment("Comment on answer by " + answer.owner.getDisplayName(), answer.id,
          viewPager.getCurrentItem(), answer.id + "-comment", defaultText);
    }
  }

  private void displayPostCommentFragment(String title, long id, int viewPagerPosition, String fragmentTag,
      String defaultText) {
    postCommentFragment = new PostCommentFragment();
    postCommentFragment.setPostId(id);
    postCommentFragment.setViewPagerPosition(viewPagerPosition);
    postCommentFragment.setTitle(title);
    postCommentFragment.setResultReceiver(resultReceiver);

    if (commentsDraft.get(fragmentTag) != null) postCommentFragment.setDraftText(commentsDraft.get(fragmentTag));

    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.fragmentContainer, postCommentFragment, fragmentTag);
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    transaction.addToBackStack(fragmentTag);
    transaction.commit();
  }

  private void setupViewPager() {
    questionViewPageAdapter = new QuestionViewPageAdapter(getFragmentManager());
    viewPager = (ViewPager) findViewById(R.id.viewPager);
    viewPager.setAdapter(questionViewPageAdapter);

    titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
    titlePageIndicator.setViewPager(viewPager);
    titlePageIndicator.setOnPageChangeListener(this);
  }

  private void prepareIntentAndStartService() {
    Intent intent = new Intent(this, QuestionDetailsIntentService.class);
    intent.putExtra(StringConstants.ACTION, QuestionDetailsIntentService.QA);
    intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

    setProgressBarIndeterminateVisibility(true);

    if (StringConstants.QUESTION_ID.equals(getIntent().getAction())) {
      long questionId = getIntent().getLongExtra(StringConstants.QUESTION_ID, 0);
      intent.setAction(StringConstants.QUESTION_ID);
      intent.putExtra(StringConstants.QUESTION_ID, questionId);
      intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
      startService(intent);
    } else {
      Question metaDetails = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

      if (metaDetails != null) question = Question.copyMetaDeta(metaDetails);

      if (question != null) {
        questionFragment.setQuestion(question);

        intent.setAction(StringConstants.QUESTION);
        intent.putExtra(StringConstants.QUESTION_ID, question.id);
        intent.putExtra(StringConstants.QUESTION, question);
        intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
        intent.putExtra(StringConstants.REFRESH, getIntent().getBooleanExtra(StringConstants.REFRESH, false));
        startService(intent);
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (commentFragment != null && commentFragment.isVisible()) commentFragment.onBackPressed();

    super.onBackPressed();
    dismissPostCommentFragmentIfVisible(false);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (question != null) outState.putSerializable(StringConstants.QUESTION, question);

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void refresh() {
    Intent intent = getIntent();
    finish();
    intent.putExtra(StringConstants.REFRESH, true);
    startActivity(intent);
  }

  @Override
  public void onPageScrollStateChanged(int arg0) {
  }

  @Override
  public void onPageScrolled(int arg0, float arg1, int arg2) {
  }

  @Override
  public void onPageSelected(int position) {
    int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;

    if (commentFragment != null && commentFragment.isVisible()) {
      getFragmentManager().popBackStackImmediate();
      commentFragment = null;
    }

    dismissPostCommentFragmentIfVisible(false);

    if (numAnswersDisplayed < question.answerCount && numAnswersDisplayed - position < 2) {
      if (question.answers != null && question.answers.size() == numAnswersDisplayed && !serviceRunningForAnswers) {
        setProgressBarIndeterminateVisibility(true);
        startServiceForAnswers();
      }
    }
  }

  private void dismissPostCommentFragmentIfVisible(boolean finish) {
    if (postCommentFragment != null) {
      postCommentFragment.hideSoftKeyboard();

      if (!finish) commentsDraft.put(postCommentFragment.getTag(), postCommentFragment.getCurrentText());

      getFragmentManager().popBackStackImmediate();
      postCommentFragment = null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    switch (resultCode) {
      case QuestionDetailsIntentService.RESULT_CODE_Q_CACHED:
        setProgressBarIndeterminateVisibility(false);
      case QuestionDetailsIntentService.RESULT_CODE_Q:
        question = (Question) resultData.getSerializable(StringConstants.QUESTION);
        displayQuestion();
        if (question.answerCount > 0 && (question.answers == null || question.answers.isEmpty())) startServiceForAnswers();
        else setProgressBarIndeterminateVisibility(false);
        break;
      case QuestionDetailsIntentService.RESULT_CODE_Q_BODY:
        questionFragment.displayBody(resultData.getString(StringConstants.BODY));
        if (question.answerCount == 0) setProgressBarIndeterminateVisibility(false);
        break;
      case QuestionDetailsIntentService.RESULT_CODE_Q_COMMENTS:
        question.comments = (ArrayList<Comment>) resultData.getSerializable(StringConstants.COMMENTS);
        questionFragment.setComments(question.comments);
        break;
      case QuestionDetailsIntentService.RESULT_CODE_ANSWERS:
        serviceRunningForAnswers = false;
        setProgressBarIndeterminateVisibility(false);
        displayAnswers((ArrayList<Answer>) resultData.getSerializable(StringConstants.ANSWERS), true);
        break;
      case WriteIntentService.ACTION_ADD_COMMENT:
        dismissPostCommentFragmentIfVisible(true);
        addMyCommentToPost(resultData);
        break;
      case QuestionDetailsIntentService.RESULT_CODE_FAVORITE_SUCCESS:
        serviceRunningForFavorite = false;
        question.favorited = true;
        setupFavoriteActionBarIcon();
        break;
      case QuestionDetailsIntentService.RESULT_CODE_FAVORITE_UNDO_SUCCESS:
        serviceRunningForFavorite = false;
        question.favorited = false;
        setupFavoriteActionBarIcon();
        break;
      case QuestionDetailsIntentService.ERROR:
        if (serviceRunningForAnswers) serviceRunningForAnswers = false;
        if (serviceRunningForFavorite) serviceRunningForFavorite = false;
        handleError(resultData);
        break;
      default:
        break;
    }
  }

  private void handleError(Bundle resultData) {
    setProgressBarIndeterminateVisibility(false);

    HttpException e = (HttpException) resultData.getSerializable(StringConstants.EXCEPTION);
    int requestCode = resultData.getInt(StringConstants.REQUEST_CODE, -1);

    if (requestCode == WriteIntentService.ACTION_ADD_COMMENT) {
      PostCommentFragment lastPostCommentFragment = findFragmentByTag("postCommentFragment", PostCommentFragment.class);
      if (lastPostCommentFragment != null) lastPostCommentFragment.setSendError(e.getErrorResponse());
      else if (postCommentFragment != null) postCommentFragment.setSendError(e.getErrorResponse());
    } else AppUtils.getErrorView(this, e);
  }

  protected void addMyCommentToPost(Bundle resultData) {
    SharedPreferencesUtil.setLong(this, WritePermission.PREF_LAST_COMMENT_WRITE, System.currentTimeMillis());
    Comment comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
    int viewPagerPosition = resultData.getInt(StringConstants.VIEW_PAGER_POSITION, -1);
    if (viewPagerPosition == 0) {
      if (question.comments == null) question.comments = new ArrayList<Comment>();

      question.comments.add(comment);
      questionFragment.setComments(question.comments);
    } else {
      AnswerFragment answerFragment =
          findFragmentByTag(getViewPagerFragmentTag(viewPagerPosition), AnswerFragment.class);
      answerFragment.onCommentAdd(comment);
    }
  }

  private void startServiceForAnswers() {
    Intent intent = new Intent(this, QuestionDetailsIntentService.class);
    intent.setAction(StringConstants.ANSWERS);
    intent.putExtra(StringConstants.ACTION, QuestionDetailsIntentService.QA);
    intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
    intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
    intent.putExtra(StringConstants.QUESTION_ID, question.id);
    intent.putExtra(StringConstants.PAGE, getNextPageNumber());
    startService(intent);
    serviceRunningForAnswers = true;
  }

  private void displayQuestion() {
    if (questionFragment == null) questionFragment = QuestionFragment.newFragment();

    questionFragment.setAndDisplay(question);
    titlePageIndicator.notifyDataSetChanged();
    questionViewPageAdapter.notifyDataSetChanged();
  }

  private void displayAnswers(ArrayList<Answer> answers, boolean add) {
    if (answers != null && !answers.isEmpty()) {
      if (question.answers == null) question.answers = new ArrayList<Answer>();

      if (add && !StringConstants.QUESTION_ID.equals(getIntent().getAction())) question.answers.addAll(answers);

      titlePageIndicator.notifyDataSetChanged();
      questionViewPageAdapter.notifyDataSetChanged();
    }
  }

  private int getNextPageNumber() {
    int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;
    return (numAnswersDisplayed / 10) + 1;
  }

  @Override
  public void selectQuestionPage() {
    if (viewPager != null && viewPager.getAdapter().getCount() > 0) {
      final int currentItem = viewPager.getCurrentItem();

      questionFragment.enableNavigationBack(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          viewPager.setCurrentItem(currentItem);
        }
      });

      viewPager.setCurrentItem(0);
    }
  }

  private String getViewPagerFragmentTag(int position) {
    return "android:switcher:" + R.id.viewPager + ":" + position;
  }

  @Override
  public void onShowComments() {
    if (viewPager.getCurrentItem() == 0) prepareCommentFragment(question.id, question.comments);
    else {
      Answer answer = question.answers.get(viewPager.getCurrentItem() - 1);
      prepareCommentFragment(answer.id, answer.comments);
    }
  }

  private void prepareCommentFragment(long postId, ArrayList<Comment> comments) {
    if (comments != null && !comments.isEmpty()) showCommentFragment(postId, comments);
    else Toast.makeText(this, "No comments", Toast.LENGTH_SHORT).show();
  }

  private void showCommentFragment(long postId, ArrayList<Comment> comments) {
    String fragmentTag = postId + "-" + StringConstants.COMMENTS;
    commentFragment = findFragmentByTag(fragmentTag, CommentFragment.class);

    if (commentFragment == null) {
      commentFragment = new CommentFragment();
      commentFragment.setComments(comments);
      commentFragment.setResultReceiver(resultReceiver);

      String currentViewPagerFragmentTag = "android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem();

      OnCommentChangeListener onCommentChangeListener =
          (OnCommentChangeListener) getFragmentManager().findFragmentByTag(currentViewPagerFragmentTag);

      if (onCommentChangeListener != null) commentFragment.setOnCommentChangeListener(onCommentChangeListener);
    }

    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.fragmentContainer, commentFragment, fragmentTag);
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    transaction.addToBackStack(fragmentTag);
    transaction.commit();
  }
}

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

import com.prasanna.android.stacknetwork.fragment.AnswersFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionFragment;
import com.prasanna.android.stacknetwork.intent.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class QuestionActivity extends AbstractUserActionBarActivity
{
    private static final String TAG = QuestionActivity.class.getSimpleName();

    private Question question;
    private Intent questionIntent;
    private AnswersFragment answersFragment;
    private QuestionFragment questionFragment;

    public class TabListener implements ActionBar.TabListener
    {
        private final Fragment fragment;

        public TabListener(Fragment fragment)
        {
            this.fragment = fragment;
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            Log.d(TAG, "Selected tab:" + tab.getText());

            ft.add(R.id.fragmentContainer, fragment, null);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            ft.remove(fragment);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
            onTabUnselected(tab, ft);
            onTabSelected(tab, ft);
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

            answersFragment.setAnswers(answers);
        }
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ll_whitebg_vertical);

        question = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

        answersFragment = new AnswersFragment();
        questionFragment = new QuestionFragment();
        questionFragment.setQuestion(question);

        setupActionBarTabs();

        boolean cached = getIntent().getBooleanExtra(StringConstants.CACHED, false);

        if (cached == false)
        {
            registerReceivers();
            startQuestionService(IntentActionEnum.QuestionIntentAction.QUESTION_BODY.name());
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void setupActionBarTabs()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(OperatingSite.getSite().name);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab questionTab = actionBar.newTab();
        questionTab.setText(StringConstants.QUESTION).setContentDescription(StringConstants.QUESTION)
                .setTabListener(new TabListener(questionFragment));
        actionBar.addTab(questionTab);

        Tab answersTab = actionBar.newTab();
        answersTab.setText(StringConstants.ANSWERS + " (" + question.answerCount + ")")
                .setContentDescription(StringConstants.ANSWERS).setTabListener(new TabListener(answersFragment));
        actionBar.addTab(answersTab);
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

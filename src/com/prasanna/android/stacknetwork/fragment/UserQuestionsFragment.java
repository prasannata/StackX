package com.prasanna.android.stacknetwork.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserQuestionsFragment extends AbstractQuestionsFragment
{
    private static final String TAG = UserQuestionsFragment.class.getSimpleName();
    private LinearLayout scrollViewContainer;
    private ScrollViewWithNotifier itemScroller;
    private User user;
    private Intent intent;
    private int page = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        page = 0;
        
        if (items == null || items.isEmpty() == true)
        {
            user = (User) getActivity().getIntent().getSerializableExtra(StringConstants.USER);

            registerReceiver();

            startIntentService();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "Creating question fragment");

        super.onCreateView(inflater, container, savedInstanceState);

        scrollViewContainer = (LinearLayout) inflater.inflate(R.layout.items_scroll_layout, null);
        itemScroller = (ScrollViewWithNotifier) scrollViewContainer.findViewById(R.id.itemScroller);
        itemScroller.addView(itemsContainer);
        itemScroller.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
        {
            @Override
            public void onScrollToBottom(View view)
            {
                if (loadingProgressView == null)
                {
                    loadingProgressView = (LinearLayout) getActivity().getLayoutInflater().inflate(
                            R.layout.loading_progress, null);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 15, 0, 15);
                    itemsContainer.addView(loadingProgressView, layoutParams);
                }

                startIntentService();
            }
        });

        loadingDialog = ProgressDialog.show(getActivity(), "", "Loading questions");
        
        if (items != null && items.isEmpty() == false)
        {
            displayItems();
        }

        return scrollViewContainer;
    }

    
    @Override
    protected void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void startIntentService()
    {
        intent = getIntentForService(UserQuestionsIntentService.class, IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        intent.putExtra(StringConstants.USER_ID, user.id);
        intent.putExtra(StringConstants.PAGE, ++page);
        intent.putExtra(StringConstants.ACCESS_TOKEN, user.accessToken);
        getActivity().startService(intent);
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }
}

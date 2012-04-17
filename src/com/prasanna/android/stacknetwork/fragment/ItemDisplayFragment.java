package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.BaseStackExchangeItem;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.IntentAction;

public abstract class ItemDisplayFragment<T extends BaseStackExchangeItem> extends Fragment implements
        ScrollableFragment
{
    protected boolean serviceRunning = false;

    protected LinearLayout itemsContainer;

    protected ArrayList<T> items = new ArrayList<T>();

    protected ProgressDialog loadingDialog;

    protected BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(getLogTag(), "Receiver invoked: " + intent.getExtras());

            items.addAll((ArrayList<T>) intent.getSerializableExtra(getReceiverExtraName().getExtra()));

            displayItems();
        }
    };

    protected LinearLayout loadingProgressView;

    private Intent intentForService;

    public abstract IntentAction getReceiverExtraName();

    public abstract void startIntentService();
    
    protected abstract void registerReceiver();

    protected abstract void displayItems();

    protected abstract String getLogTag();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null)
        {
            Log.d(getLogTag(), "onCreateView return null");
            return null;
        }

        itemsContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.items_fragment_container,
                null);

        return itemsContainer;
    }

    @Override
    public void onResume()
    {
        registerReceiver();
        super.onResume();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopServiceAndUnregisterReceiver();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        stopServiceAndUnregisterReceiver();
    }

    private void stopServiceAndUnregisterReceiver()
    {
        if (intentForService != null)
        {
            getActivity().stopService(intentForService);
        }

        try
        {
            getActivity().unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(getLogTag(), e.getMessage());
        }
    }

    @Override
    public void onScrollToBottom()
    {
        if (serviceRunning == false)
        {
            loadingProgressView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.loading_progress,
                    null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 15, 0, 15);
            itemsContainer.addView(loadingProgressView, layoutParams);
            startIntentService();
        }
    }

    protected Intent getIntentForService(Class<?> clazz, String action)
    {
        intentForService = new Intent(getActivity().getApplicationContext(), clazz);
        intentForService.setAction(action);
        return intentForService;
    }
}

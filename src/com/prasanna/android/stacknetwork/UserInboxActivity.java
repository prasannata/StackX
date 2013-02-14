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
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserInboxActivity extends AbstractUserActionBarActivity implements OnScrollListener,
                StackXRestQueryResultReceiver, ListItemView<InboxItem>
{
    private static final String TAG = UserInboxActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private ListView listView;
    private Intent intent = null;
    private int page = 0;
    private RestQueryResultReceiver receiver;
    private ItemListAdapter<InboxItem> itemListAdapter;
    private boolean serviceRunning = false;
    protected List<StackXPage<InboxItem>> pages;
    private StackXPage<InboxItem> currentPageObject;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        getActionBar().setTitle(getString(R.string.inbox));

        receiver = new RestQueryResultReceiver(new Handler());
        receiver.setReceiver(this);

        if (progressBar == null)
            progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.progress_bar, null);

        if (pages == null)
            pages = new ArrayList<StackXPage<InboxItem>>();

        setContentView(R.layout.list_view);

        setupListView();

        startIntentService();
    }

    private void setupListView()
    {
        listView = (ListView) findViewById(android.R.id.list);
        listView.addFooterView(progressBar);
        itemListAdapter = new ItemListAdapter<InboxItem>(getApplicationContext(), R.layout.inbox_item,
                        new ArrayList<InboxItem>(), this);
        listView.setAdapter(itemListAdapter);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
            {
                InboxItem item = (InboxItem) adapter.getItemAtPosition(position);

                if (item.itemType != null)
                {
                    boolean isSupportedItem = item.itemType.equals(InboxItem.ItemType.COMMENT)
                                    || item.itemType.equals(InboxItem.ItemType.NEW_ANSWER);

                    if (isSupportedItem)
                    {
                        Intent intent = new Intent(UserInboxActivity.this, InboxItemActivity.class);
                        intent.putExtra(StringConstants.INBOX_ITEM, item);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(UserInboxActivity.this, "Sorry, this message type not supported by application",
                                        Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (itemListAdapter != null)
            itemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (intent != null)
            stopService(intent);
    }

    private void startIntentService()
    {
        if (!serviceRunning)
        {
            SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());
            if (sharedPreferences.contains(StringConstants.ACCESS_TOKEN))
            {
                intent = new Intent(this, UserIntentService.class);
                intent.setAction(StringConstants.INBOX_ITEMS);
                intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_INBOX);
                intent.putExtra(StringConstants.PAGE, ++page);
                intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);

                progressBar.setVisibility(View.VISIBLE);

                startService(intent);

                serviceRunning = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);

        menu.removeItem(R.id.menu_my_inbox);

        return ret & true;
    }

    @Override
    public void refresh()
    {
        itemListAdapter.clear();
        page = 0;
        startIntentService();
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        serviceRunning = false;
        progressBar.setVisibility(View.GONE);

        currentPageObject = (StackXPage<InboxItem>) resultData.getSerializable(StringConstants.INBOX_ITEMS);

        if (currentPageObject != null)
        {
            pages.add(currentPageObject);
            itemListAdapter.addAll(currentPageObject.items);
        }
    }

    @Override
    public View getView(InboxItem item, int position, View convertView, ViewGroup parent)
    {
        RelativeLayout itemRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.inbox_item, null);

        TextView textView = (TextView) itemRow.findViewById(R.id.itemTitle);
        textView.setText(Html.fromHtml(item.title));

        if (item.body != null)
        {
            textView = (TextView) itemRow.findViewById(R.id.itemBodyPreview);
            textView.setText(Html.fromHtml(item.body));
        }

        textView = (TextView) itemRow.findViewById(R.id.itemCreationTime);
        textView.setText(DateTimeUtils.getElapsedDurationSince(item.creationDate));

        textView = (TextView) itemRow.findViewById(R.id.itemType);
        textView.setText(item.itemType.getRepr());

        if (item.site != null)
        {
            textView = (TextView) itemRow.findViewById(R.id.itemSite);
            textView.setText(item.site.name);
        }

        return itemRow;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (!serviceRunning && totalItemCount >= StackUri.QueryParamDefaultValues.PAGE_SIZE
                        && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1))
        {
            if (currentPageObject != null && currentPageObject.hasMore)
            {
                Log.d(TAG, "onScroll reached bottom threshold. Fetching more questions");
                progressBar.setVisibility(View.VISIBLE);
                startIntentService();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        Log.v(TAG, "onScrollStateChanged");
    }
}

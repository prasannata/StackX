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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.cache.BitmapCache;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.AsyncTaskExecutor;
import com.prasanna.android.task.GetImageAsyncTask;

public class UserInboxActivity extends AbstractUserActionBarActivity implements OnScrollListener,
    StackXRestQueryResultReceiver, ListItemView<InboxItem> {

  private ProgressBar progressBar;
  private ListView listView;
  private MultiAutoCompleteTextView searchInputText;
  private Button clearFilterInputText;
  private Intent intent = null;
  private int page = 0;
  private RestQueryResultReceiver receiver;
  private ItemListAdapter<InboxItem> itemListAdapter;
  private boolean serviceRunning = false;
  protected List<StackXPage<InboxItem>> pages = new ArrayList<StackXPage<InboxItem>>();
  private StackXPage<InboxItem> currentPageObject;
  private CharSequence searchHint;
  private HashSet<String> autocompleteOptions = new HashSet<String>();
  private ArrayAdapter<String> searchOptionsAdapter;

  public class InboxFilter extends Filter {
    private Object filterLock = new Object();

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults result = new FilterResults();
      ArrayList<InboxItem> filteredInboxItems = new ArrayList<InboxItem>();
      synchronized (filterLock) {
        if (constraint != null && constraint.length() > 0) {
          for (StackXPage<InboxItem> page : pages) {
            for (InboxItem inboxItem : page.items) {
              String[] words = constraint.toString().split(",");
              boolean match = true;
              for (String word : words) {
                String trimmedWord = word.trim();
                boolean titleMatch =
                    Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE).matcher(inboxItem.title).find();
                if (!inboxItem.itemType.getRepr().contains(trimmedWord)
                    && !inboxItem.site.apiSiteParameter.contains(trimmedWord) && !titleMatch) {
                  match = false;
                  break;
                }
              }

              if (match) {
                filteredInboxItems.add(inboxItem);
              }
            }
          }

          result.count = filteredInboxItems.size();
          result.values = filteredInboxItems;
        } else {

          for (StackXPage<InboxItem> page : pages) {
            for (InboxItem inboxItem : page.items) {
              filteredInboxItems.add(inboxItem);
            }
          }
          result.count = filteredInboxItems.size();
          result.values = filteredInboxItems;
        }

      }
      return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void publishResults(CharSequence constraint, FilterResults results) {
      ArrayList<InboxItem> filteredTags = (ArrayList<InboxItem>) results.values;

      itemListAdapter.notifyDataSetChanged();
      itemListAdapter.clear();

      itemListAdapter.addAll(filteredTags);
      itemListAdapter.notifyDataSetInvalidated();
    }

  }

  static class InboxItemViewHolder {
    TextView title;
    TextView body;
    TextView creationTime;
    TextView itemType;
    TextView itemSite;
    ImageView siteIcon;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.inbox);

    receiver = new RestQueryResultReceiver(new Handler());
    receiver.setReceiver(this);

    if (progressBar == null)
      progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.progress_bar, null);

    setupSearch();
    setupListView();
    startIntentService();
  }

  private void setupSearch() {
    searchOptionsAdapter = new ArrayAdapter<String>(this, R.layout.tag_include_exclude, new ArrayList<String>());
    searchInputText = (MultiAutoCompleteTextView) findViewById(R.id.search);
    searchInputText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    searchInputText.setAdapter(searchOptionsAdapter);
    searchHint = searchInputText.getHint();
    clearFilterInputText = (Button) findViewById(R.id.clearTextAndFocus);

    searchInputText.setOnFocusChangeListener(new OnFocusChangeListener() {

      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          searchInputText.setHint("");
          clearFilterInputText.setVisibility(View.VISIBLE);
        } else
          clearFilterInputText.setVisibility(View.GONE);
      }
    });

    searchInputText.addTextChangedListener(new TextWatcher() {

      private int lastCount;

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        lastCount = count;
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.length() < lastCount || s.length() > 3)
          itemListAdapter.getFilter().filter(s);
      }
    });

    clearFilterInputText.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        searchInputText.setText("");
        searchInputText.setHint(searchHint);
        listView.requestFocus();
        AppUtils.hideSoftInput(getApplicationContext(), v);
      }
    });

  }

  private void setupListView() {
    listView = (ListView) findViewById(android.R.id.list);
    listView.addFooterView(progressBar);
    itemListAdapter =
        new ItemListAdapter<InboxItem>(getApplicationContext(), R.layout.inbox_item, new ArrayList<InboxItem>(), this);
    itemListAdapter.setFilter(new InboxFilter());
    listView.setAdapter(itemListAdapter);
    listView.setOnScrollListener(this);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        InboxItem item = (InboxItem) adapter.getItemAtPosition(position);

        if (item.itemType != null) {
          boolean isSupportedItem =
              item.itemType.equals(InboxItem.ItemType.COMMENT) || item.itemType.equals(InboxItem.ItemType.NEW_ANSWER);

          if (isSupportedItem) {
            Intent intent = new Intent(UserInboxActivity.this, InboxItemActivity.class);
            intent.putExtra(StringConstants.INBOX_ITEM, item);
            startActivity(intent);
          } else {
            Toast.makeText(UserInboxActivity.this, "Sorry, this message type not supported by application",
                Toast.LENGTH_LONG).show();
          }
        }
      }
    });
  }

  private void startIntentService() {
    if (!serviceRunning && AppUtils.inAuthenticatedRealm(getApplicationContext())) {
      intent = new Intent(getApplicationContext(), UserIntentService.class);
      intent.setAction(StringConstants.INBOX_ITEMS);
      intent.putExtra(StringConstants.ACTION, UserIntentService.GET_USER_INBOX);
      intent.putExtra(StringConstants.PAGE, ++page);
      intent.putExtra(StringConstants.RESULT_RECEIVER, receiver);

      progressBar.setVisibility(View.VISIBLE);
      startService(intent);
      serviceRunning = true;
    }
  }

  @Override
  protected void setActionBarTitle(String title) {
    getActionBar().setTitle(getString(R.string.inbox));
  }

  @Override
  protected void setActionBarHomeIcon(Site site) {
    getActionBar().setIcon(R.drawable.icon);
    getActionBar().setHomeButtonEnabled(true);
  }

  @Override
  public void onResume() {
    super.onResume();

    if (itemListAdapter != null)
      itemListAdapter.notifyDataSetChanged();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    boolean ret = super.onCreateOptionsMenu(menu);
    menu.removeItem(R.id.menu_my_inbox);
    return ret & true;
  }

  @Override
  public void refresh() {
    itemListAdapter.clear();
    page = 0;
    startIntentService();
  }

  @Override
  protected boolean shouldSearchViewBeEnabled() {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    serviceRunning = false;
    progressBar.setVisibility(View.GONE);

    currentPageObject = (StackXPage<InboxItem>) resultData.getSerializable(StringConstants.INBOX_ITEMS);

    if (currentPageObject != null) {
      pages.add(currentPageObject);
      updateAutoCompleteOptions();
      itemListAdapter.addAll(currentPageObject.items);
    }
  }

  private void updateAutoCompleteOptions() {
    for (InboxItem inboxItem : currentPageObject.items) {
      boolean notifyAutoCompleteAdapter = false;

      if (!autocompleteOptions.contains(inboxItem.itemType.getRepr())) {
        autocompleteOptions.add(inboxItem.itemType.getRepr());
        notifyAutoCompleteAdapter = true;
      }

      if (!autocompleteOptions.contains(inboxItem.site.apiSiteParameter)) {
        autocompleteOptions.add(inboxItem.site.apiSiteParameter);
        notifyAutoCompleteAdapter = true;
      }

      if (notifyAutoCompleteAdapter) {
        searchOptionsAdapter.clear();
        searchOptionsAdapter.notifyDataSetInvalidated();

        searchOptionsAdapter.addAll(autocompleteOptions);
        searchOptionsAdapter.notifyDataSetChanged();
      }
    }
  }

  @Override
  public View getView(InboxItem item, int position, View convertView, ViewGroup parent) {
    InboxItemViewHolder inboxItemViewHolder;
    if (convertView == null) {
      inboxItemViewHolder = new InboxItemViewHolder();
      convertView = getLayoutInflater().inflate(R.layout.inbox_item, null);
      inboxItemViewHolder.title = (TextView) convertView.findViewById(R.id.itemTitle);
      inboxItemViewHolder.body = (TextView) convertView.findViewById(R.id.itemBodyPreview);
      inboxItemViewHolder.creationTime = (TextView) convertView.findViewById(R.id.itemCreationTime);
      inboxItemViewHolder.itemType = (TextView) convertView.findViewById(R.id.itemType);
      inboxItemViewHolder.itemSite = (TextView) convertView.findViewById(R.id.itemSite);
      inboxItemViewHolder.siteIcon = (ImageView) convertView.findViewById(R.id.siteIcon);
      convertView.setTag(inboxItemViewHolder);
    } else {
      inboxItemViewHolder = (InboxItemViewHolder) convertView.getTag();
    }

    loadSiteIcon(convertView, inboxItemViewHolder.siteIcon, item.site);
    inboxItemViewHolder.title.setText(Html.fromHtml(item.title));
    inboxItemViewHolder.creationTime.setText(DateTimeUtils.toDateString(item.creationDate));
    inboxItemViewHolder.itemType.setText(item.itemType.getRepr());

    if (item.body != null)
      inboxItemViewHolder.body.setText(Html.fromHtml(item.body));

    if (item.site != null)
      inboxItemViewHolder.itemSite.setText(item.site.name);
    else
      inboxItemViewHolder.itemSite.setVisibility(View.GONE);

    if (item.unread) {
      inboxItemViewHolder.title.setTypeface(null, Typeface.BOLD);
      inboxItemViewHolder.body.setTypeface(null, Typeface.BOLD);
    } else {
      inboxItemViewHolder.title.setTypeface(null, Typeface.NORMAL);
      inboxItemViewHolder.body.setTypeface(null, Typeface.NORMAL);
    }

    return convertView;
  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    if (!serviceRunning && totalItemCount >= StackUri.QueryParamDefaultValues.PAGE_SIZE
        && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1)) {
      if (currentPageObject != null && currentPageObject.hasMore) {
        progressBar.setVisibility(View.VISIBLE);
        startIntentService();
      }
    }
  }

  private void loadSiteIcon(final View parent, final ImageView imageView, final Site site) {
    if (!BitmapCache.getInstance().containsKey(site.iconUrl)) {
      final ProgressBar siteIconLoadProgress = (ProgressBar) parent.findViewById(R.id.siteIconLoadProgress);
      siteIconLoadProgress.setVisibility(View.VISIBLE);
      AsyncTaskCompletionNotifier<Bitmap> imageFetchAsyncTaskCompleteNotiferImpl =
          new AsyncTaskCompletionNotifier<Bitmap>() {
            @Override
            public void notifyOnCompletion(Bitmap result) {
              siteIconLoadProgress.setVisibility(View.GONE);
              displayImage(imageView, result);
            }
          };

      AsyncTaskExecutor.getInstance().executeInThreadPoolExecutor(
          new GetImageAsyncTask(imageFetchAsyncTaskCompleteNotiferImpl), site.iconUrl);
    } else {
      displayImage(imageView, BitmapCache.getInstance().get(site.iconUrl));
    }
  }

  private void displayImage(final ImageView imageView, final Bitmap bitmap) {
    imageView.setVisibility(View.VISIBLE);
    imageView.setImageBitmap(bitmap);
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
  }
}

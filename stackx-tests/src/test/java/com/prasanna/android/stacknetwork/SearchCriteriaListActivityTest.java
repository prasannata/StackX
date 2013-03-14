package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;

import android.app.ActionBar;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.prasanna.android.runners.ConfigurableRobolectricTestRunner;
import com.prasanna.android.stacknetwork.SearchCriteriaListActivity.SearchCriteriaViewHolder;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;
import com.prasanna.android.stacknetwork.model.SearchCriteriaDomain;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

@RunWith(ConfigurableRobolectricTestRunner.class)
public class SearchCriteriaListActivityTest extends AbstractBaseListActivityTest<SearchCriteriaDomain>
{
    private class SearchCriteriaListActivityStub extends SearchCriteriaListActivity
    {
        private final ActionBar actionBar = Mockito.mock(ActionBar.class);

        @Override
        public ActionBar getActionBar()
        {
            return actionBar;
        }
    }

    private SearchCriteriaListActivityStub searchCriteriaListActivity;

    @Before
    public void setup()
    {
        searchCriteriaListActivity = new SearchCriteriaListActivityStub();
        setContext(searchCriteriaListActivity);
        OperatingSite.setSite(getSite("Stack Overflow", "stackoverflow", true, true));
    }

    @Test
    public void loadCriteria()
    {
        searchCriteriaListActivity.onCreate(null);
        ListView listView = (ListView) searchCriteriaListActivity.findViewById(android.R.id.list);

        assertSortOptionSpinner();
        ArrayList<View> listViews = assertListView(listView, getExpectedSearchCriteriaList());
        assetListItemClick(listViews.get(0));
    }

    private void assertSortOptionSpinner()
    {
        String[] sortOptionArray =
                        searchCriteriaListActivity.getResources().getStringArray(R.array.searchCriteriaSortArray);
        Spinner sortSpinner = (Spinner) searchCriteriaListActivity.findViewById(R.id.searchSortSpinner);
        assertNotNull(sortSpinner);
        assertTrue(0 == sortSpinner.getSelectedItemPosition());
        assertEquals(sortOptionArray[0], (String) sortSpinner.getSelectedItem());
    }

    @Override
    protected View assertListItem(ListAdapter listAdpater, int position, SearchCriteriaDomain item)
    {
        View view = listAdpater.getView(position, null, null);
        assertNotNull(view);

        SearchCriteriaViewHolder holder = (SearchCriteriaViewHolder) view.getTag();
        assertNotNull(holder);
        assertTextViewForValue(holder.itemText, item.name);
        assertTextViewForValue(holder.itemDetails, getExpectedItemDetails(item));
        assertTextViewForValue(holder.lastRun, getExpectedLastRunTime(item));
        assertTextViewForValue(holder.ran, getExpectedRunCount(item));
        assertEquals(item.tab, holder.addTabToggle.isChecked());
        assertFalse(holder.delCheckBox.isChecked());

        return view;
    }

    private String getExpectedItemDetails(SearchCriteriaDomain searchCriteriaDomain)
    {
        String expectedItemDetails = "query: " + searchCriteriaDomain.searchCriteria.getQuery();
        expectedItemDetails += ", sort: " + searchCriteriaDomain.searchCriteria.getSort();
        expectedItemDetails += ", answers: " + (searchCriteriaDomain.searchCriteria.getAnswerCount() > 0);
        expectedItemDetails += ", answered: " + searchCriteriaDomain.searchCriteria.isAnswered();
        String includedTags = searchCriteriaDomain.searchCriteria.getIncludedTagsAsSemicolonDelimitedString();
        if (includedTags != null)
            expectedItemDetails += ", tagged: " + includedTags;
        String excludedTags = searchCriteriaDomain.searchCriteria.getExcludedTagsAsSemicolonDelimitedString();
        if (excludedTags != null)
            expectedItemDetails += ", not tagged: " + excludedTags;

        return expectedItemDetails;
    }

    private String getExpectedLastRunTime(SearchCriteriaDomain searchCriteriaDomain)
    {
        String expectedLastRunTime = searchCriteriaListActivity.getString(R.string.lastRan) + " ";

        if (searchCriteriaDomain.lastRun > 0)
            expectedLastRunTime += DateTimeUtils.getElapsedDurationSince(searchCriteriaDomain.lastRun);
        else
            expectedLastRunTime += searchCriteriaListActivity.getString(R.string.never);

        return expectedLastRunTime;
    }

    public String getExpectedRunCount(SearchCriteriaDomain searchCriteriaDomain)
    {
        String expectedRunCount = searchCriteriaListActivity.getString(R.string.ran) + " ";
        if (searchCriteriaDomain.runCount == 0)
            expectedRunCount += searchCriteriaListActivity.getString(R.string.never);
        else if (searchCriteriaDomain.runCount == 1)
            expectedRunCount +=
                            searchCriteriaDomain.runCount + " " + searchCriteriaListActivity.getString(R.string.time);
        else
            expectedRunCount =
                            AppUtils.formatNumber(searchCriteriaDomain.runCount) + "  "
                                            + searchCriteriaListActivity.getString(R.string.times);
        return expectedRunCount;
    }

    private SearchCriteriaDomain getSearchCriteriaDomain(int id, boolean tab)
    {
        SearchCriteriaDomain searchCriteriaDomain = new SearchCriteriaDomain();
        searchCriteriaDomain.id = id;
        searchCriteriaDomain.name = "Search " + id;
        searchCriteriaDomain.runCount = id;
        searchCriteriaDomain.site = "stackoveflow";
        searchCriteriaDomain.lastRun = System.currentTimeMillis();
        searchCriteriaDomain.tab = tab;
        searchCriteriaDomain.searchCriteria =
                        SearchCriteria.newCriteria("query").mustBeAnswered().sortBy(SearchSort.RELEVANCE)
                                        .includeTag("java").excludeTag("c#");
        return searchCriteriaDomain;
    }

    private void assetListItemClick(View view)
    {
        SearchCriteriaViewHolder holder = (SearchCriteriaViewHolder) view.getTag();
        ShadowActivity shadowSearchCriteriaListActivity = Robolectric.shadowOf(searchCriteriaListActivity);
        holder.itemLayout.performClick();
        assertNextStartedActivity(shadowSearchCriteriaListActivity, AdvancedSearchActivity.class,
                        StringConstants.SEARCH_CRITERIA);
    }

    private ArrayList<SearchCriteriaDomain> getExpectedSearchCriteriaList()
    {
        ArrayList<SearchCriteriaDomain> searchCriteriaDomains = new ArrayList<SearchCriteriaDomain>();
        searchCriteriaDomains.add(getSearchCriteriaDomain(1, true));
        return searchCriteriaDomains;
    }
}

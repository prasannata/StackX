package com.prasanna.android.stacknetwork.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.prasanna.android.stacknetwork.model.SearchCriteria.SearchSort;

public class SearchCriteriaTest
{
    private static final String QUERY = "query";

    @Test
    public void build()
    {
        SearchCriteria searchCriteria =
                        SearchCriteria.newCriteria(QUERY).includeTags(Arrays.asList("tag1", "tag2"))
                                        .excludeTags(Arrays.asList("tag3", "tag4")).mustBeAnswered().setMinAnswers(5)
                                        .sortBy(SearchSort.VOTES).build();

        assertNotNull(searchCriteria);
        assertCriteriaMap(searchCriteria, "1");
        assertCriteriaMap(searchCriteria.nextPage(), "2");

        clearBuildAndAssert(searchCriteria);
    }

    @Test
    public void buildHandlesError()
    {
        SearchCriteria searchCriteria =
                        SearchCriteria.newCriteria(null).includeTags(null)
                                        .excludeTags(null).setMinAnswers(-1)
                                        .sortBy(null).build();

        assertNotNull(searchCriteria);
        assertNull(searchCriteria.getMap().get(SearchCriteria.Q));
        assertNull(searchCriteria.getMap().get(SearchCriteria.TAGGED));
        assertNull(searchCriteria.getMap().get(SearchCriteria.NOT_TAGGED));
        assertNull(searchCriteria.getMap().get(SearchCriteria.ANSWERS));
        assertNull(searchCriteria.getMap().get(SearchCriteria.ACCEPTED));
        assertNull(searchCriteria.getMap().get(SearchCriteria.SORT));
    }

    private void clearBuildAndAssert(SearchCriteria searchCriteria)
    {
        searchCriteria.clear();
        searchCriteria =
                        SearchCriteria.newCriteria().includeTags(Arrays.asList("tag5", "tag6"))
                                        .sortBy(SearchSort.CREATION).build();
        assertNotNull(searchCriteria);
        assertNull(searchCriteria.getMap().get(SearchCriteria.Q));
        assertNull(searchCriteria.getMap().get(SearchCriteria.NOT_TAGGED));
        assertNull(searchCriteria.getMap().get(SearchCriteria.ANSWERS));
        assertNull(searchCriteria.getMap().get(SearchCriteria.ACCEPTED));
        
        assertEquals("tag5;tag6",searchCriteria.getMap().get(SearchCriteria.TAGGED));
        assertEquals(SearchSort.CREATION.getValue(), searchCriteria.getMap().get(SearchCriteria.SORT));
        assertEquals("1", searchCriteria.getMap().get(SearchCriteria.PAGE));
        assertEquals(String.valueOf(SearchCriteria.DEFAULT_PAGE_SIZE), searchCriteria.getMap().get(SearchCriteria.PAGESIZE));
    }

    private void assertCriteriaMap(SearchCriteria builtCriteria, String page)
    {
        Map<String, String> criteriaMap = builtCriteria.getMap();
        assertNotNull(criteriaMap);
        assertEquals(QUERY, criteriaMap.get(SearchCriteria.Q));
        assertEquals(String.valueOf(true), criteriaMap.get(SearchCriteria.ACCEPTED));
        assertEquals("5", criteriaMap.get(SearchCriteria.ANSWERS));
        assertEquals("tag2;tag1", criteriaMap.get(SearchCriteria.TAGGED));
        assertEquals("tag4;tag3", criteriaMap.get(SearchCriteria.NOT_TAGGED));
        assertEquals(SearchSort.VOTES.getValue(), criteriaMap.get(SearchCriteria.SORT));
        assertEquals(page, criteriaMap.get(SearchCriteria.PAGE));
        assertEquals(String.valueOf(SearchCriteria.DEFAULT_PAGE_SIZE), criteriaMap.get(SearchCriteria.PAGESIZE));
    }
}

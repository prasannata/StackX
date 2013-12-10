package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.matchers.StartedMatcher;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowListActivity;
import org.robolectric.shadows.ShadowToast;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.runners.ConfigurableRobolectricTestRunner;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

@RunWith(ConfigurableRobolectricTestRunner.class)
public class StackNetworkListActivityTest extends AbstractBaseListActivityTest<Site> {
  private StackNetworkListActivity stackNetworkListActivity;

  @Before
  public void setup() {
    stackNetworkListActivity = new StackNetworkListActivity();
    super.setContext(stackNetworkListActivity);
  }

  @Test
  public void siteListDisplayedForNonAuthUser() {
    ArrayList<Site> siteList = getSiteArrayListForNonAuthUser();

    stackNetworkListActivity.onCreate(null);
    ListView listView = (ListView) stackNetworkListActivity.findViewById(android.R.id.list);

    ShadowListActivity shadowListActivity = Robolectric.shadowOf(stackNetworkListActivity);
    assertGetUserSitesIntentServiceStarted(shadowListActivity, false);
    ArrayList<View> siteListViews = assertListView(listView, siteList);
    assertListItemClick(siteList, siteListViews, 0);
    assertOnDefaultSiteClick(siteList, siteListViews, 0);
  }

  @Test
  public void siteListDisplayedForAuthUserWithWrite() {
    ArrayList<Site> siteList = getSiteArrayListForAuthUserWithWrite();
    AppUtils.setAccessToken(stackNetworkListActivity, "validAccessToken");
    stackNetworkListActivity.onCreate(null);
    ListView listView = (ListView) stackNetworkListActivity.findViewById(android.R.id.list);

    ShadowListActivity shadowListActivity = Robolectric.shadowOf(stackNetworkListActivity);
    assertGetUserSitesIntentServiceStarted(shadowListActivity, true);
    ArrayList<View> siteListViews = assertListView(listView, siteList);
    assertListItemClick(siteList, siteListViews, 1);
  }

  private void assertListItemClick(ArrayList<Site> siteList, ArrayList<View> siteListViews, int position) {
    assertTrue(siteListViews.get(position).performClick());
    assertThat(stackNetworkListActivity, new StartedMatcher(QuestionsActivity.class));
    assertSame(siteList.get(position), OperatingSite.getSite());
  }

  private void assertOnDefaultSiteClick(ArrayList<Site> siteList, ArrayList<View> siteListViews, int position) {
    assertTrue(siteListViews.get(position).findViewById(R.id.isDefaultSite).performClick());
    Site defaultSite = AppUtils.getDefaultSite(stackNetworkListActivity);
    assertNotNull(defaultSite);
    assertEquals(siteList.get(position).apiSiteParameter, defaultSite.apiSiteParameter);

    ShadowHandler.idleMainLooper();
    assertEquals(ShadowToast.getTextOfLatestToast(), siteList.get(0).name + " set as default site.");
  }

  @Override
  protected View assertListItem(ListAdapter listAdpater, int position, Site site) {
    View view = listAdpater.getView(position, null, null);
    assertNotNull(view);
    TextView siteNameView = (TextView) view.findViewById(R.id.siteName);
    assertNotNull(siteNameView);
    assertEquals(site.name, siteNameView.getText().toString());

    assertRegisteredUserHint(site, view);
    assertWritePermissionHint(site, view);

    ImageView isDefaultSiteImageView = (ImageView) view.findViewById(R.id.isDefaultSite);
    assertNotNull(isDefaultSiteImageView);
    return view;
  }

  private void assertRegisteredUserHint(Site site, View view) {
    if (site.userType == null || !UserType.REGISTERED.equals(site.userType))
      assertTrue(view.findViewById(R.id.siteUserTypeRegistered).getVisibility() == View.GONE);
    else if (UserType.REGISTERED.equals(site.userType))
      assertTrue(view.findViewById(R.id.siteUserTypeRegistered).getVisibility() == View.VISIBLE);
  }

  private void assertWritePermissionHint(Site site, View view) {
    if (site.writePermissions == null || site.writePermissions.isEmpty())
      assertTrue(view.findViewById(R.id.writePermissionEnabled).getVisibility() == View.GONE);
    else {
      for (WritePermission writePermission : site.writePermissions) {
        if (writePermission.canAdd && writePermission.canEdit && writePermission.canDelete)
          assertTrue(view.findViewById(R.id.writePermissionEnabled).getVisibility() == View.VISIBLE);
      }
    }
  }

  private void assertGetUserSitesIntentServiceStarted(ShadowListActivity shadowListActivity, boolean me) {
    Intent nextStartedServiceIntent = assertNextStartedIntentService(shadowListActivity, UserIntentService.class);
    assertEquals(UserIntentService.GET_USER_SITES, nextStartedServiceIntent.getIntExtra(StringConstants.ACTION, -1));
    assertEquals(me, nextStartedServiceIntent.getBooleanExtra(StringConstants.ME, true));
    assertNotNull(nextStartedServiceIntent.getParcelableExtra(StringConstants.RESULT_RECEIVER));
  }

  private ArrayList<Site> getSiteArrayListForNonAuthUser() {
    ArrayList<Site> sites = new ArrayList<Site>();
    sites.add(getSite("Stack Overflow", "stackoverflow", false, false));
    sites.add(getSite("Super User", "superuser", false, false));
    return sites;
  }

  private ArrayList<Site> getSiteArrayListForAuthUserWithWrite() {
    ArrayList<Site> sites = new ArrayList<Site>();
    sites.add(getSite("Stack Overflow", "stackoverflow", true, true));
    sites.add(getSite("Super User", "superuser", true, false));
    return sites;
  }

}

package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.matchers.StartedMatcher;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import android.content.DialogInterface;
import android.widget.Button;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest extends AbstractBaseActivityTest {
  private LoginActivity loginActivity;

  @Before
  public void setup() {
    loginActivity = new LoginActivity();
    loginActivity.onCreate(null);
  }

  @Test
  public void login() {
    loginActivity = new LoginActivity();
    assertTrue(AppUtils.isFirstRun(loginActivity));

    loginActivity.onCreate(null);
    Button loginButton = (Button) loginActivity.findViewById(R.id.login_button);
    assertNotNull(loginButton);

    loginButton.performClick();
    assertThat(loginActivity, new StartedMatcher(OAuthActivity.class));
    assertFalse(AppUtils.isFirstRun(loginActivity));
  }

  @Test
  public void loginScreenNotShowAfterFirstRun() {
    loginActivity = new LoginActivity();
    AppUtils.setFirstRunComplete(loginActivity);
    assertFalse(AppUtils.isFirstRun(loginActivity));
    loginActivity.onCreate(null);
    assertThat(loginActivity, new StartedMatcher(StackNetworkListActivity.class));
  }

  @Test
  public void loginScreenNotShowAfterFirstRunWithDefaulSiteSet() {
    loginActivity = new LoginActivity();
    AppUtils.setFirstRunComplete(loginActivity);
    AppUtils.setDefaultSite(loginActivity, getSite("Stack Overflow", "stackOverflow", false, false));
    assertFalse(AppUtils.isFirstRun(loginActivity));
    loginActivity.onCreate(null);
    assertThat(loginActivity, new StartedMatcher(QuestionsActivity.class));
  }

  @Test
  public void skipLogin() {
    TextView skipLogin = (TextView) loginActivity.findViewById(R.id.skipLogin);
    assertNotNull(skipLogin);
    skipLogin.performClick();
    ShadowAlertDialog dialog = Robolectric.shadowOf(ShadowAlertDialog.getLatestAlertDialog());
    assertEquals(loginActivity.getString(R.string.noLoginWarn), dialog.getMessage());
    assertTrue(dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick());
    assertThat(loginActivity, new StartedMatcher(StackNetworkListActivity.class));
  }

  @Test
  public void skipLoginAndCancel() {
    ShadowActivity shadowActivity = Robolectric.shadowOf(loginActivity);

    TextView skipLogin = (TextView) loginActivity.findViewById(R.id.skipLogin);
    assertNotNull(skipLogin);
    skipLogin.performClick();
    ShadowAlertDialog dialog = Robolectric.shadowOf(ShadowAlertDialog.getLatestAlertDialog());
    assertEquals(loginActivity.getString(R.string.noLoginWarn), dialog.getMessage());
    assertTrue(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick());
    assertNull(shadowActivity.getNextStartedActivity());
  }

  @After
  public void cleanup() {
    SharedPreferencesUtil.clear(loginActivity);
  }
}

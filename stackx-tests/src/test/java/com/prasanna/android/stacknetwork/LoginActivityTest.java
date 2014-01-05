package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import android.app.AlertDialog;
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
    loginActivity = createActivity(LoginActivity.class);
  }

  @Test
  public void login() {
    assertTrue(AppUtils.isFirstRun(loginActivity));
    Button loginButton = (Button) loginActivity.findViewById(R.id.login_button);
    assertNotNull(loginButton);

    loginButton.performClick();
    assertNextActivity(loginActivity, OAuthActivity.class);
    assertFalse(AppUtils.isFirstRun(loginActivity));
  }

  @Test
  public void loginScreenNotShowAfterFirstRun() {
    AppUtils.setFirstRunComplete(loginActivity);
    assertFalse(AppUtils.isFirstRun(loginActivity));
    assertNextActivity(createActivity(LoginActivity.class), StackNetworkListActivity.class);
  }

  @Test
  public void loginScreenNotShowAfterFirstRunWithDefaulSiteSet() {
    AppUtils.setFirstRunComplete(loginActivity);
    AppUtils.setDefaultSite(loginActivity, getSite("Stack Overflow", "stackOverflow", false, false));
    assertFalse(AppUtils.isFirstRun(loginActivity));
    assertNextActivity(createActivity(LoginActivity.class), QuestionsActivity.class);
  }

  @Test
  public void skipLogin() {
    ((TextView) loginActivity.findViewById(R.id.skipLogin)).performClick();
    AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
    ShadowAlertDialog dialog = Robolectric.shadowOf(latestAlertDialog);
    assertEquals(loginActivity.getString(R.string.noLoginWarn), dialog.getMessage());
    assertTrue(latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick());
    assertNextActivity(loginActivity, StackNetworkListActivity.class);
  }

  @Test
  public void skipLoginAndCancel() {
    ShadowActivity shadowActivity = Robolectric.shadowOf(loginActivity);

    ((TextView) loginActivity.findViewById(R.id.skipLogin)).performClick();
    AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
    ShadowAlertDialog dialog = Robolectric.shadowOf(latestAlertDialog);
    assertEquals(loginActivity.getString(R.string.noLoginWarn), dialog.getMessage());
    assertTrue(latestAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick());
    assertNull(shadowActivity.getNextStartedActivity());
  }

  @After
  public void cleanup() {
    SharedPreferencesUtil.clear(loginActivity);
  }
}

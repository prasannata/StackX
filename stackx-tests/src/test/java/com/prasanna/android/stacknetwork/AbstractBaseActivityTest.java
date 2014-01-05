package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;

public abstract class AbstractBaseActivityTest {
  private Context context;

  public void setContext(Context context) {
    this.context = context;
  }

  protected <T extends Activity> T createActivity(Class<T> clazz) {
    return Robolectric.buildActivity(clazz).create().get();
  }

  protected <T extends Activity> T createActivityAndResume(Class<T> clazz) {
    return Robolectric.buildActivity(clazz).create().resume().get();
  }
  
  protected Intent assertNextStartedIntentService(ShadowActivity shadowActivity, Class<?> clazz) {
    Intent intent = shadowActivity.getNextStartedService();
    assertNotNull(intent);
    assertEquals(clazz.getName(), intent.getComponent().getClassName());
    return intent;
  }

  protected Intent assertNextStartedActivity(ShadowActivity shadowActivity, Class<?> clazz, String action) {
    Intent intent = shadowActivity.getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(clazz.getName(), intent.getComponent().getClassName());
    if (action != null)
      assertEquals(action, intent.getAction());
    return intent;
  }

  protected Site getSite(String name, String apiSiteParamter, boolean registered, boolean hasWritePermission) {
    Site site = new Site();
    site.name = name;
    site.apiSiteParameter = apiSiteParamter;

    if (registered)
      site.userType = UserType.REGISTERED;

    if (hasWritePermission) {
      site.writePermissions = new ArrayList<WritePermission>();
      site.writePermissions.add(getWritePermission(ObjectType.COMMENT));
    }

    return site;
  }

  protected WritePermission getWritePermission(ObjectType objectType) {
    WritePermission writePermission = new WritePermission();
    writePermission.canAdd = true;
    writePermission.canEdit = true;
    writePermission.canDelete = true;
    writePermission.objectType = objectType;
    return writePermission;
  }

  protected void assertTextViewForValue(TextView textView, String expectedValue) {
    assertNotNull(textView);
    assertEquals(expectedValue, textView.getText().toString());
  }

  protected void assertToggleButtonState(ToggleButton toggleButton, boolean expectedState) {
    assertNotNull(toggleButton);
    assertTrue(toggleButton.isChecked() == expectedState);
  }

  protected void assertNextActivity(Activity currentActivity, Class<? extends Activity> nextActivity) {
    Intent intent = Robolectric.shadowOf(currentActivity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(nextActivity.getCanonicalName(), intent.getComponent().getClassName());
  }

  @After
  public void cleanup() {
    if (context != null)
      SharedPreferencesUtil.clear(context);
  }
}

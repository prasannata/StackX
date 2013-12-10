package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.robolectric.shadows.ShadowActivity;

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

  @After
  public void cleanup() {
    if (context != null)
      SharedPreferencesUtil.clear(context);
  }
}

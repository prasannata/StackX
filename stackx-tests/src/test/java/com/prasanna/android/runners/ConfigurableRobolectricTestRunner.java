package com.prasanna.android.runners;

import java.lang.reflect.Method;

import org.junit.runners.model.InitializationError;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.os.Build;

public class ConfigurableRobolectricTestRunner extends RobolectricTestRunner {
  private static final int SDK_INT = Build.VERSION.SDK_INT;

  public ConfigurableRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  public void beforeTest(final Method method) {
    Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 17);
  }

  @Override
  public void afterTest(final Method method) {
    resetStaticState();
  }

  @Override
  public void resetStaticState() {
    Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", SDK_INT);
  }

}

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

package com.prasanna.android.stacknetwork.fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.LogoutActivity;
import com.prasanna.android.stacknetwork.OAuthActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.sqlite.SiteDAO;
import com.prasanna.android.stacknetwork.utils.AlarmUtils;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DialogBuilder;
import com.prasanna.android.utils.LogWrapper;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
  private static final String TAG = SettingsFragment.class.getSimpleName();

  public static final String KEY_PREF_INBOX = "pref_inbox";
  public static final String KEY_PREF_INBOX_REFRESH_INTERVAL = "pref_inboxRefreshInterval";
  public static final String KEY_PREF_INBOX_NOTIFICATION = "pref_newNotification";
  public static final String KEY_PREF_NOTIF_VIBRATE = "pref_vibrate";
  public static final String KEY_PREF_NOTIF_RINGTONE = "pref_notificationTone";
  public static final String KEY_PREF_CLEAR_CACHE = "pref_clearCache";
  public static final String KEY_PREF_DEFAULT_SITE = "pref_defaultSite";
  public static final String KEY_PREF_SEARCH_IN_TITLE = "pref_searchInTitle";
  public static final String KEY_PREF_SEARCH_ONLY_WITH_ANSWERS = "pref_searchOnlyWithAnswers";
  public static final String KEY_PREF_SEARCH_ONLY_ANSWERED = "pref_searchOnlyAnswered";
  public static final String KEY_PREF_NUM_SAVED_SEARCHES = "pref_numSavedSearches";
  public static final String KEY_PREF_RO_APP_VERSION = "pref_ro_appVersion";
  public static final String KEY_PREF_WRITE_RESTRICTIONS = "pref_write_restrictions";
  public static final String PREFIX_KEY_PREF_WRITE_PERMISSION = "pref_writePermission_";

  private static final String KEY_PREF_ACCOUNT_ACTION = "pref_accountAction";
  private static final String KEY_PREF_RATE_APP = "pref_rateApp";
  private static final String DEFAULT_RINGTONE = "content://settings/system/Silent";

  private String versionName;
  private int versionCode = -1;
  private ListPreference refreshIntervalPref;
  private ListPreference accountActionPref;
  private RingtonePreference notifRingTonePref;
  private PreferenceCategory inboxPrefCategory;

  public static int getInboxRefreshInterval(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return Integer.parseInt(sharedPreferences.getString(KEY_PREF_INBOX_REFRESH_INTERVAL, "-1"));
  }

  public static boolean isNotificationEnabled(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.getBoolean(KEY_PREF_INBOX_NOTIFICATION, false);
  }

  public static boolean isVibrateEnabled(Context context) {
    if (!isNotificationEnabled(context))
      return false;
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.getBoolean(KEY_PREF_NOTIF_VIBRATE, false);
  }

  public static Uri getRingtone(Context context) {
    if (!isNotificationEnabled(context))
      return null;

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return Uri.parse(sharedPreferences.getString(KEY_PREF_NOTIF_RINGTONE, ""));
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    setupDefaultSitePreference();
    setupRateAppPreference();
    setupAccountPreference();
    setupInboxPreference();
    setupAboutPreference();
  }

  private void setupDefaultSitePreference() {
    final ListPreference defaultSitePref = (ListPreference) findPreference(KEY_PREF_DEFAULT_SITE);
    Site defaultSite = AppUtils.getDefaultSite(getActivity().getApplicationContext());
    defaultSitePref.setEntryValues(new String[0]);
    defaultSitePref.setEntries(new String[0]);
    defaultSitePref.setSummary(defaultSite != null ? defaultSite.name : "None");
  }

  private void setupRateAppPreference() {
    final ListPreference rateApp = (ListPreference) findPreference(KEY_PREF_RATE_APP);
    rateApp.setEntryValues(new String[0]);
    rateApp.setEntries(new String[0]);

    rateApp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        rateApp.getDialog().dismiss();
        String packageName = "com.prasanna.android.stacknetwork";

        if (AppUtils.AMAZON_APK)
          openAmazonAppStore(packageName);
        else
          openPlayStore(packageName);

        return true;
      }

      private void openAmazonAppStore(String packageName) {
        try {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.amazon.com/gp/mas/dl/android?p="
              + packageName)));
        } catch (ActivityNotFoundException anfe) {
          Toast.makeText(getActivity(), "Amazon AppStore not found", Toast.LENGTH_SHORT).show();
        }
      }

      private void openPlayStore(String packageName) {
        try {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException anfe) {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="
              + packageName)));
        }
      }
    });
  }

  private void setupAccountPreference() {
    accountActionPref = (ListPreference) findPreference(KEY_PREF_ACCOUNT_ACTION);
    accountActionPref.setEntryValues(new String[0]);
    accountActionPref.setEntries(new String[0]);

    if (AppUtils.inAuthenticatedRealm(getActivity()))
      setupLogoutPreference();
    else
      setupLoginPreference();
  }

  private void setupLoginPreference() {
    accountActionPref.setTitle(getString(R.string.login));
    accountActionPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        accountActionPref.getDialog().dismiss();

        Intent oAuthIntent = new Intent(getActivity(), OAuthActivity.class);
        AppUtils.clearDefaultSite(getActivity());
        SiteDAO.deleteAll(SettingsFragment.this.getActivity());
        startActivity(oAuthIntent);
        return true;
      }
    });
  }

  private void setupLogoutPreference() {
    accountActionPref.setTitle(getString(R.string.logout));
    accountActionPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
              Intent logoutIntent = new Intent(getActivity(), LogoutActivity.class);
              AppUtils.clearDefaultSite(getActivity());
              logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(logoutIntent);
              break;

            case DialogInterface.BUTTON_NEGATIVE:
              dialog.dismiss();
              break;
          }
        }
      };

      @Override
      public boolean onPreferenceClick(Preference preference) {
        accountActionPref.getDialog().dismiss();
        DialogBuilder.yesNoDialog(getActivity(), R.string.logoutMsg, dialogClickListener).show();
        return true;
      }
    });
  }

  private void setupInboxPreference() {
    inboxPrefCategory = (PreferenceCategory) findPreference(KEY_PREF_INBOX);
    inboxPrefCategory.setEnabled(AppUtils.inAuthenticatedRealm(getActivity()));
    refreshIntervalPref = (ListPreference) findPreference(KEY_PREF_INBOX_REFRESH_INTERVAL);
    refreshIntervalPref.setSummary(refreshIntervalPref.getEntry());

    setupRingtonePreference();
  }

  private void setupRingtonePreference() {
    notifRingTonePref = (RingtonePreference) findPreference(KEY_PREF_NOTIF_RINGTONE);
    notifRingTonePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        setRingtoneSummary(Uri.parse((String) newValue));
        return true;
      }
    });

    Uri ringtoneUri =
        Uri.parse(notifRingTonePref.getSharedPreferences().getString(KEY_PREF_NOTIF_RINGTONE, DEFAULT_RINGTONE));
    if (ringtoneUri != null)
      setRingtoneSummary(ringtoneUri);
  }

  private void setupAboutPreference() {
    setupVersionSummary();
    setupWriteRestrictionSummary();
  }

  private void setupVersionSummary() {
    final ListPreference appVersionROPref = (ListPreference) findPreference(KEY_PREF_RO_APP_VERSION);
    appVersionROPref.setEntryValues(new String[0]);
    appVersionROPref.setEntries(new String[0]);
    appVersionROPref.setSummary(getAppVersion());
  }

  private void setupWriteRestrictionSummary() {
    final ListPreference writeRestrictionsPref = (ListPreference) findPreference(KEY_PREF_WRITE_RESTRICTIONS);
    writeRestrictionsPref.setEntryValues(new String[0]);
    writeRestrictionsPref.setEntries(new String[0]);
    writeRestrictionsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (writeRestrictionsPref.getDialog() != null) {
          writeRestrictionsPref.getDialog().dismiss();
          showDialog(getWebView());
          return true;
        }

        return false;
      }

      private WebView getWebView() {
        WebView webView = new WebView(getActivity());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && url.startsWith("http://")) {
              view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
              return true;
            }

            return false;
          }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/write_permissions.html");
        return webView;
      }

      private void showDialog(WebView webView) {
        AlertDialog alertDialog = DialogBuilder.okDialog(getActivity(), webView, new OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
        alertDialog.show();
      }
    });
  }

  private String getAppVersion() {
    if (versionName != null && versionCode > -1)
      return versionName + "." + versionCode;

    return "Unknown";
  }

  @Override
  public void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    LogWrapper.d(TAG, "Preference changed for " + key);

    if (key.equals(KEY_PREF_INBOX_REFRESH_INTERVAL)) {
      refreshIntervalPref.setSummary(refreshIntervalPref.getEntry());
      AlarmUtils.rescheduleInboxRefreshAlarm(getActivity().getApplicationContext());
    }
  }

  private void setRingtoneSummary(Uri uri) {
    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
    if (ringtone != null)
      notifRingTonePref.setSummary(ringtone.getTitle(getActivity()));
    else
      notifRingTonePref.setSummary("");
  }

  public void setAppVersionName(String versionName) {
    this.versionName = versionName;
  }

  public void setAppVersionCode(int versionCode) {
    this.versionCode = versionCode;
  }
}

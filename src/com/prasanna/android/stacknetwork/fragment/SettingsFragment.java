package com.prasanna.android.stacknetwork.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.utils.AlarmUtils;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
    public static final String KEY_PREF_INBOX_REFRESH_INTERVAL = "pref_inboxRefreshInterval";
    public static final String KEY_PREF_INBOX_NOTIFICATION = "pref_newNotification";
    public static final String KEY_PREF_NOTIF_VIBRATE = "pref_vibrate";
    public static final String KEY_PREF_NOTIF_RINGTONE = "pref_notificationTone";

    private static final String DEFAULT_RINGTONE = "content://settings/system/Silent";

    private ListPreference refreshIntervalPref;
    private RingtonePreference notifRingTonePref;

    public static int getInboxRefreshInterval(Context context)
    {
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return Integer.parseInt(sharedPreferences.getString(KEY_PREF_INBOX_REFRESH_INTERVAL, "-1"));
    }

    public static boolean isNotificationEnabled(Context context)
    {
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return sharedPreferences.getBoolean(KEY_PREF_INBOX_NOTIFICATION, false);
    }

    public static boolean isVibrateEnabled(Context context)
    {
	if (!isNotificationEnabled(context))
	    return false;

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return sharedPreferences.getBoolean(KEY_PREF_NOTIF_VIBRATE, false);
    }

    public static Uri getRingtone(Context context)
    {
	if (!isNotificationEnabled(context))
	    return null;

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return Uri.parse(sharedPreferences.getString(KEY_PREF_NOTIF_RINGTONE, ""));
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.preferences);

	refreshIntervalPref = (ListPreference) findPreference(KEY_PREF_INBOX_REFRESH_INTERVAL);
	setupRingtonePreference();
    }

    private void setupRingtonePreference()
    {
	notifRingTonePref = (RingtonePreference) findPreference(KEY_PREF_NOTIF_RINGTONE);
	notifRingTonePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	{
	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue)
	    {
		setRingtoneSummary(Uri.parse((String) newValue));
		return true;
	    }
	});
	refreshIntervalPref.setSummary(refreshIntervalPref.getEntry());

	Uri ringtoneUri = Uri.parse(notifRingTonePref.getSharedPreferences().getString(KEY_PREF_NOTIF_RINGTONE,
	                DEFAULT_RINGTONE));
	setRingtoneSummary(ringtoneUri);
    }

    @Override
    public void onResume()
    {
	super.onResume();
	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
	super.onPause();
	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
	if (key.equals(KEY_PREF_INBOX_REFRESH_INTERVAL))
	{
	    refreshIntervalPref.setSummary(refreshIntervalPref.getEntry());
	    AlarmUtils.rescheduleInboxRefreshAlarm(getActivity());
	}
    }

    private void setRingtoneSummary(Uri uri)
    {
	Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
	notifRingTonePref.setSummary(ringtone.getTitle(getActivity()));
    }
}

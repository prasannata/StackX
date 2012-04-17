package com.prasanna.android.stacknetwork.utils;

import android.content.Context;
import android.content.Intent;

import com.prasanna.android.stacknetwork.StackNetworkListActivity;
import com.prasanna.android.stacknetwork.UserProfileActivity;
import com.prasanna.android.stacknetwork.model.User;

public class IntentUtils
{
    public static Intent createUserProfileIntent(Context context, long userId)
    {
	Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
	User user = new User();
	user.id = userId;
	userProfileIntent.putExtra(StringConstants.USER, user);
	return userProfileIntent;
    }

    public static Intent createUserProfileIntent(Context context, String accessToken)
    {
	Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
	User user = new User();
	user.accessToken = accessToken;
	userProfileIntent.putExtra(StringConstants.USER, user);
	return userProfileIntent;
    }

    public static Intent createSiteListIntent(Context context)
    {
	Intent listStackNetworkIntent = new Intent(context, StackNetworkListActivity.class);
	return listStackNetworkIntent.putExtra("allSites", true);
    }

    public static Intent createEmailIntent(String subject, String body)
    {
	String type = "plain/text";
	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	emailIntent.setType(type);
	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
	return emailIntent;
    }
}

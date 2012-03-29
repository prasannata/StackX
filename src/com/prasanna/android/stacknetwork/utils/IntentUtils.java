package com.prasanna.android.stacknetwork.utils;

import android.content.Context;
import android.content.Intent;

import com.prasanna.android.stacknetwork.UserProfileActivity;

public class IntentUtils
{
    public static Intent createUserProfileIntent(Context context, long userId)
    {
        Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        return userProfileIntent;
    }
}

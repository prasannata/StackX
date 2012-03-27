package com.prasanna.android.stacknetwork.utils;

import android.content.Intent;
import android.view.View;

import com.prasanna.android.stacknetwork.UserProfileActivity;

public class IntentUtils
{
    public static Intent createUserProfileIntent(View view, long userId)
    {
        Intent userProfileIntent = new Intent(view.getContext(), UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        return userProfileIntent;
    }
}

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserSitesIntentService extends IntentService
{
    private UserService userService = UserService.getInstance();

    public UserSitesIntentService()
    {
	this(UserSitesIntentService.class.getName());
    }

    public UserSitesIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	ArrayList<Site> sites = null;
	sites = userService.getAllSitesInNetwork();

	Intent broadcastIntent = new Intent();
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.setAction(StringConstants.SITES);
	broadcastIntent.putExtra(StringConstants.SITES, sites);
	sendBroadcast(broadcastIntent);
    }

}

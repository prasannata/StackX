package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.model.Account;
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
        String accessToken = null;
        if (intent.hasExtra(StringConstants.ACCESS_TOKEN))
        {
            accessToken = intent.getStringExtra(StringConstants.ACCESS_TOKEN);
        }

        LinkedHashMap<String, Site> linkSitesMap = userService.getAllSitesInNetwork(accessToken);
        HashMap<String, Account> linkAccountsMap = userService.getAccounts(accessToken, 1);

        if (linkAccountsMap != null && linkSitesMap != null)
        {
            for (String siteUrl : linkAccountsMap.keySet())
            {
                if (linkSitesMap.containsKey(siteUrl) == true)
                {
                    Site site = linkSitesMap.get(siteUrl);
                    Log.d("Usertype for " + siteUrl, linkAccountsMap.get(siteUrl).userType.name());
                    site.userType = linkAccountsMap.get(siteUrl).userType;
                    linkSitesMap.put(siteUrl, site);
                }
            }
        }

        if (linkSitesMap != null)
        {
            Intent broadcastIntent = new Intent();
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.setAction(StringConstants.SITES);
            broadcastIntent.putExtra(StringConstants.SITES, new ArrayList<Site>(linkSitesMap.values()));
            sendBroadcast(broadcastIntent);
        }
    }
}

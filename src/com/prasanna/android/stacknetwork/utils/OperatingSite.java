package com.prasanna.android.stacknetwork.utils;

import com.prasanna.android.stacknetwork.model.Site;

public class OperatingSite
{
    private static Site site;

    public static Site getSite()
    {
	return site;
    }

    public static void setSite(Site site)
    {
	OperatingSite.site = site;
    }
}

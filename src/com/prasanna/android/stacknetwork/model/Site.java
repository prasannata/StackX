package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

import com.prasanna.android.stacknetwork.model.User.UserType;

public class Site implements Serializable
{
    private static final long serialVersionUID = 7057957647981597410L;

    public String name;

    public String apiSiteParameter;

    public String link;

    public String logoUrl;

    public int headerResId;

    public UserType userType = UserType.DOES_NOT_EXIST;
}

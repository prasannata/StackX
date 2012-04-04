package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

import com.prasanna.android.stacknetwork.model.User.UserType;

public class Account implements Serializable
{
    private static final long serialVersionUID = 5454221525646765113L;

    public long id;
    
    public long userId;
    
    public String siteName;
    
    public String siteUrl;
    
    public UserType userType;
}

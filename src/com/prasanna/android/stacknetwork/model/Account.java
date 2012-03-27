package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class Account implements Serializable
{
    private static final long serialVersionUID = 5454221525646765113L;

    private String name;

    private String iconLink;

    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public String getIconLink()
    {
	return iconLink;
    }

    public void setIconLink(String iconLink)
    {
	this.iconLink = iconLink;
    }

}

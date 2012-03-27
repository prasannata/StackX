package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class Site implements Serializable
{

    private static final long serialVersionUID = 7057957647981597410L;

    private String name;

    private String apiSiteParameter;

    private String link;

    private String logoUrl;

    private int headerResId;

    public String getApiSiteParameter()
    {
	return apiSiteParameter;
    }

    public void setApiSiteParameter(String apiSiteParameter)
    {
	this.apiSiteParameter = apiSiteParameter;
    }

    public String getLink()
    {
	return link;
    }

    public void setLink(String link)
    {
	this.link = link;
    }

    public String getLogoUrl()
    {
	return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
	this.logoUrl = logoUrl;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public void setHeaderResId(int headerResId)
    {
	this.headerResId = headerResId;
    }

    public String getName()
    {
	return name;
    }

    public int getHeaderResId()
    {
	return headerResId;
    }
}

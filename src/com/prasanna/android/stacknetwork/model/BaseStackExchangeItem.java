package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public abstract class BaseStackExchangeItem implements Serializable
{
    private static final long serialVersionUID = -7850382261881073395L;

    public long id = -1;

    public int score = 0;

    public String title;

    public User owner;

    public String body;

    public long creationDate;

}

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;


public abstract class BaseUserContribModelWithId implements Serializable
{
    private static final long serialVersionUID = -7850382261881073395L;

    private long id = -1;

    private int score = 0;
    
    private String title;
    
    private User owner;
    
    private String body;

    private long creationDate;
    
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public User getOwner()
    {
        return owner;
    }

    public void setOwner(User owner)
    {
        this.owner = owner;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public long getCreationDate()
    {
	return creationDate;
    }

    public void setCreationDate(long creationDate)
    {
	this.creationDate = creationDate;
    }

    public String getTitle()
    {
	return title;
    }

    public void setTitle(String title)
    {
	this.title = title;
    }
    
    
}

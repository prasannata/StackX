package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class Answer implements Serializable
{
    private static final long serialVersionUID = 536695149282894189L;

    private long id = -1;

    private long questionId = -1;

    private int score;

    private String relativeLink;

    private User owner;

    private boolean accepted;

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

    public String getRelativeLink()
    {
	return relativeLink;
    }

    public void setRelativeLink(String relativeLink)
    {
	this.relativeLink = relativeLink;
    }

    public User getOwner()
    {
	return owner;
    }

    public void setOwner(User owner)
    {
	this.owner = owner;
    }

    public boolean isAccepted()
    {
	return accepted;
    }

    public void setAccepted(boolean accepted)
    {
	this.accepted = accepted;
    }

    public String getBody()
    {
	return body;
    }

    public void setBody(String body)
    {
	this.body = body;
    }

    public long getQuestionId()
    {
	return questionId;
    }

    public void setQuestionId(long questionId)
    {
	this.questionId = questionId;
    }

    public long getCreationDate()
    {
	return creationDate;
    }

    public void setCreationDate(long creationDate)
    {
	this.creationDate = creationDate;
    }
}

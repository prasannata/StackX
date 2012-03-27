package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable
{
    private static final long serialVersionUID = -4722553914475051236L;

    private long id = -1;

    private String title;

    private int viewCount;

    private int answerCount;

    private int votes;

    private int score;

    private String[] tags;

    private String relativeLink;

    private User owner;

    private List<Answer> answers = new ArrayList<Answer>();

    private List<Comment> comments = new ArrayList<Comment>();

    private boolean answered;

    private String body;

    private long createDate;

    private String restEndpoint = "/questions";

    private boolean hasAcceptedAnswer = false;

    public String getTitle()
    {
	return title;
    }

    public void setTitle(String title)
    {
	this.title = title;
    }

    public int getVotes()
    {
	return votes;
    }

    public void setVotes(int votes)
    {
	this.votes = votes;
    }

    public String[] getTags()
    {
	return tags;
    }

    public void setTags(String[] tags)
    {
	this.tags = tags;
    }

    public String getRelativeLink()
    {
	return relativeLink;
    }

    public void setRelativeLink(String relativeLink)
    {
	this.relativeLink = relativeLink;
    }

    public long getId()
    {
	return id;
    }

    public void setId(long id)
    {
	this.id = id;
    }

    public List<Answer> getAnswers()
    {
	return answers;
    }

    public void setAnswers(List<Answer> answers)
    {
	this.answers = answers;
    }

    public List<Comment> getComments()
    {
	return comments;
    }

    public void setComments(List<Comment> comments)
    {
	this.comments = comments;
    }

    public void setAnswered(boolean answered)
    {
	this.answered = answered;
    }

    public boolean isAnswered()
    {
	return answered;
    }

    public int getScore()
    {
	return score;
    }

    public void setScore(int score)
    {
	this.score = score;
    }

    public int getAnswerCount()
    {
	return answerCount;
    }

    public void setAnswerCount(int answerCount)
    {
	this.answerCount = answerCount;
    }

    public int getViewCount()
    {
	return viewCount;
    }

    public void setViewCount(int viewCount)
    {
	this.viewCount = viewCount;
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

    public long getCreateDate()
    {
	return createDate;
    }

    public void setCreateDate(long createDate)
    {
	this.createDate = createDate;
    }

    public String getRestEndpoint()
    {
	if (id > 0)
	    return restEndpoint + id;
	else
	    return null;
    }

    public boolean getHasAcceptedAnswer()
    {
	return hasAcceptedAnswer;
    }

    public void setHasAcceptedAnswer(boolean hasAcceptedAnswer)
    {
	this.hasAcceptedAnswer = hasAcceptedAnswer;
    }
}

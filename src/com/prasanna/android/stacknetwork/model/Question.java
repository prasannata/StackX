package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question extends BaseUserContribModelWithId implements Serializable
{
    private static final long serialVersionUID = -4722553914475051236L;

    private int viewCount;

    private int answerCount;

    private int votes;

    private String[] tags;

    private String relativeLink;

    private List<Answer> answers = new ArrayList<Answer>();

    private List<Comment> comments;

    private boolean answered;

    private boolean hasAcceptedAnswer = false;

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

    public boolean getHasAcceptedAnswer()
    {
	return hasAcceptedAnswer;
    }

    public void setHasAcceptedAnswer(boolean hasAcceptedAnswer)
    {
	this.hasAcceptedAnswer = hasAcceptedAnswer;
    }
}

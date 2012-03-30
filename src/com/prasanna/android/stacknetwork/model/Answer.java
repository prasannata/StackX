package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.List;

public class Answer extends BaseUserContribModelWithId implements Serializable
{
    private static final long serialVersionUID = -307252373087687685L;

    private long questionId = -1;

    private String relativeLink;

    private boolean accepted;

    private List<Comment> comments;

    public String getRelativeLink()
    {
	return relativeLink;
    }

    public void setRelativeLink(String relativeLink)
    {
	this.relativeLink = relativeLink;
    }

    public boolean isAccepted()
    {
	return accepted;
    }

    public void setAccepted(boolean accepted)
    {
	this.accepted = accepted;
    }

    public long getQuestionId()
    {
	return questionId;
    }

    public void setQuestionId(long questionId)
    {
	this.questionId = questionId;
    }

    public List<Comment> getComments()
    {
	return comments;
    }

    public void setComments(List<Comment> comments)
    {
	this.comments = comments;
    }
}

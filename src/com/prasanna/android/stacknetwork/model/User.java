package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class User implements Serializable
{
    private static final long serialVersionUID = -5427063287288616795L;

    private long id;
    
    private long accountId;

    private String displayName;

    private Bitmap avatar;

    private int reputation;

    private int[] badgeCounts;

    private String profileImageLink;

    private int acceptRate = -1;
    
    private int questionCount;
    
    private int answerCount;
    
    private int upvoteCount;
    
    private int downvoteCount;
    
    private int profileViews;
    
    private List<Account> accounts = new ArrayList<Account>();
    
    private long lastAccessTime;
    
    public long getId()
    {
	return id;
    }

    public void setId(long id)
    {
	this.id = id;
    }

    public String getDisplayName()
    {
	return displayName;
    }

    public void setDisplayName(String displayName)
    {
	this.displayName = displayName;
    }

    public int getReputation()
    {
	return reputation;
    }

    public void setReputation(int reputation)
    {
	this.reputation = reputation;
    }

    public int[] getBadgeCounts()
    {
	return badgeCounts;
    }

    public void setBadgeCounts(int[] badgeCounts)
    {
	this.badgeCounts = badgeCounts;
    }

    public String getProfileImageLink()
    {
	return profileImageLink;
    }

    public void setProfileImageLink(String profileImageLink)
    {
	this.profileImageLink = profileImageLink;
    }

    public int getAcceptRate()
    {
	return acceptRate;
    }

    public void setAcceptRate(int acceptRate)
    {
	this.acceptRate = acceptRate;
    }

    public Bitmap getAvatar()
    {
	return avatar;
    }

    public void setAvatar(Bitmap avatar)
    {
	this.avatar = avatar;
    }

    public List<Account> getAccounts()
    {
	return accounts;
    }

    public void setAccounts(List<Account> accounts)
    {
	this.accounts = accounts;
    }

    public int getQuestionCount()
    {
        return questionCount;
    }

    public void setQuestionCount(int questionCount)
    {
        this.questionCount = questionCount;
    }

    public int getAnswerCount()
    {
        return answerCount;
    }

    public void setAnswerCount(int answerCount)
    {
        this.answerCount = answerCount;
    }

    public int getUpvoteCount()
    {
        return upvoteCount;
    }

    public void setUpvoteCount(int upvoteCount)
    {
        this.upvoteCount = upvoteCount;
    }

    public int getDownvoteCount()
    {
        return downvoteCount;
    }

    public void setDownvoteCount(int downvoteCount)
    {
        this.downvoteCount = downvoteCount;
    }

    public int getProfileViews()
    {
        return profileViews;
    }

    public void setProfileViews(int profileViews)
    {
        this.profileViews = profileViews;
    }

    public long getAccountId()
    {
	return accountId;
    }

    public void setAccountId(long accountId)
    {
	this.accountId = accountId;
    }

    public long getLastAccessTime()
    {
	return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime)
    {
	this.lastAccessTime = lastAccessTime;
    }

}

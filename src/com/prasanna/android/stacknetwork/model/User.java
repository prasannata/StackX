package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Bitmap;

public class User implements Serializable
{
    private static final long serialVersionUID = -5427063287288616795L;

    public long id;

    public long accountId;

    public String displayName;

    public Bitmap avatar;

    public int reputation;

    public int[] badgeCounts;

    public String profileImageLink;

    public int acceptRate = -1;

    public int questionCount;

    public int answerCount;

    public int upvoteCount;

    public int downvoteCount;

    public int profileViews;

    public ArrayList<Account> accounts;

    public long lastAccessTime;
    
    public String accessToken;
}

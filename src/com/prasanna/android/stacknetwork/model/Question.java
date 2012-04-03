package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Question extends BaseStackExchangeItem implements Serializable
{
    private static final long serialVersionUID = -4722553914475051236L;

    public int viewCount;

    public int answerCount;

    public int votes;

    public String[] tags;

    public String relativeLink;

    public ArrayList<Answer> answers = new ArrayList<Answer>();

    public ArrayList<Comment> comments;

    public boolean answered;

    public boolean hasAcceptedAnswer = false;
}

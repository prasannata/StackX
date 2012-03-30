package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question extends BaseUserContribModelWithId implements Serializable
{
    private static final long serialVersionUID = -4722553914475051236L;

    public int viewCount;

    public int answerCount;

    public int votes;

    public String[] tags;

    public String relativeLink;

    public List<Answer> answers = new ArrayList<Answer>();

    public List<Comment> comments;

    public boolean answered;

    public boolean hasAcceptedAnswer = false;
}

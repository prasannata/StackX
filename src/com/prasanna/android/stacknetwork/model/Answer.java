package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;
import java.util.List;

public class Answer extends BaseStackExchangeItem implements Serializable
{
    private static final long serialVersionUID = -307252373087687685L;

    public long questionId = -1;

    public String relativeLink;

    public boolean accepted;

    public List<Comment> comments;
}

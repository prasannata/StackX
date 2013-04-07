package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class IdentifiableItem implements Serializable
{
    private static final long serialVersionUID = 6939413330720394521L;
    
    public long id = -1;
    
    public String link;

    public long creationDate;

}

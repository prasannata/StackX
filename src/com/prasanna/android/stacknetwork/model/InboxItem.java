package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class InboxItem implements Serializable
{
    public static enum ItemType
    {
        COMMENT,
        CHAT_MESSAGE,
        NEW_ANSWER,
        CAREERS_MESSAGE,
        CAREERS_INVITATIONS,
        META_QUESTION,
        POST_NOTICE;

        public static ItemType getValue(String string)
        {
            ItemType itemType = null;

            if (string != null)
            {
                try
                {
                    itemType = valueOf(string);
                }
                catch (IllegalArgumentException e)
                {
                    itemType = null;
                }

            }
            return itemType;
        }
    }

    private static final long serialVersionUID = 7015749387787062217L;

    public long questionId;

    public long answerId;

    public long commentId;

    public String title;

    public ItemType itemType;

    public Site site;

    public String body;
    
    public long creationDate;
}

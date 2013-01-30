package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class WritePermission implements Serializable
{
    private static final long serialVersionUID = -1311818798901923216L;

    public static final String PREF_LAST_COMMENT_WRITE = "pref_last_comment_write";
    public static final String PREF_MIN_SECONDS_BETWEEN_WRITE = "pref_min_seconds_between_write";
    
    public enum ObjectType
    {
        ANSWER("answer"),
        COMMENT("comment"),
        QUESTION("question");

        private final String value;

        ObjectType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public static ObjectType getEnum(String value)
        {
            if (value == null)
                return null;

            try
            {
                return valueOf(value.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
            }

            return null;
        }
    }

    public boolean canAdd = false;
    public boolean canDelete = false;
    public boolean canEdit = false;
    public int maxDailyActions = 0;
    public int minSecondsBetweenActions = -1;
    public ObjectType objectType;
    public long userId;
}

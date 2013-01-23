package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class WritePermission implements Serializable
{
    private static final long serialVersionUID = -1311818798901923216L;

    public enum ObjectType
    {
        QUESTION("question"),
        COMMENT("comment");

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

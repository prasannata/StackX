package com.prasanna.android.stacknetwork.utils;

public class AppUtils
{
    public static String formatUserReputation(int reputation)
    {
        String reputationString = "";

        if (reputation > 10000)
        {
            float reputationInThousands = ((float) reputation) / 1000f;
            reputationString += " " + String.format("(%.1fk)", reputationInThousands);
        }
        else
        {
            reputationString += " (" + reputation + ")";
        }
        return reputationString;
    }
}

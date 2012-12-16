/*
    Copyright 2012 Prasanna Thirumalai
    
    This file is part of StackX.

    StackX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    StackX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with StackX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.prasanna.android.stacknetwork.utils;

public class DateTimeUtils
{
    public static final String SECOND = "second";
    public static final String MINUTE = "minute";
    public static final String HOUR = "hour";
    public static final String DAY = "day";
    public static final String MONTH = "month";
    public static final String YEAR = "year";
    public static final int SECONDS_IN_MIN = 60;
    public static final int SECONDS_IN_HOUR = 3600;
    public static final int SECONDS_IN_DAY = 3600 * 24;
    public static final int DAYS_IN_MONTH = 30;
    public static final int MONTHS_IN_YEAR = 12;

    public static String getElapsedDurationSince(long seconds)
    {
        String duration = null;
        String unit = SECOND;
        String residueUnit = null;

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        long elapsedTimeInSeconds = currentTimeInSeconds - seconds;

        int count = 0;
        int residue = 0;

        if (elapsedTimeInSeconds > SECONDS_IN_MIN && elapsedTimeInSeconds < SECONDS_IN_HOUR)
        {
            count = (int) (elapsedTimeInSeconds / SECONDS_IN_MIN);
            unit = MINUTE;
        }
        else if (elapsedTimeInSeconds > SECONDS_IN_HOUR && elapsedTimeInSeconds < SECONDS_IN_DAY)
        {
            count = (int) (elapsedTimeInSeconds / SECONDS_IN_HOUR);
            unit = HOUR;
        }
        else if (elapsedTimeInSeconds > SECONDS_IN_DAY)
        {
            count = (int) (elapsedTimeInSeconds / SECONDS_IN_DAY);
            if (count > DAYS_IN_MONTH)
            {

                residue = count % DAYS_IN_MONTH;
                count /= DAYS_IN_MONTH;

                if (count > MONTHS_IN_YEAR)
                {
                    residue = count % MONTHS_IN_YEAR;
                    count /= MONTHS_IN_YEAR;
                    unit = YEAR;
                    residueUnit = MONTH;
                }
                else
                {
                    unit = MONTH;
                    residueUnit = DAY;
                }
            }
            else
            {
                unit = DAY;
            }
        }

        duration = new String();
        duration = count + " " + getUnit(count, unit);
        if (residue > 0)
        {
            duration += " and " + residue + " " + getUnit(residue, residueUnit);
        }

        duration += " ago";
        return duration;
    }

    private static String getUnit(int count, String unit)
    {
        if (count > 1)
        {
            unit += "s";
        }

        return unit;
    }
}

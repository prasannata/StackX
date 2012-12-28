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

/*
 Copyright (C) 2012 Prasanna Thirumalai

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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MarkdownFormatter
{
    private static final String TAG = MarkdownFormatter.class.getSimpleName();
    private static final String NEW_LINE_STR = System.getProperty("line.separator");

    public static class Tags
    {
        public static final String CODE = "code";
        public static final String IMG = "img";
    }

    public static ArrayList<TextView> format(Context context, String markdownText)
    {
        ArrayList<TextView> views = new ArrayList<TextView>();
        try
        {
            if (markdownText != null)
            {
                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
                xmlPullParser.setInput(new StringReader(markdownText));
                int eventType = xmlPullParser.getEventType();
                StringBuffer buffer = new StringBuffer();

                boolean codeFound = false;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(2, 5, 2, 5);
                while (eventType != XmlPullParser.END_DOCUMENT)
                {

                    if (eventType == XmlPullParser.START_DOCUMENT)
                    {
                    }
                    else if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xmlPullParser.getName().equals(Tags.CODE))
                        {
                            codeFound = true;
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG)
                    {
                        if (xmlPullParser.getName().equals(Tags.CODE))
                        {
                            codeFound = false;
                        }
                    }
                    else if (eventType == XmlPullParser.TEXT)
                    {
                        String text = xmlPullParser.getText();

                        if (codeFound)
                        {
                            if (!text.contains(NEW_LINE_STR))
                            {
                                if (buffer.length() > 0 && buffer.lastIndexOf(NEW_LINE_STR) == buffer.length() - 1)
                                    buffer.setCharAt(buffer.length() - 1, ' ');
                                buffer.append(text);
                            }
                            else
                            {
                                views.add(getTextViewForCode(context, params, text));
                            }
                        }
                        else
                        {
                            buffer.append(text);

                            views.add(getTextView(context, params, buffer));
                            buffer.delete(0, buffer.length());
                        }
                    }

                    eventType = xmlPullParser.next();
                }
            }
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, "Error parsing: " + e);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error parsing: " + e);
        }
        return views;

    }

    private static TextView getTextView(Context context, LinearLayout.LayoutParams params, StringBuffer buffer)
    {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(params);
        textView.setText(Html.fromHtml(buffer.toString()));
        return textView;
    }

    private static TextView getTextViewForCode(Context context, LinearLayout.LayoutParams params, String text)
    {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(params);
        textView.setTextSize(10f);
        textView.setBackgroundResource(com.prasanna.android.stacknetwork.R.color.lightGrey);
        textView.setText(text);
        textView.setPadding(0, 0, 0, 0);
        return textView;
    }
}
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
import java.io.StringWriter;
import java.util.ArrayList;

import org.htmlcleaner.CleanerTransformations;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagTransformation;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;

public class MarkdownFormatter
{
    private static final String TAG = MarkdownFormatter.class.getSimpleName();
    private static final String NEW_LINE_STR = System.getProperty("line.separator");

    public static class Tags
    {
        public static final String CODE = "code";
        public static final String IMG = "img";
        public static final String BR = "br";
    }

    private static String clean(String markdownText) throws IOException
    {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerTransformations transformations = new CleanerTransformations();
        TagTransformation tt = new TagTransformation("br", "br/", true);
        transformations.addTransformation(tt);
        cleaner.setTransformations(transformations);
        TagNode node = cleaner.clean(markdownText);
        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
        serializer.write(node, new StringWriter(), "utf-8");
        return serializer.getAsString(node);
    }

    /**
     * Format HTML text to fit into one or more vertically aligned text views.
     * Parses the given text and removes {@code <code> </code>} tags. If the
     * code text is of multiple lines a new {@link android.widget.TextView
     * TextView} is created and added to the view container else the code text
     * is added to already created {@link android.widget.TextView TextView}.
     * 
     * @param context
     * @param markdownText
     * @return
     */
    public static ArrayList<TextView> parse(Context context, String markdownText)
    {
        ArrayList<TextView> views = new ArrayList<TextView>();
        try
        {
            if (markdownText != null)
            {
                markdownText = clean(markdownText);

                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
                xmlPullParser.setInput(new StringReader(markdownText));
                int eventType = xmlPullParser.getEventType();
                StringBuffer buffer = new StringBuffer();
                StringBuffer code = new StringBuffer();

                boolean codeFound = false;
                boolean oneLineCode = false;

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if (eventType == XmlPullParser.START_DOCUMENT)
                    {
                    }
                    else if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xmlPullParser.getName().equals(Tags.CODE))
                            codeFound = true;
                        else
                            buffer.append("<" + xmlPullParser.getName() + ">");
                    }
                    else if (eventType == XmlPullParser.END_TAG)
                    {
                        if (xmlPullParser.getName().equals(Tags.CODE))
                        {
                            codeFound = false;

                            if (oneLineCode)
                                oneLineCode = false;
                            else
                            {
                                addSimpleTextToView(context, views, buffer, params);
                                views.add(getTextViewForCode(context, code.toString()));
                                buffer.delete(0, code.length());
                            }
                        }
                        else
                        {
                            buffer.append("<" + xmlPullParser.getName() + "/>");
                        }
                    }
                    else if (eventType == XmlPullParser.TEXT)
                    {
                        String text = xmlPullParser.getText();

                        if (codeFound)
                        {
                            if (!text.contains(NEW_LINE_STR))
                            {
                                if (buffer.length() > 0
                                        && buffer.lastIndexOf(NEW_LINE_STR) == buffer.length() - 1)
                                    buffer.setCharAt(buffer.length() - 1, ' ');

                                buffer.append(text);
                                oneLineCode = true;
                            }
                            else
                            {
                                code.append(text);
                            }
                        }
                        else
                        {
                            text = text.replace("\n", " ").replace("\r", " ");
                            buffer.append(text);
                        }
                    }

                    eventType = xmlPullParser.next();
                }

                addSimpleTextToView(context, views, buffer, params);
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

    private static void addSimpleTextToView(Context context, ArrayList<TextView> views,
            StringBuffer buffer, LinearLayout.LayoutParams params)
    {
        if (buffer.length() > 0)
        {
            views.add(getTextView(context, params, buffer));
            buffer.delete(0, buffer.length());
        }
    }

    private static TextView getTextView(Context context, LinearLayout.LayoutParams params,
            StringBuffer buffer)
    {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(params);
        textView.setAutoLinkMask(Linkify.WEB_URLS);
        textView.setTextSize(10f);
        textView.setTextIsSelectable(true);
        textView.setText(Html.fromHtml(buffer.toString()));
        return textView;
    }

    private static TextView getTextViewForCode(Context context, String text)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(context);
        textView.setTextColor(context.getResources().getColor(R.color.delft));
        textView.setLayoutParams(params);
        textView.setTextSize(10f);
        textView.setBackgroundColor(context.getResources().getColor(R.color.codeBg));
        textView.setTextIsSelectable(true);
        textView.setPadding(5, 5, 5, 5);
        textView.setText(text);
        return textView;
    }
}
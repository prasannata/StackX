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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HtmlTagFragmenter
{
    public static ArrayList<TextView> parse(Context context, String htmlText)
    {
        ArrayList<TextView> codeSnippet = new ArrayList<TextView>();
        try
        {

            if (htmlText != null)
            {
                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
                xmlPullParser.setInput(new StringReader(htmlText));
                int eventType = xmlPullParser.getEventType();

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
                        if (xmlPullParser.getName().equals("code"))
                        {
                            codeFound = true;
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG)
                    {
                        if (xmlPullParser.getName().equals("code"))
                        {
                            codeFound = false;
                        }
                    }
                    else if (eventType == XmlPullParser.TEXT)
                    {
                        TextView textView = new TextView(context);
                        textView.setTextColor(Color.BLACK);
                        textView.setLayoutParams(params);
//                        textView.setLinksClickable(true);
//                        textView.setMovementMethod(LinkMovementMethod.getInstance());

                        if (codeFound)
                        {
                            textView.setTextSize(10f);
                            textView.setBackgroundResource(com.prasanna.android.stacknetwork.R.color.lightGrey);
                            textView.setText(xmlPullParser.getText());
                            textView.setPadding(0, 0, 0, 0);
                        }
                        else
                        {
                            textView.setText(Html.fromHtml(xmlPullParser.getText()));
                        }

                        codeSnippet.add(textView);
                    }

                    eventType = xmlPullParser.next();
                }
            }
        }
        catch (XmlPullParserException e)
        {

        }
        catch (IOException e)
        {

        }
        return codeSnippet;

    }
}

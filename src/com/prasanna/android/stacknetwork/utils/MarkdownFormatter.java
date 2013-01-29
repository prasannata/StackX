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
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.http.protocol.HTTP;
import org.htmlcleaner.CleanerTransformations;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagTransformation;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.FullscreenTextActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.task.AsyncTaskCompletionNotifier;
import com.prasanna.android.task.GetImageAsyncTask;

public class MarkdownFormatter
{
    private static final String TAG = MarkdownFormatter.class.getSimpleName();
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String CR = "\r";

    public static class Tags
    {
        public static final String CODE = "code";
        public static final String IMG = "img";
        public static final String BR = "br";
    }

    public static class Attributes
    {
        public static final String SRC = "src";
    }

    private static class AsyncTaskCompletionNotifierImagePopup implements AsyncTaskCompletionNotifier<Bitmap>
    {
        private final Context context;
        private Dialog progressDialog;

        public AsyncTaskCompletionNotifierImagePopup(Context context, Dialog progressDialog)
        {
            this.context = context;
            this.progressDialog = progressDialog;
        }

        @Override
        public void notifyOnCompletion(Bitmap result)
        {
            if (progressDialog != null)
                progressDialog.dismiss();

            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(result);
            showImageDialog(context, imageView);
        }

        private void showImageDialog(final Context context, ImageView imageView)
        {
            AlertDialog.Builder imageDialog = new AlertDialog.Builder(context);
            imageDialog.setView(imageView);
            String hide = context.getResources().getString(R.string.hide);
            imageDialog.setPositiveButton(hide, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            imageDialog.show();
        }
    };

    public static String escapeHtml(CharSequence text)
    {
        if (text == null)
            return null;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);

            if (c == '<')
                builder.append("&lt;");
            else if (c == '>')
                builder.append("&gt;");
            else if (c == '&')
                builder.append("&amp;");
            else
                builder.append(c);

        }
        return builder.toString();
    }

    private static String clean(String markdownText) throws IOException
    {
        HtmlCleaner cleaner = new HtmlCleaner();

        CleanerTransformations transformations = new CleanerTransformations();
        transformations.addTransformation(new TagTransformation(Tags.BR, Tags.BR + "/", true));
        cleaner.setTransformations(transformations);

        TagNode node = cleaner.clean(markdownText);

        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
        serializer.write(node, new StringWriter(), HTTP.UTF_8);

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
    public static ArrayList<View> parse(Context context, String markdownText)
    {
        if (context != null && markdownText != null)
        {
            ArrayList<View> views = new ArrayList<View>();
            try
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
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if (eventType == XmlPullParser.START_DOCUMENT)
                    {
                    }
                    else if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xmlPullParser.getName().equals(Tags.CODE))
                            codeFound = true;
                        else if (xmlPullParser.getName().equals(Tags.IMG))
                        {
                            addSimpleTextToView(context, views, buffer, params);

                            String attributeValue = xmlPullParser.getAttributeValue(null, Attributes.SRC);
                            addImgLinkText(context, views, attributeValue, params);
                        }
                        else
                        {
                            buffer.append("<" + xmlPullParser.getName());
                            for (int i = 0; i < xmlPullParser.getAttributeCount(); i++)
                            {
                                buffer.append(" " + xmlPullParser.getAttributeName(i) + "=\""
                                                + xmlPullParser.getAttributeValue(i) + "\"");
                            }

                            buffer.append(">");
                        }
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
                                code.delete(0, code.length());
                            }
                        }
                        else if (xmlPullParser.getName().equals(Tags.IMG))
                        {
                            Log.v(TAG, "Ignore img tag");
                        }
                        else
                        {
                            buffer.append("</" + xmlPullParser.getName() + ">");
                        }
                    }
                    else if (eventType == XmlPullParser.TEXT)
                    {
                        String text = xmlPullParser.getText();

                        if (codeFound)
                        {
                            if (!text.contains(NEW_LINE))
                            {
                                if (buffer.length() > 0 && buffer.lastIndexOf(NEW_LINE) == buffer.length() - 1)
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
                            text = text.replace(NEW_LINE, " ").replace(CR, " ");
                            buffer.append(text);
                        }
                    }

                    eventType = xmlPullParser.next();
                }

                addSimpleTextToView(context, views, buffer, params);
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
        return null;

    }

    private static void addSimpleTextToView(Context context, ArrayList<View> views, StringBuffer buffer,
                    LinearLayout.LayoutParams params)
    {
        if (buffer.length() > 0)
        {
            views.add(getTextView(context, params, buffer));
            buffer.delete(0, buffer.length());
        }
    }

    private static void addImgLinkText(final Context context, ArrayList<View> views, final String url,
                    LinearLayout.LayoutParams params)
    {
        final TextView textView = new TextView(context);
        textView.setTextColor(Color.BLUE);
        textView.setLayoutParams(params);
        textView.setText("View image");
        textView.setTextSize(12f);
        textView.setPadding(3, 3, 3, 3);
        textView.setTag(url);
        textView.setClickable(true);
        setupOnLinkClick(context, url, textView);
        views.add(textView);
    }

    private static void setupOnLinkClick(final Context context, final String url, final TextView textView)
    {
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder imageDialog = new AlertDialog.Builder(context);
                ProgressBar progressBar = new ProgressBar(context);
                imageDialog.setView(progressBar);
                AlertDialog progressDialog = imageDialog.create();
                progressDialog.show();
                new GetImageAsyncTask(new AsyncTaskCompletionNotifierImagePopup(context, progressDialog)).execute(url);
            }
        });
    }

    private static TextView getTextView(Context context, LinearLayout.LayoutParams params, StringBuffer buffer)
    {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(params);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setTextSize(12f);
        textView.setText(Html.fromHtml(buffer.toString()));
        return textView;
    }

    private static RelativeLayout getTextViewForCode(final Context context, final String text)
    {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout codeLayout = (RelativeLayout) inflater.inflate(R.layout.code, null);
        final TextView textView = (TextView) codeLayout.findViewById(R.id.code);
        textView.setText(text);

        ImageView imageView = (ImageView) codeLayout.findViewById(R.id.copyCode);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipDescription clipDescription = new ClipDescription("Stackx code", new String[]
                { ClipDescription.MIMETYPE_TEXT_PLAIN });
                clipboard.setPrimaryClip(new ClipData(clipDescription, new Item(text)));
                Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show();
            }
        });

        imageView = (ImageView) codeLayout.findViewById(R.id.fullScreenImg);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, FullscreenTextActivity.class);
                intent.putExtra(StringConstants.TEXT, textView.getText());
                context.startActivity(intent);
            }
        });

        return codeLayout;
    }
};
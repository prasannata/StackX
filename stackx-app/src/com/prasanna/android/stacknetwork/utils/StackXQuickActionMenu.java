/*
    Copyright (C) 2013 Prasanna Thirumalai
    
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

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnShowCommentsListener;
import com.prasanna.android.views.QuickActionItem;
import com.prasanna.android.views.QuickActionMenu;

public class StackXQuickActionMenu
{
    private QuickActionMenu quickActionMenu;
    private Activity currentActivity;

    public StackXQuickActionMenu(Activity currentActivity)
    {
        this.currentActivity = currentActivity;
        quickActionMenu = new QuickActionMenu(currentActivity);
    }

    public static StackXQuickActionMenu newMenu(Activity currentActivity)
    {
        return new StackXQuickActionMenu(currentActivity);
    }

    public StackXQuickActionMenu addUserProfileItem(final long userId, final String userName)
    {
        quickActionMenu.addActionItem(new QuickActionItem(userName + "'s profile", new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityStartHelper.startUserProfileActivity(currentActivity, userId);
            }
        }));

        return this;
    }

    public StackXQuickActionMenu addSimilarQuestionsItem(final String title)
    {
        quickActionMenu.addActionItem(new QuickActionItem(currentActivity, R.string.similar, new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityStartHelper.startSimilarQuestionActivity(currentActivity, title);
            }
        }));

        return this;
    }

    public StackXQuickActionMenu addRelatedQuickActionItem(final long questionId)
    {
        quickActionMenu.addActionItem(new QuickActionItem(currentActivity, R.string.related, new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityStartHelper.startRelatedQuestionActivity(currentActivity, questionId);
            }
        }));
        return this;
    }

    public StackXQuickActionMenu addEmailQuickActionItem(final String subject, final String body)
    {
        quickActionMenu.addActionItem(new QuickActionItem(currentActivity, R.string.email, new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityStartHelper.startEmailActivity(currentActivity, subject, body);
            }
        }));

        return this;
    }

    public StackXQuickActionMenu addCommentsItem(final OnShowCommentsListener onShowCommentsListener)
    {
        quickActionMenu.addActionItem(new QuickActionItem(currentActivity, R.string.comments, new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onShowCommentsListener.onShowComments();
            }
        }));

        return this;
    }

    public QuickActionMenu build()
    {
        return quickActionMenu;
    }
}

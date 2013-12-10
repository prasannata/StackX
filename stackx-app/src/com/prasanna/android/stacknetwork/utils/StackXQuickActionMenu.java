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

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnShowCommentsListener;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.views.QuickActionItem;
import com.prasanna.android.views.QuickActionMenu;

public class StackXQuickActionMenu {
  private QuickActionMenu quickActionMenu;
  private Context context;

  public StackXQuickActionMenu(Context context) {
    this.context = context;
    quickActionMenu = new QuickActionMenu(context);
  }

  public static StackXQuickActionMenu newMenu(Context context) {
    return new StackXQuickActionMenu(context);
  }

  public StackXQuickActionMenu addUserProfileItem(final long userId, final String userName) {
    quickActionMenu.addActionItem(new QuickActionItem(userName + "'s profile", new OnClickListener() {
      @Override
      public void onClick(View v) {
        ActivityStartHelper.startUserProfileActivityForDefaultSite(context, userId);
      }
    }));

    return this;
  }

  public StackXQuickActionMenu addUserProfileItem(final long userId, final String userName, final Site site) {
    quickActionMenu.addActionItem(new QuickActionItem(userName + "'s profile", new OnClickListener() {
      @Override
      public void onClick(View v) {
        ActivityStartHelper.startUserProfileActivity(context, userId, site);
      }
    }));

    return this;
  }

  public StackXQuickActionMenu addSimilarQuestionsItem(final String title) {
    quickActionMenu.addActionItem(new QuickActionItem(context, R.string.similar, new OnClickListener() {
      @Override
      public void onClick(View v) {
        ActivityStartHelper.startSimilarQuestionActivity(context, title);
      }
    }));

    return this;
  }

  public StackXQuickActionMenu addRelatedQuickActionItem(final long questionId) {
    quickActionMenu.addActionItem(new QuickActionItem(context, R.string.related, new OnClickListener() {
      @Override
      public void onClick(View v) {
        ActivityStartHelper.startRelatedQuestionActivity(context, questionId);
      }
    }));
    return this;
  }

  public StackXQuickActionMenu addEmailQuickActionItem(final String subject, final String body) {
    quickActionMenu.addActionItem(new QuickActionItem(context, R.string.email, new OnClickListener() {
      @Override
      public void onClick(View v) {
        ActivityStartHelper.startEmailActivity(context, subject, body);
      }
    }));

    return this;
  }

  public StackXQuickActionMenu addCommentsItem(final OnShowCommentsListener onShowCommentsListener) {
    quickActionMenu.addActionItem(new QuickActionItem(context, R.string.comments, new OnClickListener() {
      @Override
      public void onClick(View v) {
        onShowCommentsListener.onShowComments();
      }
    }));

    return this;
  }

  public StackXQuickActionMenu addCopyToClipboardItem(final String text) {
    quickActionMenu.addActionItem(new QuickActionItem(context, android.R.string.copy, new OnClickListener() {
      @Override
      public void onClick(View v) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipDescription clipDescription =
            new ClipDescription("Stackx code", new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });
        clipboard.setPrimaryClip(new ClipData(clipDescription, new Item(text)));
        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show();
      }
    }));

    return this;
  }

  public StackXQuickActionMenu addItem(final int titleResId, final OnClickListener onClickListener) {
    return addItem(context.getString(titleResId), onClickListener);
  }

  public StackXQuickActionMenu addItem(final String title, final OnClickListener onClickListener) {
    quickActionMenu.addActionItem(new QuickActionItem(title, new OnClickListener() {
      @Override
      public void onClick(View v) {
        onClickListener.onClick(v);
      }
    }));

    return this;
  }

  public StackXQuickActionMenu setOnDismissListener(OnDismissListener onDismissListener) {
    quickActionMenu.setOnDismissListener(onDismissListener);
    return this;
  }

  public QuickActionMenu build() {
    return quickActionMenu;
  }
}

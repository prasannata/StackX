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
package com.prasanna.android.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class DialogPreferenceImpl extends DialogPreference {
  private OnClickListener onClickListener;

  public DialogPreferenceImpl(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DialogPreferenceImpl(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (onClickListener != null)
      onClickListener.onClick(dialog, which);
  }

  public void setOnClickListener(OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
  }
}

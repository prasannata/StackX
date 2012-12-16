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

package com.prasanna.android.stacknetwork.fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.prasanna.android.stacknetwork.intent.TagFaqIntentService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class TagFaqFragment extends AbstractQuestionsFragment
{
    private static final String TAG = TagFaqFragment.class.getSimpleName();

    private int currentPage = 0;

    private String qTag;

    private Intent tagFaqIntent;

    public TagFaqFragment()
    {
        currentPage = 0;
        items.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (qTag != null)
        {
            registerReceiver();

            showLoadingDialog();

            startIntentService();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (items != null && items.isEmpty() == false)
        {
            outState.putSerializable(StringConstants.QUESTIONS, items);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void startIntentService()
    {

        tagFaqIntent = getIntentForService(TagFaqIntentService.class,
                IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
        tagFaqIntent.setAction(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
        tagFaqIntent.putExtra(QuestionIntentAction.TAGS_FAQ.getExtra(), qTag);
        tagFaqIntent.putExtra(StringConstants.PAGE, ++currentPage);
        getActivity().startService(tagFaqIntent);
        serviceRunning = true;
    }

    @Override
    protected void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(receiver, filter);
    }

    public String getqTag()
    {
        return qTag;
    }

    public void setqTag(String qTag)
    {
        this.qTag = qTag;
    }

    @Override
    public String getLogTag()
    {
        return TAG;
    }
}

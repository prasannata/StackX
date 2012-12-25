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

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.content.Intent;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class TagFaqIntentService extends AbstractIntentService
{
    private QuestionService questionService = QuestionService.getInstance();

    public TagFaqIntentService()
    {
	this(TagFaqIntentService.class.getSimpleName());
    }

    public TagFaqIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	try
	{
	    String tag = intent.getStringExtra(QuestionIntentAction.TAGS_FAQ.getExtra());
	    int page = intent.getIntExtra(StringConstants.PAGE, 1);

	    ArrayList<Question> questions = questionService.getFaqForTag(tag, page);

	    Intent broadcastIntent = new Intent();
	    broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.TAGS_FAQ.name());
	    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	    broadcastIntent.putExtra(QuestionIntentAction.QUESTIONS.getExtra(), questions);
	    sendBroadcast(broadcastIntent);
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}
    }

}

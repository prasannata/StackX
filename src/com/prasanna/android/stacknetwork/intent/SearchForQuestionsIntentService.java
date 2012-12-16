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

import android.app.SearchManager;
import android.content.Intent;
import android.util.Log;

import com.prasanna.android.stacknetwork.exceptions.HttpErrorException;
import com.prasanna.android.stacknetwork.service.QuestionService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class SearchForQuestionsIntentService extends AbstractIntentService
{
    private static final String TAG = SearchForQuestionsIntentService.class.getSimpleName();

    public SearchForQuestionsIntentService()
    {
	this("SearchForQuestionsIntentService");
    }

    public SearchForQuestionsIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	try
	{
	    String query = intent.getStringExtra(SearchManager.QUERY);
	    int page = intent.getIntExtra(StringConstants.PAGE, 1);

	    Log.d(TAG, "Received search query: " + query);

	    Intent broadcastIntent = new Intent();
	    broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.name());
	    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	    broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.QUESTION_SEARCH.getExtra(), QuestionService
		            .getInstance().search(query, page));
	    sendBroadcast(broadcastIntent);
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getCode(), e.getMessage());
	}
    }
}

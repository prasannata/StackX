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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class InboxItem extends StackXItem implements Serializable
{
    public static enum ItemType
    {
	COMMENT("comment"),
	CHAT_MESSAGE("chat message"),
	NEW_ANSWER("answer"),
	CAREERS_MESSAGE("career message"),
	CAREERS_INVITATIONS("career invite"),
	META_QUESTION("meta question"),
	POST_NOTICE("post notice"),
	MODERATOR_MESSAGE("message from moderator");

	private final String repr;

	ItemType(String repr)
	{
	    this.repr = repr;
	}

	public static ItemType getValue(String string)
	{
	    ItemType itemType = null;

	    if (string != null)
	    {
		try
		{
		    itemType = valueOf(string.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
		    itemType = null;
		}

	    }
	    return itemType;
	}

	public String getNotificationTitle(int count)
	{
	    String plural = (count > 1) ? "s" : "";

	    return count + " new " + getRepr() + plural;
	}

	public String getRepr()
	{
	    return repr;
	}
    }

    private static final long serialVersionUID = 7015749387787062217L;

    public long questionId;

    public long answerId;

    public long commentId;

    public ItemType itemType;

    public Site site;

    public boolean unread = false;
}

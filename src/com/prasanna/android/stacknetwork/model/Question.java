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
import java.util.ArrayList;

public class Question extends StackXItem implements Serializable
{
    private static final long serialVersionUID = -4722553914475051236L;

    public int viewCount;

    public int answerCount;

    public int votes;

    public String[] tags;

    public String relativeLink;

    public ArrayList<Answer> answers;

    public ArrayList<Comment> comments;

    public boolean answered;

    public boolean hasAcceptedAnswer = false;
}

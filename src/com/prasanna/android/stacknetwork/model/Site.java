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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

import com.prasanna.android.stacknetwork.model.User.UserType;

public class Site implements Serializable
{
    private static final long serialVersionUID = 7057957647981597410L;

    public String name;

    public String apiSiteParameter;

    public String link;

    public int headerResId;

    public String logoUrl;

    public String iconUrl;

    public String faviconUrl;

    public UserType userType = UserType.DOES_NOT_EXIST;
}

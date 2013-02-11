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

import com.prasanna.android.stacknetwork.model.User.UserType;

public class Account implements Serializable
{
    private static final long serialVersionUID = 5454221525646765113L;

    public long id;
    
    public long userId;
    
    public String siteName;
    
    public String siteUrl;
    
    public UserType userType;
    
    public Site site;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((siteUrl == null) ? 0 : siteUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Account other = (Account) obj;
        if (id != other.id)
            return false;
        if (siteUrl == null)
        {
            if (other.siteUrl != null)
                return false;
        }
        else if (!siteUrl.equals(other.siteUrl))
            return false;
        return true;
    }
    
    
}

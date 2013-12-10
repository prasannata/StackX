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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

public class SearchCriteriaDomain implements Serializable {
  private static final long serialVersionUID = 8151064092604079290L;

  public long id = 0L;
  public String name;
  public long created = 0L;
  public long lastModified = 0L;
  public long lastRun = 0L;
  public int runCount = 0;
  public boolean tab = false;
  public String site;
  public SearchCriteria searchCriteria;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SearchCriteriaDomain other = (SearchCriteriaDomain) obj;
    if (id != other.id)
      return false;
    return true;
  }

}

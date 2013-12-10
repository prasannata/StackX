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

package com.prasanna.android.http;

import java.io.Serializable;

public class ServerException extends AbstractHttpException implements Serializable {
  private static final long serialVersionUID = 653718270475335315L;
  private Code code;

  public ServerException(int statusCode) {
    this(statusCode, null, null);
  }

  public ServerException(int statusCode, String statusDescription, String errorResponse) {
    super(statusCode, statusDescription, errorResponse);
  }

  public ServerException(Code code) {
    super();

    this.code = code;
  }

  @Override
  public Code getCode() {
    return code;
  }
}

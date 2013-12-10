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

package com.prasanna.android.http;

public abstract class AbstractHttpException extends RuntimeException implements HttpException {
  private String errorResponse;
  private int statusCode;
  private String statusDescription;

  private static final long serialVersionUID = 7575439728639763037L;

  public AbstractHttpException() {
    super();
  }

  public AbstractHttpException(String message) {
    this(0, null, message);
  }

  public AbstractHttpException(int statusCode) {
    this(statusCode, null);
  }

  public AbstractHttpException(int statusCode, String errorResponse) {
    this(statusCode, null, errorResponse);
  }

  public AbstractHttpException(int statusCode, String statusDescription, String errorResponse) {
    super(errorResponse);

    this.statusCode = statusCode;
    this.statusDescription = statusDescription;
    this.errorResponse = errorResponse;
  }

  public AbstractHttpException(Throwable throwable) {
    super(throwable);
  }

  @Override
  public String getErrorResponse() {
    return errorResponse;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public String getStatusDescription() {
    return statusDescription;
  }

}

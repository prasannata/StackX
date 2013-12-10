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

import java.io.Serializable;

public class ClientException extends AbstractHttpException implements Serializable {
    private static final long serialVersionUID = -6758967927788004257L;

    public enum ClientErrorCode implements Code {
        NO_NETWORK(1, "No network available"),
        INVALID_ENCODING(2, "Invalid encoding"),
        HTTP_REQ_ERROR(3, "Failed to create http request"),
        RESPONSE_PARSE_ERROR(4, "Http response parsing failed");

        private final int statusCode;
        private final String description;

        ClientErrorCode(int statusCode, String description) {
            this.statusCode = statusCode;
            this.description = description;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    private ClientErrorCode code;

    public ClientException(ClientErrorCode code) {
        super();

        this.code = code;
    }

    public ClientException(int statusCode, String statusDescription, String errorResponse) {
        super(statusCode, statusDescription, errorResponse);
    }

    public ClientException(ClientErrorCode code, String errorResponse) {
        super(errorResponse);

        this.code = code;
    }

    public ClientException(ClientErrorCode code, Throwable throwable) {
        super(throwable);

        this.code = code;
    }

    public ClientErrorCode getCode() {
        return code;
    }

    @Override
    public String getErrorResponse() {
        if (code != null)
            return code.getDescription();
        else
            return super.getErrorResponse();
    }

    @Override
    public int getStatusCode() {
        if (code != null)
            return code.getStatusCode();
        else
            return super.getStatusCode();
    }
}

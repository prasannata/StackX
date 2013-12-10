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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.protocol.HttpContext;

import com.prasanna.android.utils.LogWrapper;

public class HttpGzipResponseInterceptor implements HttpResponseInterceptor {
    private final String TAG = HttpGzipResponseInterceptor.class.getSimpleName();
    private final Class<? extends HttpEntityWrapper> entityWrapper;
    private final String contentEncoding;

    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    public HttpGzipResponseInterceptor(final String contentEncoding,
            final Class<? extends HttpEntityWrapper> entityWrapper) {
        this.contentEncoding = contentEncoding;
        this.entityWrapper = entityWrapper;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        Header encodingHeader = entity.getContentEncoding();
        if (encodingHeader != null) {
            HeaderElement[] codecs = encodingHeader.getElements();
            for (int i = 0; i < codecs.length; i++) {
                if (codecs[i].getName().equalsIgnoreCase(contentEncoding)) {
                    try {
                        entityWrapper.getConstructor(HttpEntity.class).newInstance(entity);
                        response.setEntity(new GzipDecompressingEntity(entity));
                        return;
                    }
                    catch (IllegalArgumentException e) {
                        LogWrapper.e(TAG, e.getMessage());
                    }
                    catch (SecurityException e) {
                        LogWrapper.e(TAG, e.getMessage());
                    }
                    catch (InstantiationException e) {
                        LogWrapper.e(TAG, e.getMessage());
                    }
                    catch (IllegalAccessException e) {
                        LogWrapper.e(TAG, e.getMessage());
                    }
                    catch (InvocationTargetException e) {
                        LogWrapper.e(TAG, e.getMessage());
                    }
                    catch (NoSuchMethodException e) {
                        LogWrapper.e(TAG, e.getMessage());
                    }
                }
            }
        }
    }

}

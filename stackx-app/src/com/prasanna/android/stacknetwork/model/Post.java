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

public class Post extends IdentifiableItem implements Serializable {
    private static final long serialVersionUID = -7850382261881073395L;

    public enum PostType {
        QUESTION("question"),
        ANSWER("answer");

        private final String value;

        PostType(String value) {
            this.value = value;
        }

        public static PostType getEnum(String string) {
            PostType postType = null;

            if (string != null) {
                try {
                    postType = valueOf(string.toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    postType = null;
                }

            }
            return postType;
        }

        public String getValue() {
            return value;
        }
    }

    public PostType postType;

    public int score = 0;

    public String title;

    public String body;

    public User owner;
}

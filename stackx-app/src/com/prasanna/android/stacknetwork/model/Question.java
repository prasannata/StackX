/*
    Copyright 2014 Prasanna Thirumalai
    
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

public class Question extends VotablePost implements Serializable {
  private static final long serialVersionUID = -4722553914475051236L;

  public static Question copyMetaDeta(Question that) {
    if (that == null) return null;

    Question question = new Question();
    question.id = that.id;
    question.title = that.title;
    question.score = that.score;
    question.creationDate = that.creationDate;
    question.body = that.body;
    question.link = that.link;
    question.viewCount = that.viewCount;
    question.answerCount = that.answerCount;
    question.relativeLink = that.relativeLink;
    question.answered = that.answered;
    question.upvoted = that.upvoted;
    question.downvoted = that.downvoted;
    question.favorited = that.favorited;
    question.hasAcceptedAnswer = that.hasAcceptedAnswer;
    question.bountyAmount = that.bountyAmount;

    if (that.tags != null) {
      question.tags = new String[that.tags.length];
      for (int i = 0; i < that.tags.length; i++)
        question.tags[i] = that.tags[i];
    }
    question.owner = User.copyShallowUser(that.owner);
    return question;
  }

  public int viewCount;

  public int answerCount;

  public int votes;

  public String[] tags;

  public String relativeLink;

  public ArrayList<Answer> answers;

  public ArrayList<Comment> comments;

  public boolean answered = false;

  public boolean hasAcceptedAnswer = false;

  public boolean favorited = false;

  public final PostType postType = PostType.QUESTION;

  public int bountyAmount = 0;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Answer other = (Answer) obj;
    if (id != other.id) return false;
    return true;
  }
}

/*
    Copyright (C) 2014 Prasanna Thirumalai
    
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

import org.json.JSONArray;
import org.json.JSONException;

import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public class Reputation extends IdentifiableItem implements Serializable {

  private static final long serialVersionUID = 1872029972544683866L;

  public enum ReputationHistoryType {
    asker_accepts_answer("accept"),
    asker_unaccept_answer("unaccept"),
    answer_accepted("unaccept"),
    answer_unaccepted("unaccept"),
    voter_downvotes("downvote"),
    voter_undownvotes("upvote"),
    post_downvoted("downvoted"),
    post_undownvoted("undownvoted"),
    post_upvoted("upvoted"),
    post_unupvoted("unupvoted"),
    suggested_edit_approval_received("edit accept"),
    post_flagged_as_spam("spam"),
    post_flagged_as_offensive("offensive"),
    bounty_given("bounty given"),
    bounty_earned("bounty earned"),
    bounty_cancelled("bounty cancelled"),
    post_deleted("delete"),
    post_undeleted("undelete"),
    association_bonus("association bonus"),
    arbitrary_reputation_change("rep change"),
    vote_fraud_reversal("vote fraud reverse"),
    post_migrated("post migrate"),
    user_deleted("user delete");

    private final String displayText;

    private ReputationHistoryType(final String displayText) {
      this.displayText = displayText;
    }

    public static ReputationHistoryType getEnum(final String value) {
      try {
        return valueOf(value);
      } catch (Exception e) {
        return null;
      }
    }

    public String getDisplayText() {
      return displayText;
    }
  }

  public long postId;

  public int reputationChange;

  public ReputationHistoryType reputationHistoryType;

  public long userId;

  public String postTitle;
  
  private static Reputation parseReputation(final JSONObjectWrapper json) {
    final Reputation reputation = new Reputation();
    reputation.postId = json.getLong(JsonFields.Reputation.POST_ID);
    reputation.creationDate = json.getLong(JsonFields.Reputation.CREATION_DATE);
    reputation.reputationChange = json.getInt(JsonFields.Reputation.REPUTATION_CHANGE);
    reputation.userId = json.getLong(JsonFields.Reputation.USER_ID);
    reputation.reputationHistoryType =
        ReputationHistoryType.valueOf(json.getString(JsonFields.Reputation.REPUTATION_HISTORY_TYPE));    
    return reputation;
  }

  public static ArrayList<Reputation> parseCollection(final JSONObjectWrapper json) {
    final ArrayList<Reputation> reputationHistory = new ArrayList<Reputation>();

    if (json != null) {
      JSONArray items = json.getJSONArray(JsonFields.ITEMS);
      if (items != null) {
        for (int idx = 0; idx < items.length(); idx++) {
          try {
            Reputation reputation = parseReputation(JSONObjectWrapper.wrap(items.getJSONObject(idx)));
            if (reputation.reputationChange != 0) reputationHistory.add(reputation);
          } catch (JSONException e) {
          }
        }
      }
    }

    return reputationHistory;
  }
}

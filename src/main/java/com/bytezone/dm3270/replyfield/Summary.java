package com.bytezone.dm3270.replyfield;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Summary extends QueryReplyField {

  public Summary(byte[] buffer) {
    super(buffer);
    assert data[1] == SUMMARY_QUERY_REPLY;
  }

  public Summary(List<QueryReplyField> replyFields) {
    super(SUMMARY_QUERY_REPLY);
    replies = replyFields;
    ByteBuffer buffer = createReplyBuffer(replyFields.size() + 1);
    buffer.put(SUMMARY_QUERY_REPLY);
    replyFields.forEach(r -> buffer.put(r.replyType.type));
  }

  protected boolean isListed(byte type) {
    for (int i = 2; i < data.length; i++) {
      if (data[i] == type) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder(super.toString());

    for (int i = 2; i < data.length; i++) {
      text.append(String.format("%n  %-30s  %s", ReplyType.fromId(data[i]),
          isProvided(data[i]) ? "" : "** missing **"));
    }

    // check for QueryReplyFields sent but not listed in the summary
    List<QueryReplyField> missingFields = new ArrayList<>(4);
    for (QueryReplyField reply : replies) {
      if (!isListed(reply.replyType.type)) {
        missingFields.add(reply);
      }
    }

    if (missingFields.size() > 0) {
      text.append("\n\nNot listed in Summary:");
      for (QueryReplyField qrf : missingFields) {
        text.append(String.format("%n  %s", qrf.replyType));
      }
    }

    return text.toString();
  }

}

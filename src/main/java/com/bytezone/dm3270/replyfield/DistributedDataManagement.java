package com.bytezone.dm3270.replyfield;

import java.nio.ByteBuffer;

public class DistributedDataManagement extends QueryReplyField {

  private final short flags;
  private final short maximumInboundBytes;
  private final short maximumOutboundBytes;
  private final byte supportedSubsetsCount;
  private final byte ddmSubsetId;

  public DistributedDataManagement(byte[] buffer) {
    super(buffer);
    ByteBuffer dataBuffer = ByteBuffer.wrap(buffer);
    //skip queryReply id
    dataBuffer.get();
    assert dataBuffer.get() == DISTRIBUTED_DATA_MANAGEMENT_REPLY;
    flags = dataBuffer.getShort();
    maximumInboundBytes = dataBuffer.getShort();
    maximumOutboundBytes = dataBuffer.getShort();
    supportedSubsetsCount = dataBuffer.get();
    ddmSubsetId = dataBuffer.get();
  }

  public DistributedDataManagement() {
    super(DISTRIBUTED_DATA_MANAGEMENT_REPLY);
    flags = 0;
    maximumInboundBytes = 8192;
    maximumOutboundBytes = 8192;
    supportedSubsetsCount = 1;
    ddmSubsetId = 1;
    ByteBuffer buffer = createReplyBuffer(8);
    buffer.putShort(flags);
    buffer.putShort(maximumInboundBytes);
    buffer.putShort(maximumOutboundBytes);
    buffer.put(supportedSubsetsCount);
    buffer.put(ddmSubsetId);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  flags      : %04X", flags)
        + String.format("%n  limit in   : %d", maximumInboundBytes)
        + String.format("%n  limit out  : %d", maximumOutboundBytes)
        + String.format("%n  subsets    : %d", supportedSubsetsCount)
        + String.format("%n  DDMSS      : %d", ddmSubsetId);
  }

}

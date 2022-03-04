package com.bytezone.dm3270.replyfield;

import java.nio.ByteBuffer;

public class AuxiliaryDevices extends QueryReplyField {

  private final short flags;

  public AuxiliaryDevices(byte[] buffer) {
    super(buffer);
    ByteBuffer dataBuffer = ByteBuffer.wrap(buffer);
    //skip queryReply id
    dataBuffer.get();
    assert dataBuffer.get() == AUXILIARY_DEVICE_REPLY;
    flags = dataBuffer.getShort();
  }

  public AuxiliaryDevices() {
    super(AUXILIARY_DEVICE_REPLY);
    flags = 0;
    ByteBuffer buffer = createReplyBuffer(2);
    buffer.putShort(flags);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  flags     : %02X", flags);
  }

}

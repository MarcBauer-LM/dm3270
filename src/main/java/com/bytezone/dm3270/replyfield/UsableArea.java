package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.display.ScreenDimensions;
import java.nio.ByteBuffer;

public class UsableArea extends QueryReplyField {

  private static final int HARD_COPY_FLAG_MASK = 0x01;
  private static final int PAGE_PRINTER_FLAG_MASK = 0x04;

  private final AddressingMode addressingMode;
  private final boolean hardCopy;
  private final boolean pagePrinter;
  private final short width;
  private final short height;
  private final MeasurementUnit measurementUnit;
  private final short xPointsDistanceNumerator;
  private final short xPointsDistanceDenominator;
  private final short yPointsDistanceNumerator;
  private final short yPointsDistanceDenominator;
  private final byte defaultCellXUnits;
  private final byte defaultCellYUnits;
  private final short bufferSize;

  private static class MeasurementUnit {

    private static final MeasurementUnit INCHES = new MeasurementUnit(0, "Inches");
    private static final MeasurementUnit MILLIMETRES = new MeasurementUnit(1, "Millimetres");
    private static final MeasurementUnit[] UNITS = {INCHES, MILLIMETRES};

    private final byte id;
    private final String name;

    MeasurementUnit(int id, String name) {
      this.id = (byte) id;
      this.name = name;
    }

    static MeasurementUnit fromByte(byte val) {
      return (val < UNITS.length) ? UNITS[val] : new MeasurementUnit(val, "Unknown");
    }

  }

  private static class AddressingMode {

    private static final String RESERVED = "Reserved";
    private static final AddressingMode ADDRESSING_12_14_BIT = new AddressingMode(1, "12/14 bit");
    private static final AddressingMode ADDRESSING_14_14_16_BIT = new AddressingMode(3,
        "12/14/16 bit");
    private static final AddressingMode[] MODES = {new AddressingMode(0, RESERVED),
        ADDRESSING_12_14_BIT, new AddressingMode(1, RESERVED), ADDRESSING_14_14_16_BIT};

    private final byte id;
    private final String name;

    private AddressingMode(int id, String name) {
      this.id = (byte) id;
      this.name = name;
    }

    static AddressingMode fromByte(byte b) {
      int val = b & 0x0F;
      return (val < MODES.length) ? MODES[val] : new AddressingMode(val, "Unknown");
    }

  }

  public UsableArea(int rows, int columns) {
    super(USABLE_AREA_REPLY);
    addressingMode = AddressingMode.ADDRESSING_12_14_BIT;
    hardCopy = false;
    pagePrinter = false;
    width = (short) columns;
    height = (short) rows;
    measurementUnit = MeasurementUnit.MILLIMETRES;
    xPointsDistanceNumerator = 211;
    xPointsDistanceDenominator = 800;
    yPointsDistanceNumerator = 158;
    yPointsDistanceDenominator = 600;
    defaultCellXUnits = 7;
    defaultCellYUnits = 12;
    bufferSize = (short) (rows * columns);
    ByteBuffer buffer = createReplyBuffer(19);
    byte flags1 = flagsToByte();
    buffer.put(flags1);
    byte flags2 = 0;
    buffer.put(flags2);
    buffer.putShort(width);
    buffer.putShort(height);
    buffer.put(measurementUnit.id);
    buffer.putShort(xPointsDistanceNumerator);
    buffer.putShort(xPointsDistanceDenominator);
    buffer.putShort(yPointsDistanceNumerator);
    buffer.putShort(yPointsDistanceDenominator);
    buffer.put(defaultCellXUnits);
    buffer.put(defaultCellYUnits);
    buffer.putShort(bufferSize);
  }

  public UsableArea(byte[] buffer) {
    super(buffer);
    ByteBuffer dataBuffer = ByteBuffer.wrap(buffer);
    //skip queryReply id
    dataBuffer.get();
    assert dataBuffer.get() == USABLE_AREA_REPLY;
    byte flags1 = dataBuffer.get();
    addressingMode = AddressingMode.fromByte((byte) (flags1 & 0x0F));
    hardCopy = (flags1 & HARD_COPY_FLAG_MASK) != 0;
    pagePrinter = (flags1 & PAGE_PRINTER_FLAG_MASK) != 0;
    //flags 2 currently being ignored
    dataBuffer.get();
    width = dataBuffer.getShort();
    height = dataBuffer.getShort();
    measurementUnit = MeasurementUnit.fromByte(dataBuffer.get());
    xPointsDistanceNumerator = dataBuffer.getShort();
    xPointsDistanceDenominator = dataBuffer.getShort();
    yPointsDistanceNumerator = dataBuffer.getShort();
    yPointsDistanceDenominator = dataBuffer.getShort();
    defaultCellXUnits = dataBuffer.get();
    defaultCellYUnits = dataBuffer.get();
    bufferSize = dataBuffer.getShort();
  }

  private byte flagsToByte() {
    byte flags = addressingMode.id;
    if (hardCopy) {
      flags |= HARD_COPY_FLAG_MASK;
    }
    if (pagePrinter) {
      flags |= PAGE_PRINTER_FLAG_MASK;
    }
    return flags;
  }

  public ScreenDimensions getScreenDimensions() {
    return new ScreenDimensions(height, width);
  }

  @Override
  public String toString() {
    return super.toString()
        + String.format("%n  flags     : %02X", flagsToByte())
        + String.format("%n  ad mode    : %d - %s", addressingMode.id, addressingMode.name)
        + String.format("%n  width      : %d", width)
        + String.format("%n  height     : %d", height)
        + String.format("%n  units      : %d - %s", measurementUnit.id, measurementUnit.name)
        + String
        .format("%n  x ratio    : %d / %d", xPointsDistanceNumerator, xPointsDistanceDenominator)
        + String
        .format("%n  y ratio    : %d / %d", yPointsDistanceNumerator, yPointsDistanceDenominator)
        + String.format("%n  x units    : %d", defaultCellXUnits)
        + String.format("%n  y units    : %d", defaultCellYUnits)
        + String.format("%n  buffer     : %d", bufferSize);
  }

}

package com.bytezone.dm3270.display;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import java.util.ArrayList;
import java.util.List;

public final class ScreenPosition {

  private final int position;

  private StartFieldAttribute startFieldAttribute;
  private final List<Attribute> attributes = new ArrayList<>();

  private byte value;
  private ScreenContext screenContext;
  private final Charset charset;

  public ScreenPosition(int position, ScreenContext screenContext,
      Charset charset) {
    this.position = position;
    this.screenContext = screenContext;
    this.charset = charset;
    reset();
  }

  public void reset() {
    value = 0;
    screenContext = screenContext.withGraphic(false);
    startFieldAttribute = null;
    attributes.clear();
  }

  public void setChar(byte value) {
    this.value = value;
    screenContext = screenContext.withGraphic(false);
  }

  public void setAplGraphicChar(byte value) {
    this.value = value;
    screenContext = screenContext.withGraphic(true);
  }

  public StartFieldAttribute getStartFieldAttribute() {
    return startFieldAttribute;
  }

  public void setStartField(StartFieldAttribute startFieldAttribute) {
    if (startFieldAttribute == null) {
      if (this.startFieldAttribute != null) {
        attributes.clear();
      }
    }
    this.startFieldAttribute = startFieldAttribute;
  }

  public void addAttribute(Attribute attribute) {
    attributes.add(attribute);
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public int getPosition() {
    return position;
  }

  // All the colour and highlight options
  public void setScreenContext(ScreenContext screenContext) {
    if (screenContext == null) {
      throw new IllegalArgumentException("ScreenContext cannot be null");
    }
    this.screenContext = screenContext;
  }

  public ScreenContext getScreenContext() {
    return screenContext;
  }

  public boolean isStartField() {
    return startFieldAttribute != null;
  }

  public boolean isGraphic() {
    return screenContext.isGraphic();
  }

  public char getChar() {
    if (value == 0) {
      return '\u0000';
    }
    if ((value & 0xC0) == 0) {
      return ' ';
    }

    if (screenContext.isGraphic()) {
      return convertGraphicChar(value);
    }

    return charset.getChar(value);
  }

  private static char convertGraphicChar(byte val) {
    switch (val) {
      case (byte) 0x85:
        return '│';
      case (byte) 0xA2:
        return '─';
      case (byte) 0xC4:
        return '└';
      case (byte) 0xC5:
        return '┌';
      case (byte) 0xC6:
        return '├';
      case (byte) 0xC7:
        return '┴';
      case (byte) 0xD3:
        return '┼';
      case (byte) 0xD4:
        return '┘';
      case (byte) 0xD5:
        return '┐';
      case (byte) 0xD6:
        return '┤';
      case (byte) 0xD7:
        return '┬';
      default:
        return ' ';
    }
  }

  public String getCharString() {
    if (isStartField()) {
      return " ";
    }

    if (screenContext.isGraphic()) {
      return String.valueOf(convertGraphicChar(value));
    }

    char ret = charset.getChar(value);
    return ret < ' ' ? " " : String.valueOf(ret);
  }

  public byte getByte() {
    return value;
  }

  public boolean isNull() {
    return value == 0;
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    if (isStartField()) {
      text.append("..").append(startFieldAttribute);
    } else {
      for (Attribute attribute : attributes) {
        text.append("--").append(attribute);
      }
    }

    text.append(", byte: ").append(getCharString());

    return text.toString();
  }

}

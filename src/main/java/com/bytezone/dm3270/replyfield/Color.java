package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.attributes.ColorAttribute;
import java.nio.ByteBuffer;

public class Color extends QueryReplyField {

  private static final byte[] COLORS_ARRAY = {
      ColorAttribute.COLOR_NEUTRAL1, ColorAttribute.COLOR_GREEN,
      ColorAttribute.COLOR_BLUE, ColorAttribute.COLOR_BLUE,
      ColorAttribute.COLOR_RED, ColorAttribute.COLOR_RED,
      ColorAttribute.COLOR_PINK, ColorAttribute.COLOR_PINK,
      ColorAttribute.COLOR_GREEN, ColorAttribute.COLOR_GREEN,
      ColorAttribute.COLOR_TURQUOISE, ColorAttribute.COLOR_TURQUOISE,
      ColorAttribute.COLOR_YELLOW, ColorAttribute.COLOR_YELLOW,
      ColorAttribute.COLOR_NEUTRAL2, ColorAttribute.COLOR_NEUTRAL2,
      ColorAttribute.COLOR_BLACK, ColorAttribute.COLOR_BLACK,
      ColorAttribute.COLOR_DEEP_BLUE, ColorAttribute.COLOR_DEEP_BLUE,
      ColorAttribute.COLOR_ORANGE, ColorAttribute.COLOR_ORANGE,
      ColorAttribute.COLOR_PURPLE, ColorAttribute.COLOR_PURPLE,
      ColorAttribute.COLOR_PALE_GREEN, ColorAttribute.COLOR_PALE_GREEN,
      ColorAttribute.COLOR_PALE_TURQUOISE, ColorAttribute.COLOR_PALE_TURQUOISE,
      ColorAttribute.COLOR_GREY, ColorAttribute.COLOR_GREY,
      ColorAttribute.COLOR_WHITE, ColorAttribute.COLOR_WHITE
  };

  private final byte flags;
  private final byte[] colors;

  public Color() {
    super(COLOR_QUERY_REPLY);
    flags = 0;
    colors = COLORS_ARRAY;
    ByteBuffer buffer = createReplyBuffer(
        colors.length + 1 + 1);  // adding flags and colors length bytes
    buffer.put(flags);
    buffer.put((byte) (colors.length / 2));
    buffer.put(colors, 0, colors.length);
  }

  public Color(byte[] buffer) {
    super(buffer);
    ByteBuffer dataBuffer = ByteBuffer.wrap(buffer);
    //skip queryReply id
    dataBuffer.get();
    assert dataBuffer.get() == COLOR_QUERY_REPLY;
    flags = dataBuffer.get();
    byte colorsCount = dataBuffer.get();
    colors = new byte[colorsCount * 2];
    dataBuffer.get(colors, 0, colors.length);
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder(super.toString());
    text.append(String.format("%n  flags      : %02X", flags));
    text.append(String.format("%n  pairs      : %d", colors.length / 2));
    for (int i = 0; i < colors.length; i += 2) {
      text.append(String.format("%n  val/actn   : %02X/%02X - %s", colors[i],
          colors[i + 1], ColorAttribute.colorName(colors[i])));
      if (colors[i] != colors[i + 1]) {
        text.append("/").append(ColorAttribute.colorName(colors[i + 1]));
      }
    }
    return text.toString();
  }

}

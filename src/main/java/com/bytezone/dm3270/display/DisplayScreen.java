package com.bytezone.dm3270.display;

import com.bytezone.dm3270.display.Screen.ScreenOption;

public interface DisplayScreen {

  Pen getPen();

  ScreenDimensions getScreenDimensions();

  ScreenPosition getScreenPosition(int position);

  int validate(int position);

  void clearScreen(ScreenOption newScreen);

  void insertCursor(int position);

}

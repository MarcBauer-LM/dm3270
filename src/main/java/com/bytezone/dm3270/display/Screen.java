package com.bytezone.dm3270.display;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.structuredfields.SetReplyModeSF;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Screen implements DisplayScreen {

  private static final Logger LOG = LoggerFactory.getLogger(Screen.class);

  private static final byte[] SAVE_SCREEN_REPLY_TYPES =
      {Attribute.XA_HIGHLIGHTING, Attribute.XA_FGCOLOR, Attribute.XA_CHARSET,
          Attribute.XA_BGCOLOR, Attribute.XA_TRANSPARENCY};

  private ScreenPosition[] screenPositions;
  private final FieldManager fieldManager;
  private ScreenPacker screenPacker;

  private final TelnetState telnetState;
  private final Charset charset;

  private final ScreenDimensions defaultScreenDimensions;
  private final ScreenDimensions alternateScreenDimensions;

  private Pen pen;
  private final Cursor cursor;
  private ScreenOption currentScreen;
  private boolean alarmSounded;
  private boolean sscpLuData;

  private byte currentAID;
  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  private int insertedCursorPosition = -1;
  private boolean keyboardLocked;
  private boolean insertMode;
  private boolean readModifiedAll = false;

  private final Set<KeyboardStatusListener> keyboardChangeListeners = ConcurrentHashMap.newKeySet();

  public enum ScreenOption {
    DEFAULT, ALTERNATE
  }

  public Screen(ScreenDimensions defaultScreenDimensions,
      ScreenDimensions alternateScreenDimensions, TelnetState telnetState, Charset charset) {
    this.defaultScreenDimensions = defaultScreenDimensions;
    this.alternateScreenDimensions = alternateScreenDimensions;
    this.telnetState = telnetState;
    this.charset = charset;
    ScreenOption currentOption = alternateScreenDimensions == null ? ScreenOption.DEFAULT
        : ScreenOption.ALTERNATE;

    ScreenDimensions screenDimensions = currentOption.equals(ScreenOption.DEFAULT)
        ? defaultScreenDimensions : alternateScreenDimensions;

    cursor = new Cursor(this);

    fieldManager = new FieldManager(this, screenDimensions);

    screenPositions = new ScreenPosition[screenDimensions.size];
    pen = Pen.getInstance(screenPositions, screenDimensions, charset);

    screenPacker = new ScreenPacker(pen, fieldManager, charset);

    setCurrentScreen(currentOption);
  }

  public TelnetState getTelnetState() {
    return telnetState;
  }

  public void setCurrentScreen(ScreenOption value) {
    if (currentScreen == value) {
      return;
    }

    currentScreen = value;
    ScreenDimensions screenDimensions = getScreenDimensions();

    pen.setScreenDimensions(screenDimensions);
    fieldManager.setScreenDimensions(screenDimensions);

    BufferAddress.setScreenWidth(screenDimensions.columns);
  }

  public ScreenOption getCurrentScreenOption() {
    return currentScreen;
  }

  @Override
  public ScreenDimensions getScreenDimensions() {
    return currentScreen == ScreenOption.DEFAULT ? defaultScreenDimensions
        : alternateScreenDimensions;
  }

  public void setConsolePane(ConsolePane consolePane) {
    addKeyboardStatusChangeListener(consolePane);
  }

  public FieldManager getFieldManager() {
    return fieldManager;
  }

  public Cursor getScreenCursor() {
    return cursor;
  }

  public void resetInsertMode() {
    if (insertMode) {
      toggleInsertMode();
    }
  }

  public void toggleInsertMode() {
    insertMode = !insertMode;
    fireKeyboardStatusChange("");
  }

  public boolean isInsertMode() {
    return insertMode;
  }

  public void setSscpLuData() {
    sscpLuData = true;
  }

  public boolean isSscpLuData() {
    return sscpLuData;
  }

  public Charset getCharset() {
    return charset;
  }

  public void eraseAllUnprotected() {
    Optional<Field> firstUnprotectedField = fieldManager.eraseAllUnprotected();

    restoreKeyboard();         // resets the AID to NO_AID_SPECIFIED
    resetModified();
    draw();

    firstUnprotectedField
        .ifPresent(screenPositions1 -> cursor.moveTo(screenPositions1.getFirstLocation()));
  }

  public void buildFields() {
    fieldManager.buildFields(screenPositions);        // what about resetModified?
  }

  public void checkRecording() {
    byte savedReplyMode = replyMode;
    byte[] savedReplyTypes = replyTypes;
    setReplyMode(SetReplyModeSF.RM_CHARACTER, SAVE_SCREEN_REPLY_TYPES);
    setReplyMode(savedReplyMode, savedReplyTypes);
  }

  public void draw() {
    if (insertedCursorPosition >= 0) {
      cursor.moveTo(insertedCursorPosition);
      insertedCursorPosition = -1;
      cursor.setVisible(true);
    }
  }

  public void setAID(byte aid) {
    currentAID = aid;
  }

  public void setReplyMode(byte replyMode, byte[] replyTypes) {
    this.replyMode = replyMode;
    this.replyTypes = replyTypes;
  }

  public void setFieldText(Field field, String text) {
    field.setText(getTextBytes(text));
    field.setModified(true);
  }

  private byte[] getTextBytes(String text) {
    try {
      return text.getBytes(charset.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void setPositionText(int position, String text) {
    byte[] bytes = getTextBytes(text);
    for (int i = 0; i < bytes.length && position + i < screenPositions.length; i++) {
      screenPositions[position + i].setChar(bytes[i]);
    }
  }

  // ---------------------------------------------------------------------------------//
  // DisplayScreen interface methods
  // ---------------------------------------------------------------------------------//

  @Override
  public Pen getPen() {
    return pen;
  }

  @Override
  public ScreenPosition getScreenPosition(int position) {
    return screenPositions[position];
  }

  @Override
  public int validate(int position) {
    return pen.validate(position);
  }

  @Override
  public void clearScreen(ScreenOption requestedScreenOption) {
    if (!requestedScreenOption.equals(currentScreen)) {
      ScreenDimensions size = requestedScreenOption.equals(ScreenOption.DEFAULT)
          ? defaultScreenDimensions
          : alternateScreenDimensions;
      screenPositions = new ScreenPosition[size.size];
      pen = Pen.getInstance(screenPositions, size, charset);

      screenPacker = new ScreenPacker(pen, fieldManager, charset);
      currentScreen = requestedScreenOption;
      sscpLuData = false;
      fieldManager.reset();
      return;
    }
    cursor.moveTo(0);
    pen.clearScreen();
    sscpLuData = false;
    fieldManager.reset();
  }

  @Override
  public void insertCursor(int position) {
    insertedCursorPosition = position;                // move it here later
  }

  // ---------------------------------------------------------------------------------//
  // Convert screen contents to an AID command
  // ---------------------------------------------------------------------------------//

  public Command readModifiedFields() {
    return screenPacker.readModifiedFields(currentAID, getScreenCursor().getLocation(),
        readModifiedAll, sscpLuData);
  }

  public Command readModifiedFields(byte type) {
    switch (type) {
      case Command.READ_MODIFIED_F6:
        return readModifiedFields();

      case Command.READ_MODIFIED_ALL_6E:
        readModifiedAll = true;
        Command command = readModifiedFields();
        readModifiedAll = false;
        return command;

      default:
        LOG.warn("Unknown type {}", type);
    }

    return null;
  }

  public AIDCommand readBuffer() {
    return screenPacker.readBuffer(currentAID, getScreenCursor().getLocation(),
        replyMode, replyTypes);
  }

  // ---------------------------------------------------------------------------------//
  // Events to be processed from WriteControlCharacter.process()
  // ---------------------------------------------------------------------------------//

  public boolean isAlarmOn() {
    return alarmSounded;
  }

  public void soundAlarm() {
    alarmSounded = true;
  }

  public boolean resetAlarm() {
    boolean sounded = alarmSounded;
    alarmSounded = false;
    return sounded;
  }

  public void restoreKeyboard() {
    setAID(AIDCommand.NO_AID_SPECIFIED);
    cursor.setVisible(true);
    keyboardLocked = false;
    fireKeyboardStatusChange("");
  }

  public void lockKeyboard(String keyName) {
    keyboardLocked = true;
    fireKeyboardStatusChange(keyName);
    cursor.setVisible(false);
  }

  public void resetModified() {
    fieldManager.getUnprotectedFields().forEach(f -> f.setModified(false));
  }

  public boolean isKeyboardLocked() {
    return keyboardLocked;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private void fireKeyboardStatusChange(String keyName) {
    KeyboardStatusChangedEvent evt =
        new KeyboardStatusChangedEvent(insertMode, keyboardLocked, keyName);
    keyboardChangeListeners.forEach(l -> l.keyboardStatusChanged(evt));
  }

  public void addKeyboardStatusChangeListener(KeyboardStatusListener listener) {
    keyboardChangeListeners.add(listener);
  }

  public void removeKeyboardStatusChangeListener(KeyboardStatusListener listener) {
    keyboardChangeListeners.remove(listener);
  }

}

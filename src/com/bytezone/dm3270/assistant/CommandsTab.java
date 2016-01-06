package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.TSOCommandListener;
import com.bytezone.dm3270.utilities.Site;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class CommandsTab extends AbstractTransferTab
    implements TSOCommandListener, KeyboardStatusListener, ScreenChangeListener
{
  ObservableList<String> commands = FXCollections.observableArrayList ();
  ListView<String> commandList = new ListView<> (commands);

  public CommandsTab (Screen screen, Site site, TSOCommand tsoCommand)
  {
    super ("Commands", screen, site, tsoCommand);

    commandList.setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setContent (commandList);
    commandList.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> setText ());
  }

  @Override
  public void tsoCommand (String command)
  {
    if (command.startsWith ("="))
      return;

    if (screenWatcher.isTSOCommandScreen () || command.toUpperCase ().startsWith ("TSO "))
      if (!commands.contains (command))
        commands.add (command);

    if (isSelected ())
      setText ();
  }

  @Override
  protected void setText ()
  {
    String selectedCommand = commandList.getSelectionModel ().getSelectedItem ();
    if (selectedCommand == null)
    {
      eraseCommand ();
      return;
    }

    tsoCommand.txtCommand.setText (selectedCommand);
    setButton ();
  }
}
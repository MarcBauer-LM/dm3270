package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.replyfield.QueryReplyField.ReplyType;
import com.bytezone.dm3270.structuredfields.StructuredField;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPartitionQuery extends Command {

  private static final Logger LOG = LoggerFactory.getLogger(ReadPartitionQuery.class);
  private final Charset charset;

  private String typeName;

  public ReadPartitionQuery(byte[] buffer, int offset, int length, Charset charset) {
    super(buffer, offset, length);
    this.charset = charset;

    assert data[0] == StructuredField.READ_PARTITION;
    assert data[1] == (byte) 0xFF;
  }

  @Override
  public void process(Screen screen) {
    if (getReply().isPresent()) {
      return;
    }

    switch (data[2]) {
      case (byte) 0x02:
        setReply(new ReadStructuredFieldCommand(screen.getTelnetState(), charset));
        typeName = "Read Partition (Query)";
        break;

      case (byte) 0x03:
        switch (data[3] & 0xFF) {
          case 0x00:
            LOG.warn("QCODE list not yet supported");
            break;

          case 0x40:
            typeName = "Read Partition (QueryList): Equivalent + QCODE list";
            List<ReplyType> queryList = new ArrayList<>();
            for (int i = 4; i < data.length; i++) {
              queryList.add(ReplyType.fromId(data[i]));
            }
            setReply(new ReadStructuredFieldCommand(queryList, screen.getTelnetState(), charset));
            break;

          case 0x80:
            typeName = "Read Partition (QueryList): all";
            setReply(new ReadStructuredFieldCommand(screen.getTelnetState(), charset));
            break;

          default:
            LOG.warn("Unknown query type: {}", String.format("%02X", data[3]));
        }
        break;

      default:
        LOG.warn("Unknown ReadStructuredField type: {}", String.format("%02X", data[2]));
    }
  }

  @Override
  public String getName() {
    return typeName;
  }

  @Override
  public String toString() {
    return String.format("%s", typeName);
  }

}

package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.attributes.ColorAttribute;

public class ContextHandler
{
  private final List<ScreenContext> contextPool = new ArrayList<> ();

  public ContextHandler ()
  {
    ScreenContext base =
        new ScreenContext (ColorAttribute.colors[0], ColorAttribute.colors[8], (byte) 0,
            false);
    contextPool.add (base);
  }

  public ScreenContext getBase ()
  {
    return contextPool.get (0);
  }

  public ScreenContext setForeground (ScreenContext oldContext, Color color)
  {
    for (ScreenContext sc : contextPool)
      if (sc.foregroundColor == color //
          && sc.backgroundColor == oldContext.backgroundColor
          && sc.highlight == oldContext.highlight
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (color, oldContext.backgroundColor, oldContext.highlight,
            oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setBackground (ScreenContext oldContext, Color color)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == color //
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.highlight == oldContext.highlight
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, color, oldContext.highlight,
            oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setHighlight (ScreenContext oldContext, byte highlight)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.highlight == highlight //
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, oldContext.backgroundColor,
            highlight, oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setHighIntensity (ScreenContext oldContext, boolean highIntensity)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.highlight == oldContext.highlight //
          && sc.highIntensity == highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, oldContext.backgroundColor,
            oldContext.highlight, highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext resetAttribute (ScreenContext oldContext)
  {
    return getBase ();
  }
}
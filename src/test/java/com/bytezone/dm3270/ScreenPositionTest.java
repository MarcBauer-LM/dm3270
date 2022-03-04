package com.bytezone.dm3270;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.ScreenContext;
import com.bytezone.dm3270.display.ScreenPosition;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

@RunWith(MockitoJUnitRunner.class)
public class ScreenPositionTest {

  private ScreenPosition screenPosition;
  private static final byte VALUE = 0x01;

  @Before
  public void setup() {
    screenPosition = new ScreenPosition(0, ScreenContext.DEFAULT_CONTEXT, Charset.CP1147);
  }

  @Test
  public void shouldResetCharSizeWhenReset() {
    screenPosition.setChar(VALUE);
    screenPosition.reset();
    assertThat(screenPosition.getByte()).isEqualTo((byte) 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenNullContext() {
    screenPosition.setScreenContext(null);
  }

  @Test
  public void shouldClearAttributesWhenNullStartField() {
    StartFieldAttribute startFieldAttribute = new StartFieldAttribute(VALUE);
    screenPosition.setStartField(startFieldAttribute);
    screenPosition
        .addAttribute(Attribute.getAttribute(Attribute.XA_START_FIELD, VALUE).orElse(null));
    screenPosition.addAttribute(Attribute.getAttribute(Attribute.XA_BGCOLOR, VALUE).orElse(null));
    screenPosition.setStartField(null);
    assertThat(screenPosition.getAttributes()).isEmpty();
  }

  @RunWith(Parameterized.class)
  public static class CharConversionTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ScreenContext screenContextMock;

    @Parameter()
    public byte value;
    @Parameter(1)
    public char expectedChar;
    @Parameter(2)
    public boolean isAplCharset;

    private ScreenPosition screenPosition;

    @Before
    public void setup() {
      Charset.CP1047.load();
      screenPosition = new ScreenPosition(0, screenContextMock, Charset.CP1047);
      when(screenContextMock.isGraphic()).thenReturn(isAplCharset);
    }

    @Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][]{
          {(byte) 0x85, '│', true},
          {(byte) 0xA2, '─', true},
          {(byte) 0xC4, '└', true},
          {(byte) 0xC5, '┌', true},
          {(byte) 0xC6, '├', true},
          {(byte) 0xC7, '┴', true},
          {(byte) 0xD3, '┼', true},
          {(byte) 0xD4, '┘', true},
          {(byte) 0xD5, '┐', true},
          {(byte) 0xD6, '┤', true},
          {(byte) 0xD7, '┬', true},
          {(byte) 0x85, 'e', false},
          {(byte) 0XA2, 's', false},
          {(byte) 0xC5, 'E', false},
          {(byte) 0xC7, 'G', false}
      });
    }

    @Test
    public void shouldConvertToCharWhenGetChar() {
      screenPosition.setChar(value);
      assertThat(screenPosition.getChar()).isEqualTo(expectedChar);
    }
  }
}

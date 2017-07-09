/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import nl.xs4all.home.freekdb.b52reader.browsers.EmbeddedBrowserType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link EmbeddedBrowserType} enum.
 */
public class EmbeddedBrowserTypeTest {
    @Test
    public void testEnumSizeAndOneValue() {
        assertEquals(2, EmbeddedBrowserType.values().length);

        EmbeddedBrowserType embeddedBrowserType = EmbeddedBrowserType.valueOf("EMBEDDED_BROWSER_DJ_NATIVE_SWING");
        assertEquals(EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING, embeddedBrowserType);
    }
}

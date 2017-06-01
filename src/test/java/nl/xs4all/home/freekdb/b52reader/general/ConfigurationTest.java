/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link Configuration} class.
 */
public class ConfigurationTest {
    @Test
    public void testPrivateConstructor() throws ReflectiveOperationException {
        Constructor<Configuration> constructor = Configuration.class.getDeclaredConstructor();

        assertFalse(constructor.isAccessible());

        constructor.setAccessible(true);
        Configuration instance = constructor.newInstance();

        assertNotNull(instance);
        assertEquals(Configuration.class, instance.getClass());
    }

    @Test
    public void testInitialize() throws UnsupportedEncodingException {
        String configurationLines = "source-ids = test";
        Configuration.initialize(new ByteArrayInputStream(configurationLines.getBytes("UTF-8")));

        assertEquals(new ArrayList<>(), Configuration.getSelectedArticleSources());
        assertEquals(Frame.NORMAL, Configuration.getFrameExtendedState());
        assertNull(Configuration.getFrameBounds());

        assertTrue(Configuration.useSpanTable());
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.lang.reflect.Constructor;

import javax.swing.JPanel;

import nl.xs4all.home.freekdb.b52reader.browsers.BackgroundBrowsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the {@link ObjectHub} class.
 */
public class ObjectHubTest {
    @Test
    public void testPrivateConstructor() throws ReflectiveOperationException {
        Constructor<ObjectHub> constructor = ObjectHub.class.getDeclaredConstructor();

        assertFalse(constructor.isAccessible());

        constructor.setAccessible(true);
        ObjectHub instance = constructor.newInstance();

        assertNotNull(instance);
        assertEquals(ObjectHub.class, instance.getClass());
    }

    @Test
    public void testBackgroundBrowsers() {
        ObjectHub.injectBackgroundBrowsersPanel(new JPanel());

        // The first call should result in a new BackgroundBrowsers object.
        BackgroundBrowsers firstBackgroundBrowsers = ObjectHub.getBackgroundBrowsers();
        assertNotNull(firstBackgroundBrowsers);

        // The subsequent calls should return the same BackgroundBrowsers object.
        BackgroundBrowsers subsequentBackgroundBrowsers = ObjectHub.getBackgroundBrowsers();
        assertNotNull(subsequentBackgroundBrowsers);

        assertEquals(firstBackgroundBrowsers, subsequentBackgroundBrowsers);
    }
}

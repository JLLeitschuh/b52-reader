/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.nrc.NrcScienceArticleSource;

import org.junit.Test;
import org.mockito.Mockito;

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
    public void testInitializeWithException() {
        InputStream mockConfigurationInputStream = Mockito.mock(InputStream.class,
                                                                invocationOnMock -> {
                                                                    throw new IOException();
                                                                });

        Configuration.initialize(mockConfigurationInputStream);

        assertEquals(new ArrayList<>(), Configuration.getSelectedArticleSources());
        assertEquals(Frame.NORMAL, Configuration.getFrameExtendedState());
        assertNull(Configuration.getFrameBounds());
    }

    @Test
    public void testInitializeOnlySourceIds() throws UnsupportedEncodingException {
        String configurationLines = "source-ids = test";

        Configuration.initialize(new ByteArrayInputStream(configurationLines.getBytes("UTF-8")));

        assertEquals(new ArrayList<>(), Configuration.getSelectedArticleSources());
        assertEquals(Frame.NORMAL, Configuration.getFrameExtendedState());
        assertNull(Configuration.getFrameBounds());

        assertTrue(Configuration.useSpanTable());
    }

    @Test
    public void testInitializeSourceIdsAndWindowConfiguration() throws UnsupportedEncodingException {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(null);
        ObjectHub.injectPersistencyHandler(mockPersistencyHandler);

        String configurationLines =
                "source-ids = nrc\n" +
                "source-test = nl.xs4all.home.freekdb.b52reader.sources.testdata.TestDataArticleSource\n" +
                "source-test-invalid = nl.invalid.InvalidTestDataArticleSource\n" +
                "source-verge = rss|The Verge|The Verge|https://www.theverge.com/rss/index.xml\n" +
                "source-verge-invalid = rss|The Verge|The Verge|invalid--https://www.theverge.com/rss/index.xml\n" +
                "source-nrc-rss = rss|NRC|NRC|https://www.nrc.nl/rss/|wetenschap\n" +
                "source-invalid-rss = rss|invalid\n" +
                "source-nrc = nl.xs4all.home.freekdb.b52reader.sources.nrc.NrcScienceArticleSource\n" +
                "window-configuration = maximized;0,0,1280x1024";

        Configuration.initialize(new ByteArrayInputStream(configurationLines.getBytes("UTF-8")));

        List<ArticleSource> selectedArticleSources = Configuration.getSelectedArticleSources();
        assertEquals(1, selectedArticleSources.size());
        assertEquals(NrcScienceArticleSource.class, selectedArticleSources.get(0).getClass());
        assertEquals(Frame.MAXIMIZED_BOTH, Configuration.getFrameExtendedState());
        assertEquals(new Rectangle(0, 0, 1280, 1024), Configuration.getFrameBounds());
    }

    @Test
    public void testInitializeHalfWindowConfiguration() throws UnsupportedEncodingException {
        String configurationLines = "window-configuration = normal";

        Configuration.initialize(new ByteArrayInputStream(configurationLines.getBytes("UTF-8")));

        assertEquals(new ArrayList<>(), Configuration.getSelectedArticleSources());
        assertEquals(Frame.NORMAL, Configuration.getFrameExtendedState());
        assertNull(Configuration.getFrameBounds());
    }
}

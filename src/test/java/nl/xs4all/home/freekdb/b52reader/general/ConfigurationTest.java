/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.browsers.EmbeddedBrowserType;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.articlesources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.articlesources.nrc.NrcScienceArticleSource;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link Configuration} class.
 */
public class ConfigurationTest {
    @Test
    public void testConstructorOnlySourceIds() throws IOException {
        byte[] configurationLinesBytes = "source-ids = test".getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes));

        assertEquals(new ArrayList<>(), configuration.getSelectedArticleSources());
        assertEquals(Frame.NORMAL, configuration.getFrameExtendedState());
        assertNull(configuration.getFrameBounds());
        assertTrue(configuration.useSpanTable());
    }

    @Test
    public void testConstructorSourceIdsAndWindowConfigurationWithBrowser() throws IOException {
        testConstructorSourceIdsAndWindowConfiguration(true);
    }

    @Test
    public void testConstructorSourceIdsAndWindowConfigurationDirectly() throws IOException {
        testConstructorSourceIdsAndWindowConfiguration(false);
    }

    private void testConstructorSourceIdsAndWindowConfiguration(boolean articleListWithBrowser) throws IOException {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(null);

        String configurationLines =
                "source-ids = nrc\n" +
                "source-test = nl.xs4all.home.freekdb.b52reader.articlesources.testdata.TestDataArticleSource\n" +
                "source-test-invalid = nl.invalid.InvalidTestDataArticleSource\n" +
                "source-verge = rss|The Verge|The Verge|https://www.theverge.com/rss/index.xml\n" +
                "source-verge-invalid = rss|The Verge|The Verge|invalid--https://www.theverge.com/rss/index.xml\n" +
                "source-nrc-rss = rss|NRC|NRC|https://www.nrc.nl/rss/|wetenschap\n" +
                "source-invalid-rss = rss|invalid\n" +
                "source-nrc = nl.xs4all.home.freekdb.b52reader.articlesources.nrc.NrcScienceArticleSource\n" +
                "window-configuration = maximized;0,0,1280x1024";

        byte[] configurationLinesBytes = configurationLines.getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes),
                                                        articleListWithBrowser);

        List<ArticleSource> selectedArticleSources = configuration.getSelectedArticleSources();
        assertEquals(1, selectedArticleSources.size());
        assertEquals(NrcScienceArticleSource.class, selectedArticleSources.get(0).getClass());
        assertEquals(Frame.MAXIMIZED_BOTH, configuration.getFrameExtendedState());
        assertEquals(new Rectangle(0, 0, 1280, 1024), configuration.getFrameBounds());
    }

    @Test
    public void testConstructorHalfWindowConfiguration() throws IOException {
        byte[] configurationLinesBytes = "window-configuration = normal".getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes));

        assertEquals(new ArrayList<>(), configuration.getSelectedArticleSources());
        assertEquals(Frame.NORMAL, configuration.getFrameExtendedState());
        assertNull(configuration.getFrameBounds());
    }

    @Test
    public void testConstructorWithException() {
        String exceptionMessage = "Some I/O exception...";

        InputStream mockConfigurationInputStream = Mockito.mock(InputStream.class,
                                                                invocationOnMock -> {
                                                                    throw new IOException(exceptionMessage);
                                                                });

        Configuration configuration = null;
        boolean exceptionThrown = false;

        try {
            configuration = new Configuration(mockConfigurationInputStream);
        } catch (IOException e) {
            exceptionThrown = exceptionMessage.equals(e.getMessage());
        }

        assertNull(configuration);
        assertTrue(exceptionThrown);
    }

    @Test
    public void testWriteConfiguration() throws IOException {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Author firstAuthor = new Author("Bill Hicks", 32);
        Author[] authors = {null};
        // Return an author object for the first call to getOrCreateAuthor and keep returning null after that.
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(firstAuthor, authors);

        String configurationLines =
                "source-ids = nrc\n" +
                "source-verge = rss|The Verge|The Verge|https://www.theverge.com/rss/index.xml\n" +
                "source-nrc-rss = rss|NRC|NRC|https://www.nrc.nl/rss/|wetenschap\n" +
                "source-nrc = nl.xs4all.home.freekdb.b52reader.articlesources.nrc.NrcScienceArticleSource\n" +
                "window-configuration = maximized;1,2,3x4";

        byte[] configurationLinesBytes = configurationLines.getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes));

        OutputStream configurationOutputStream = new ByteArrayOutputStream();

        assertTrue(configuration.writeConfiguration(configurationOutputStream, Frame.MAXIMIZED_BOTH,
                                                    new Rectangle(1, 2, 3, 4)));

        List<String> actualConfigurationData = Arrays.asList(configurationOutputStream
                                                                     .toString()
                                                                     .split(System.lineSeparator()));

        // Note: this test will fail if there is no internet connection (which is required for RSS article sources).
        assertEquals(9, actualConfigurationData.size());
        assertEquals("#" + configuration.getConfigurationHeader(), actualConfigurationData.get(0));
        assertTrue(actualConfigurationData.get(1).startsWith("#"));

        // Colons (":") are replaced by "\:" by the Properties.saveConvert method.
        String escapedDatabaseUrl = configuration.getDatabaseUrl().replaceAll(":", "\\\\:");

        List<String> expectedSubList = Arrays.asList(
                "source-ids=nrc",
                "source-nrc-rss=rss|NRC|NRC|https\\://www.nrc.nl/rss/|wetenschap",
                "source-nrc=nl.xs4all.home.freekdb.b52reader.articlesources.nrc.NrcScienceArticleSource",
                "source-verge=rss|The Verge|The Verge|https\\://www.theverge.com/rss/index.xml",
                "database-url=" + escapedDatabaseUrl,
                "database-driver-class-name=" + configuration.getDatabaseDriverClassName(),
                "window-configuration=maximized;1,2,3x4"
        );

        assertEquals(expectedSubList, actualConfigurationData.subList(2, 9));
    }

    @Test
    public void testWriteConfigurationWithException() throws IOException {
        OutputStream mockConfigurationOutputStream = Mockito.mock(OutputStream.class,
                                                                  invocationOnMock -> {
                                                                      throw new IOException();
                                                                  });

        byte[] configurationLinesBytes = "source-ids = test".getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes));

        assertFalse(configuration.writeConfiguration(mockConfigurationOutputStream, Frame.NORMAL, null));
    }

    @Test
    public void testConstantGetters() throws IOException {
        byte[] configurationLinesBytes = "".getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes));

        // The asserts below are rather silly. Is there a better way to check this configuration functionality?

        final String expectedFormatter = DateTimeFormatter.ofPattern("EEE dd-MMM HH:mm").toString();

        assertEquals("B52 reader 0.0.6", configuration.getApplicationNameAndVersion());
        assertEquals(expectedFormatter, configuration.getDateTimeFormatLonger().toString());
        assertEquals(6, configuration.getBackgroundBrowserMaxCount());
        assertEquals(new Color(205, 230, 247), configuration.getNiceLightBlue());
        assertEquals(EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING, configuration.getEmbeddedBrowserType());
        assertEquals(1200, configuration.getBackgroundTimerInitialDelay());
        assertEquals(1000, configuration.getBackgroundTimerDelay());
        assertEquals("fetched", configuration.getFetchedValue());
    }
}

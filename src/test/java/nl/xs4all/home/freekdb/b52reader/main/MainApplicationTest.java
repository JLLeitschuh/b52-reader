/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.awt.Frame;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;

import javax.swing.JPanel;

import nl.xs4all.home.freekdb.b52reader.articlesources.testdata.TestDataArticleSource;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class MainApplicationTest {
    private enum ShutdownApplicationResult { SUCCESS, CLOSE_CONNECTION_FAILS, EXCEPTION }

    @Test
    public void testCreateAndLaunchApplicationDatabaseWorks() throws MalformedURLException {
        MainGui mockMainGui = Mockito.mock(MainGui.class);
        URL configurationUrl = MainApplicationTest.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);

        Mockito.when(mockPersistencyHandler.initializeDatabaseConnection(Mockito.any(Connection.class)))
                .thenReturn(true);

        MainApplication mainApplication = new MainApplication(mockMainGui, configurationUrl, mockPersistencyHandler);

        mainApplication.createAndLaunchApplication();

        List<Article> expectedArticles = new TestDataArticleSource()
                .getArticles(mockPersistencyHandler, null, null);

        Mockito.verify(mockMainGui, Mockito.times(1)).initializeGui(expectedArticles);
    }

    @Test
    public void testCreateAndLaunchApplicationDatabaseFailure() throws MalformedURLException {
        MainGui mockMainGui = Mockito.mock(MainGui.class);
        URL configurationUrl = MainApplicationTest.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);

        Mockito.when(mockPersistencyHandler.initializeDatabaseConnection(Mockito.any(Connection.class)))
                .thenReturn(false);

        MainApplication mainApplication = new MainApplication(mockMainGui, configurationUrl, mockPersistencyHandler);

        mainApplication.createAndLaunchApplication();

        Mockito.verify(mockMainGui, Mockito.times(0)).initializeGui(Mockito.anyList());
    }

    @Test
    public void testCreateAndLaunchApplicationWithException() throws MalformedURLException {
        MainGui mockMainGui = Mockito.mock(MainGui.class);
        String configurationFileName = "b52-reader-exception.configuration";
        URL configurationUrl = MainApplicationTest.class.getClassLoader().getResource(configurationFileName);
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);

        MainApplication mainApplication = new MainApplication(mockMainGui, configurationUrl, mockPersistencyHandler);

        mainApplication.createAndLaunchApplication();

        Mockito.verify(mockMainGui, Mockito.times(0)).initializeGui(Mockito.anyList());
    }

    @Test
    public void testShutdownApplication() throws MalformedURLException {
        for (ShutdownApplicationResult result : ShutdownApplicationResult.values()) {
            doTestShutdownApplicationWithException(result);
        }
    }

    private void doTestShutdownApplicationWithException(ShutdownApplicationResult result) throws MalformedURLException {
        MainGui mockMainGui = Mockito.mock(MainGui.class);

        final URL configurationUrl;
        if (result.equals(ShutdownApplicationResult.EXCEPTION)) {
            configurationUrl = new URL("file:/this-directory-does-not-exist/so-this-is-not-a-valid-file");
        } else {
            configurationUrl = MainApplicationTest.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);
        }

        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);

        Mockito.when(mockMainGui.getBackgroundBrowsersPanel()).thenReturn(new JPanel());

        Mockito.when(mockPersistencyHandler.initializeDatabaseConnection(Mockito.any(Connection.class)))
                .thenReturn(true);

        final boolean closeConnectionReturnValue = !result.equals(ShutdownApplicationResult.CLOSE_CONNECTION_FAILS);
        Mockito.when(mockPersistencyHandler.closeDatabaseConnection()).thenReturn(closeConnectionReturnValue);

        MainApplication mainApplication = new MainApplication(mockMainGui, configurationUrl, mockPersistencyHandler);

        mainApplication.createAndLaunchApplication();

        final Rectangle frameBounds = new Rectangle(1, 2, 3, 4);
        final boolean returnValue = mainApplication.shutdownApplication(Frame.MAXIMIZED_BOTH, frameBounds);

        assertEquals(result.name(), !result.equals(ShutdownApplicationResult.EXCEPTION), returnValue);
    }
}

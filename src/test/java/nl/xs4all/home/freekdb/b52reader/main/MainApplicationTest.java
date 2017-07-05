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

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.testdata.TestDataArticleSource;

import org.junit.Test;
import org.mockito.Mockito;

public class MainApplicationTest {
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
        MainGui mockMainGui = Mockito.mock(MainGui.class);
        URL configurationUrl = MainApplicationTest.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);

        Mockito.when(mockMainGui.getBackgroundBrowsersPanel()).thenReturn(new JPanel());

        Mockito.when(mockPersistencyHandler.initializeDatabaseConnection(Mockito.any(Connection.class)))
                .thenReturn(true);

        MainApplication mainApplication = new MainApplication(mockMainGui, configurationUrl, mockPersistencyHandler);

        mainApplication.createAndLaunchApplication();

        mainApplication.shutdownApplication(Frame.MAXIMIZED_BOTH, new Rectangle(1, 2, 3, 4));
    }
}

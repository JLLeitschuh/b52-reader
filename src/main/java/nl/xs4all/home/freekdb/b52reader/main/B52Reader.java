/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.EmbeddedBrowserType;
import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.CombinationArticleSource;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

// todo: Embedded browser (JWebBrowser) does not resize when application window is resized after initial view?

// todo: Add Javadocs.

/**
 * The b52-reader main class which initializes the application and launches it.
 * <p>
 * mvn exec:java -Dexec.mainClass="nl.xs4all.home.freekdb.b52reader.main.B52Reader"
 */
public class B52Reader implements MainCallbacks {
    private static final Logger logger = LogManager.getLogger(B52Reader.class);

    private static MainGui mainGui;

    private PersistencyHandler persistencyHandler;
    private List<Article> currentArticles;

    public static void main(String[] arguments) {
        Utilities.ignoreStandardErrorStream();

        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            NativeInterface.open();
        }

        // todo: Why is the initialization of the GUI split in two methods?
        //       The backgroundTasksTimer is started at the end of the second phase (in the
        //       MainGui.completeGuiInitialization method).

        B52Reader b52Reader = new B52Reader();
        b52Reader.initializeApplication();

        // todo: Can we move this to the end of the initializeApplication method?
        SwingUtilities.invokeLater(() -> mainGui.completeGuiInitialization());

        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            NativeInterface.runEventPump();
        }
    }

    /**
     * Initialize and show enough of the application to fetch articles, possibly using background browsers.
     */
    private void initializeApplication() {
        initializeDatabase();

        currentArticles = getArticles(Configuration.getSelectedArticleSources());

        mainGui = new MainGui(this);
        mainGui.firstGuiInitialization(currentArticles);
    }

    private void initializeDatabase() {
        persistencyHandler = new PersistencyHandler();

        ObjectHub.injectPersistencyHandler(persistencyHandler);

        if (persistencyHandler.initializeDatabaseConnection()) {
            persistencyHandler.createTablesIfNeeded();
            persistencyHandler.readAuthorsAndArticles();
        }
    }

    private List<Article> getArticles(List<ArticleSource> articleSources) {
        Map<String, Article> storedArticlesMap = persistencyHandler.getStoredArticlesMap();
        Map<String, Author> storedAuthorsMap = persistencyHandler.getStoredAuthorsMap();

        return new CombinationArticleSource(articleSources).getArticles(storedArticlesMap, storedAuthorsMap);
    }

    private void saveDataAndCloseDatabase() {
        persistencyHandler.saveAuthorsAndArticles(currentArticles);

        if (persistencyHandler.closeDatabaseConnection()) {
            logger.debug("Closed the database connection.");
        }
    }

    @Override
    public void shutdownApplication(int frameExtendedState, Rectangle frameBounds) {
        Configuration.writeConfiguration(frameExtendedState, frameBounds);

        saveDataAndCloseDatabase();

        ObjectHub.getBackgroundBrowsers().closeAllBackgroundBrowsers();
    }
}

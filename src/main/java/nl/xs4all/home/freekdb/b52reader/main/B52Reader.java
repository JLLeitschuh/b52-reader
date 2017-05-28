/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

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

/**
 * The b52-reader main class which creates the application and launches it.
 * <p>
 * mvn exec:java -Dexec.mainClass="nl.xs4all.home.freekdb.b52reader.main.B52Reader"
 */
public class B52Reader implements MainCallbacks {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Handler for persistency functionality: storing and retrieving data in the database.
     */
    private PersistencyHandler persistencyHandler;

    /**
     * List of the currently available articles.
     */
    private List<Article> currentArticles;

    /**
     * The main method that starts the application.
     *
     * @param arguments the (currently unused) command-line parameters.
     */
    public static void main(String[] arguments) {
        Utilities.ignoreStandardErrorStream();

        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            NativeInterface.open();
        }

        new B52Reader().createAndLaunchApplication();

        if (Constants.EMBEDDED_BROWSER_TYPE == EmbeddedBrowserType.EMBEDDED_BROWSER_DJ_NATIVE_SWING) {
            NativeInterface.runEventPump();
        }
    }

    /**
     * Initialize and show enough of the application to fetch articles, possibly using background browsers.
     */
    private void createAndLaunchApplication() {
        initializeDatabase();

        MainGui mainGui = new MainGui(this);
        mainGui.initializeBackgroundBrowsersPanel();
        
        currentArticles = getArticles(Configuration.getSelectedArticleSources());

        mainGui.initializeGui(currentArticles);
    }

    /**
     * Initialize the database connection and read the articles & authors.
     */
    private void initializeDatabase() {
        persistencyHandler = new PersistencyHandler();

        ObjectHub.injectPersistencyHandler(persistencyHandler);

        if (persistencyHandler.initializeDatabaseConnection()) {
            persistencyHandler.createTablesIfNeeded();
            persistencyHandler.readAuthorsAndArticles();
        }
    }

    /**
     * Get the articles that are currently available from the configured article sources.
     *
     * @param articleSources the configured article sources.
     * @return the current articles.
     */
    private List<Article> getArticles(List<ArticleSource> articleSources) {
        Map<String, Article> storedArticlesMap = persistencyHandler.getStoredArticlesMap();
        Map<String, Author> storedAuthorsMap = persistencyHandler.getStoredAuthorsMap();

        return new CombinationArticleSource(articleSources).getArticles(storedArticlesMap, storedAuthorsMap);
    }

    /**
     * Handle shutdown of the application.
     *
     * @param frameExtendedState the application window state (normal or maximized).
     * @param frameBounds        the application window bounds.
     */
    @Override
    public void shutdownApplication(int frameExtendedState, Rectangle frameBounds) {
        Configuration.writeConfiguration(frameExtendedState, frameBounds);

        saveDataAndCloseDatabase();

        ObjectHub.getBackgroundBrowsers().closeAllBackgroundBrowsers();
    }

    /**
     * Save all data and close the database connection.
     */
    private void saveDataAndCloseDatabase() {
        persistencyHandler.saveAuthorsAndArticles(currentArticles);

        if (persistencyHandler.closeDatabaseConnection()) {
            logger.debug("Closed the database connection.");
        }
    }
}

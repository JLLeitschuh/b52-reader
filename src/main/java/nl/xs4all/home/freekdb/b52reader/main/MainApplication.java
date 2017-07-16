/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import nl.xs4all.home.freekdb.b52reader.articlesources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.articlesources.CombinationArticleSource;
import nl.xs4all.home.freekdb.b52reader.browsers.BackgroundBrowsers;
import nl.xs4all.home.freekdb.b52reader.browsers.JWebBrowserFactory;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application class that takes care of initializing and launching the application.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class MainApplication implements MainCallbacks {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Main GUI object.
     */
    private final MainGui mainGui;

    /**
     * URL pointing to the configuration data.
     */
    private final URL configurationUrl;

    /**
     * Handler for persistency functionality: storing and retrieving data in the database.
     */
    private final PersistencyHandler persistencyHandler;

    /**
     * Configuration object with data from the configuration file.
     */
    private Configuration configuration;

    /**
     * List of the currently available articles.
     */
    private List<Article> currentArticles;

    /**
     * Background browsers handler.
     */
    private BackgroundBrowsers backgroundBrowsers;

    /**
     * Construct a main application object and inject the main gui, configuration URL & persistency handler.
     *
     * @param mainGui            the main GUI object.
     * @param configurationUrl   the URL pointing to the configuration data.
     * @param persistencyHandler the persistency handler that should be used.
     */
    MainApplication(MainGui mainGui, URL configurationUrl, PersistencyHandler persistencyHandler) {
        this.mainGui = mainGui;
        this.persistencyHandler = persistencyHandler;
        this.configurationUrl = configurationUrl;

        this.mainGui.setMainCallbacks(this);
    }

    /**
     * Initialize and show enough of the application to fetch articles, possibly using background browsers.
     */
    void createAndLaunchApplication() {
        configuration = initializeConfiguration();

        if (configuration != null && initializeDatabase()) {
            mainGui.initializeBackgroundBrowsersPanel(new JFrame(), configuration);

            backgroundBrowsers = new BackgroundBrowsers(new JWebBrowserFactory(),
                                                        mainGui.getBackgroundBrowsersPanel());

            configuration.injectBackgroundBrowsers(backgroundBrowsers);

            currentArticles = getArticles(configuration.getSelectedArticleSources());

            mainGui.initializeGui(currentArticles);
        }
    }

    /**
     * Initialize the configuration with data from the configuration file.
     */
    private Configuration initializeConfiguration() {
        Configuration applicationConfiguration = null;

        try {
            if (configurationUrl != null) {
                InputStream configurationInputStream = new FileInputStream(configurationUrl.getFile());

                applicationConfiguration = new Configuration(configurationInputStream, persistencyHandler);
            }
        } catch (IOException e) {
            logger.error("Exception while reading the configuration file " + configurationUrl, e);
        }

        return applicationConfiguration;
    }

    /**
     * Initialize the database connection and read the articles & authors.
     *
     * @return whether the database initialization was successful.
     */
    private boolean initializeDatabase() {
        boolean result = true;

        try {
            Class.forName(configuration.getDatabaseDriverClassName());
            String databaseUrl = configuration.getDatabaseUrl();
            Connection databaseConnection = DriverManager.getConnection(databaseUrl, "b52", "reader");

            if (persistencyHandler.initializeDatabaseConnection(databaseConnection)) {
                persistencyHandler.createTablesIfNeeded();
                persistencyHandler.readAuthorsAndArticles();
            } else {
                result = false;
            }
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Exception while initializing the database connection.", e);

            result = false;
        }

        return result;
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

        return new CombinationArticleSource(articleSources).getArticles(persistencyHandler, storedArticlesMap,
                                                                        storedAuthorsMap);
    }

    /**
     * Handle shutdown of the application.
     *
     * @param frameExtendedState the application window state (<code>Frame.NORMAL</code> or
     *                           <code>Frame.MAXIMIZED_BOTH</code>).
     * @param frameBounds        the application window bounds.
     * @return whether the shutdown was done successfully.
     */
    @Override
    public boolean shutdownApplication(int frameExtendedState, Rectangle frameBounds) {
        boolean result = true;

        try {
            if (configurationUrl != null) {
                OutputStream configurationOutputStream = new FileOutputStream(configurationUrl.getFile());

                if (!configuration.writeConfiguration(configurationOutputStream, frameExtendedState, frameBounds)) {
                    logger.error("Error while writing the configuration file " + configurationUrl);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Exception while writing the configuration file " + configurationUrl, e);

            result = false;
        }

        if (result) {
            saveDataAndCloseDatabase();

            if (backgroundBrowsers != null) {
                backgroundBrowsers.closeAllBackgroundBrowsers();
            }
        }

        return result;
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

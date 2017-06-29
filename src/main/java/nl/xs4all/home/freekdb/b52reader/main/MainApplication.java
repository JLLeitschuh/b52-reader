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

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.gui.MainGui;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.CombinationArticleSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application class that takes care of initializing and launching the application.
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
        if (initializeDatabase()) {
            configuration = initializeConfiguration();

            if (configuration != null) {
                mainGui.initializeBackgroundBrowsersPanel(new JFrame(), configuration);

                currentArticles = getArticles(configuration.getSelectedArticleSources());

                mainGui.initializeGui(currentArticles);
            }
        }
    }

    /**
     * Initialize the database connection and read the articles & authors.
     *
     * @return whether the database initialization was successful.
     */
    private boolean initializeDatabase() {
        boolean result = true;

        try {
            Class.forName("org.h2.Driver");
            String databaseUrl = "jdbc:h2:./data/b52-reader-settings";
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
     * @param frameExtendedState the application window state (normal or maximized).
     * @param frameBounds        the application window bounds.
     */
    @Override
    public void shutdownApplication(int frameExtendedState, Rectangle frameBounds) {
        try {
            if (configurationUrl != null) {
                OutputStream configurationOutputStream = new FileOutputStream(configurationUrl.getFile());

                if (!configuration.writeConfiguration(configurationOutputStream, frameExtendedState, frameBounds)) {
                    logger.error("Error while writing the configuration file " + configurationUrl);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Exception while writing the configuration file " + configurationUrl, e);
        }

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

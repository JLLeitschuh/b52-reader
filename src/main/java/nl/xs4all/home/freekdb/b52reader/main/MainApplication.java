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
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import nl.xs4all.home.freekdb.b52reader.general.ConfigurationV2;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
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
     * Handler for persistency functionality: storing and retrieving data in the database.
     */
    private PersistencyHandler persistencyHandler;

    /**
     * Configuration object with data from the configuration file.
     */
    private ConfigurationV2 configurationV2;

    /**
     * List of the currently available articles.
     */
    private List<Article> currentArticles;

    /**
     * Initialize and show enough of the application to fetch articles, possibly using background browsers.
     */
    void createAndLaunchApplication() {
        initializeDatabase();

        configurationV2 = initializeConfiguration();

        if (configurationV2 != null) {
            MainGui mainGui = new MainGui(this);
            mainGui.initializeBackgroundBrowsersPanel(new JFrame(), configurationV2);

            currentArticles = getArticles(configurationV2.getSelectedArticleSources());

            mainGui.initializeGui(currentArticles);
        }
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
     * Initialize the configuration with data from the configuration file.
     */
    private ConfigurationV2 initializeConfiguration() {
        ConfigurationV2 applicationConfiguration = null;
        URL configurationUrl = ConfigurationV2.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);

        try {
            if (configurationUrl != null) {
                InputStream configurationInputStream = new FileInputStream(configurationUrl.getFile());
                applicationConfiguration = new ConfigurationV2(configurationInputStream);
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
        URL configurationUrl = ConfigurationV2.class.getClassLoader().getResource(Constants.CONFIGURATION_FILE_NAME);

        try {
            if (configurationUrl != null) {
                OutputStream configurationOutputStream = new FileOutputStream(configurationUrl.getFile());

                if (!configurationV2.writeConfiguration(configurationOutputStream, frameExtendedState, frameBounds)) {
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

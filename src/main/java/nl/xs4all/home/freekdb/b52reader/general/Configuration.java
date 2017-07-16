/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import nl.xs4all.home.freekdb.b52reader.articlesources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.articlesources.RssArticleSource;
import nl.xs4all.home.freekdb.b52reader.articlesources.nrc.NrcScienceArticleSource;
import nl.xs4all.home.freekdb.b52reader.articlesources.website.ArticleListFetcher;
import nl.xs4all.home.freekdb.b52reader.articlesources.website.HtmlHelper;
import nl.xs4all.home.freekdb.b52reader.browsers.BackgroundBrowsers;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration functionality (most settings are stored in and read from the configuration file).
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class Configuration {
    /**
     * Property key for source-ids: the selected article sources that are configured to be read.
     */
    private static final String SOURCE_IDS_KEY = "source-ids";

    /**
     * The name and version of the application.
     */
    private static final String APPLICATION_NAME_AND_VERSION = "B52 reader 0.0.6";

    /**
     * The maximum number of browsers that are loaded in the background.
     */
    private static final int BACKGROUND_BROWSER_MAX_COUNT = 6;

    /**
     * The initial delay in milliseconds before starting the background tasks: preloading browsers.
     */
    private static final int BACKGROUND_TIMER_INITIAL_DELAY = 1200;

    /**
     * The delay in milliseconds between background tasks: preloading browsers.
     */
    private static final int BACKGROUND_TIMER_DELAY = 1000;

    /**
     * The main URL of the NRC Handelsblad website.
     */
    private static final String NRC_MAIN_URL = "https://www.nrc.nl/";

    /**
     * The header for the configuration file.
     */
    private static final String CONFIGURATION_HEADER = "Configuration file for the b52-reader "
                                                       + "(https://github.com/FreekDB/b52-reader).";

    /**
     * Cell value for fetched articles.
     */
    private static final String FETCHED_VALUE = "fetched";

    /**
     * Default database driver class name to use for storing data.
     */
    private static final String DEFAULT_DATABASE_DRIVER_CLASS_NAME = "org.h2.Driver";

    /**
     * Default database URL to use for storing data.
     */
    private static final String DEFAULT_DATABASE_URL = "jdbc:h2:./data/b52-reader-settings";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Handler for persistency functionality: storing and retrieving data in the database.
     */
    private final PersistencyHandler persistencyHandler;

    /**
     * Selected article sources (from configuration file).
     */
    private List<ArticleSource> selectedArticleSources;

    /**
     * All available article sources (from configuration file).
     */
    private List<ArticleSource> allArticleSources;

    /**
     * The application window state (normal or maximized; from configuration file).
     */
    private int frameExtendedState;

    /**
     * The application window position (from configuration file).
     */
    private Rectangle frameBounds;

    /**
     * Database driver class name to use for storing data.
     */
    private String databaseDriverClassName;

    /**
     * Database URL to use for storing data.
     */
    private String databaseUrl;

    /**
     * Background browsers handler.
     */
    private BackgroundBrowsers backgroundBrowsers;

    /**
     * Initialize by reading the configuration data and filling the <code>selectedArticleSources</code> and
     * <code>allArticleSources</code> lists.
     *
     * @param configurationInputStream the input stream that contains the configuration data.
     * @param persistencyHandler       the handler for persistency functionality: storing and retrieving data in the
     *                                 database.
     */
    public Configuration(InputStream configurationInputStream, PersistencyHandler persistencyHandler)
            throws IOException {
        this(configurationInputStream, persistencyHandler, false);
    }

    /**
     * Initialize by reading the configuration data and filling the <code>selectedArticleSources</code> and
     * <code>allArticleSources</code> lists.
     *
     * @param configurationInputStream the input stream that contains the configuration data.
     * @param persistencyHandler       the handler for persistency functionality: storing and retrieving data in the
     *                                 database.
     * @param articleListWithBrowser   whether to use a background browser for fetching the html with the list of
     *                                 articles, which is for example necessary when the html page is dynamically
     *                                 generated.
     */
    public Configuration(InputStream configurationInputStream, PersistencyHandler persistencyHandler,
                         boolean articleListWithBrowser)
            throws IOException {
        this.persistencyHandler = persistencyHandler;

        List<String> sourceIds = new ArrayList<>(Arrays.asList("nrc", "test"));
        allArticleSources = new ArrayList<>();
        selectedArticleSources = new ArrayList<>();
        frameExtendedState = Frame.NORMAL;
        frameBounds = null;
        databaseDriverClassName = DEFAULT_DATABASE_DRIVER_CLASS_NAME;
        databaseUrl = DEFAULT_DATABASE_URL;

        try {
            Properties configuration = new Properties();

            configuration.load(configurationInputStream);

            String sourceIdsProperty = configuration.getProperty(SOURCE_IDS_KEY, "nrc,test");
            sourceIds.clear();
            sourceIds.addAll(Arrays.asList(sourceIdsProperty.split(",")));

            addConfiguredSources(configuration, articleListWithBrowser);

            String windowConfiguration = configuration.getProperty("window-configuration");

            if (windowConfiguration != null) {
                frameExtendedState = windowConfiguration.startsWith("maximized") ? Frame.MAXIMIZED_BOTH : Frame.NORMAL;

                if (windowConfiguration.contains(";")) {
                    String boundsConfiguration = windowConfiguration.substring(windowConfiguration.indexOf(';') + 1);
                    frameBounds = getBoundsFromConfiguration(boundsConfiguration);
                }
            }

            databaseDriverClassName = configuration.getProperty("database-driver-class-name",
                                                                DEFAULT_DATABASE_DRIVER_CLASS_NAME);

            databaseUrl = configuration.getProperty("database-url", DEFAULT_DATABASE_URL);

            selectedArticleSources = allArticleSources.stream()
                    .filter(articleSource -> sourceIds.contains(articleSource.getSourceId()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Exception while reading the configuration data.", e);

            throw e;
        }
    }

    /**
     * Get the selected article sources.
     *
     * @return the selected article sources.
     */
    public List<ArticleSource> getSelectedArticleSources() {
        return selectedArticleSources;
    }

    /**
     * Get the application window state (normal or maximized).
     *
     * @return the application window state (normal or maximized).
     */
    public int getFrameExtendedState() {
        return frameExtendedState;
    }

    /**
     * Get the application window position.
     *
     * @return the application window position.
     */
    public Rectangle getFrameBounds() {
        return frameBounds;
    }

    /**
     * Should the GUI span table or the table with the custom article renderer be used.
     *
     * @return use the GUI span table (true) or the table with the custom article renderer (false).
     */
    public boolean useSpanTable() {
        return true;
    }

    /**
     * Inject the background browsers handler.
     *
     * @param backgroundBrowsers the background browsers handler.
     */
    public void injectBackgroundBrowsers(BackgroundBrowsers backgroundBrowsers) {
        this.backgroundBrowsers = backgroundBrowsers;
    }

    /**
     * Write the application configuration to the output stream.
     *
     * @param configurationOutputStream the output stream that will receive the configuration data.
     * @param frameExtendedState        the application window state (normal or maximized).
     * @param frameBounds               the application window bounds.
     * @return whether the configuration was successfully written.
     */
    public boolean writeConfiguration(OutputStream configurationOutputStream, int frameExtendedState,
                                      Rectangle frameBounds) {
        boolean result = true;

        try {
            Properties configuration = new Properties();

            String sourceIds = selectedArticleSources.stream()
                    .map(ArticleSource::getSourceId)
                    .collect(Collectors.joining(","));

            configuration.setProperty(SOURCE_IDS_KEY, sourceIds);

            for (ArticleSource articleSource : allArticleSources) {
                String parameters = articleSource instanceof RssArticleSource
                        ? getRssParameters((RssArticleSource) articleSource)
                        : articleSource.getClass().getName();

                configuration.setProperty("source-" + articleSource.getSourceId(), parameters);
            }

            String windowConfiguration = (frameExtendedState != Frame.MAXIMIZED_BOTH ? "normal" : "maximized") +
                                         (frameBounds != null
                                                 ? ";" + frameBounds.x + "," + frameBounds.y + "," +
                                                   frameBounds.width + "x" + frameBounds.height
                                                 : "");

            configuration.setProperty("window-configuration", windowConfiguration);

            configuration.setProperty("database-driver-class-name", databaseDriverClassName);
            configuration.setProperty("database-url", databaseUrl);

            configuration.store(configurationOutputStream, getConfigurationHeader());
        } catch (IOException e) {
            logger.error("Exception while reading the configuration data.", e);

            result = false;
        }

        return result;
    }

    /**
     * Get the name and version of the application.
     *
     * @return the name and version of the application.
     */
    public String getApplicationNameAndVersion() {
        return APPLICATION_NAME_AND_VERSION;
    }

    /**
     * Get the maximum number of browsers that are loaded in the background.
     *
     * @return the maximum number of browsers that are loaded in the background.
     */
    public int getBackgroundBrowserMaxCount() {
        return BACKGROUND_BROWSER_MAX_COUNT;
    }

    /**
     * Get the initial delay in milliseconds before starting the background tasks: preloading browsers.
     *
     * @return the initial delay in milliseconds before starting the background tasks: preloading browsers.
     */
    public int getBackgroundTimerInitialDelay() {
        return BACKGROUND_TIMER_INITIAL_DELAY;
    }

    /**
     * Get the delay in milliseconds between background tasks: preloading browsers.
     *
     * @return the delay in milliseconds between background tasks: preloading browsers.
     */
    public int getBackgroundTimerDelay() {
        return BACKGROUND_TIMER_DELAY;
    }

    /**
     * Get the main URL of the NRC Handelsblad website.
     *
     * @return the main URL of the NRC Handelsblad website.
     */
    public String getNrcMainUrl() {
        return NRC_MAIN_URL;
    }

    /**
     * Get the header for the configuration file.
     *
     * @return the header for the configuration file.
     */
    String getConfigurationHeader() {
        return CONFIGURATION_HEADER;
    }

    /**
     * Get the cell value for fetched articles.
     *
     * @return the cell value for fetched articles.
     */
    public String getFetchedValue() {
        return FETCHED_VALUE;
    }

    /**
     * Get the database driver class name to use for storing data.
     *
     * @return the database driver class name to use for storing data.
     */
    public String getDatabaseDriverClassName() {
        return databaseDriverClassName;
    }

    /**
     * Get the database URL to use for storing data.
     *
     * @return the database URL to use for storing data.
     */
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * Add the configured article sources to the <code>allArticleSources</code> list.
     *
     * @param configuration          the configuration properties.
     * @param articleListWithBrowser whether to use a background browser for fetching the html with the list of
     *                               articles, which is for example necessary when the html page is dynamically
     *                               generated.
     */
    private void addConfiguredSources(Properties configuration, boolean articleListWithBrowser) {
        String sourcePrefix = "source-";

        Collections.list(configuration.propertyNames()).forEach(name -> {
            String propertyName = (String) name;

            if (propertyName.startsWith(sourcePrefix) && !propertyName.equals(SOURCE_IDS_KEY)) {
                String sourceId = propertyName.substring(sourcePrefix.length());
                String sourceConfiguration = configuration.getProperty(propertyName);

                ArticleSource articleSource = createArticleSource(sourceId, sourceConfiguration,
                                                                  articleListWithBrowser);

                if (articleSource != null) {
                    allArticleSources.add(articleSource);
                }
            }
        });
    }

    /**
     * Create an article source object from the source id and the source configuration.
     *
     * @param sourceId               the source id.
     * @param sourceConfiguration    the source configuration.
     * @param articleListWithBrowser whether to use a background browser for fetching the html with the list of
     *                               articles, which is for example necessary when the html page is dynamically
     *                               generated.
     * @return the new article source object.
     */
    private ArticleSource createArticleSource(String sourceId, String sourceConfiguration,
                                              boolean articleListWithBrowser) {
        ArticleSource articleSource = null;

        try {
            if (sourceConfiguration.startsWith("rss|")) {
                String[] configurationItems = sourceConfiguration.split("\\|");

                if (configurationItems.length >= 4) {
                    articleSource = (ArticleSource) constructRssArticleSource(configurationItems, sourceId);
                }
            } else {
                Class<?> sourceClass = Class.forName(sourceConfiguration);

                if (sourceClass.equals(NrcScienceArticleSource.class)) {
                    // This configuration needs to become more generic: WebSiteArticleSource as a base class?

                    Constructor<?> constructor = NrcScienceArticleSource.class.getConstructor(ArticleListFetcher.class,
                                                                                              Configuration.class);

                    String url = getNrcMainUrl() + "sectie/wetenschap/";

                    BackgroundBrowsers backgroundBrowsersHandler = articleListWithBrowser
                            ? backgroundBrowsers
                            : null;

                    ArticleListFetcher fetcher = new ArticleListFetcher(url, articleListWithBrowser,
                                                                        backgroundBrowsersHandler, new HtmlHelper());

                    articleSource = (ArticleSource) constructor.newInstance(fetcher, this);
                } else {
                    articleSource = (ArticleSource) sourceClass.getConstructor().newInstance();
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                InvocationTargetException e) {
            logger.error("Exception while initializing article source " + sourceId + ".", e);
        }

        return articleSource;
    }

    private Object constructRssArticleSource(String[] configurationItems, String sourceId) {
        Object source = null;

        try {
            String feedName = configurationItems[1];
            Author defaultAuthor = persistencyHandler.getOrCreateAuthor(configurationItems[2]);
            URL feedUrl = new URL(configurationItems[3]);
            String categoryName = configurationItems.length >= 5 ? configurationItems[4] : null;

            SyndFeed feed = new SyndFeedInput().build(new XmlReader(feedUrl));

            source = new RssArticleSource(sourceId, feed, feedName, defaultAuthor, feedUrl, categoryName);
        } catch (FeedException | IOException e) {
            logger.error("Exception while fetching articles from an RSS feed.", e);
        }

        return source;
    }

    /**
     * Create a rectangle with the window bounds from the bounds configuration.
     *
     * @param boundsConfiguration the bounds configuration.
     * @return the rectangle with the window bounds.
     */
    private Rectangle getBoundsFromConfiguration(String boundsConfiguration) {
        int[] bounds = Arrays.stream(boundsConfiguration.split("[,x]")).mapToInt(Integer::parseInt).toArray();

        return new Rectangle(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    /**
     * Get the configuration parameters for an RSS article source.
     *
     * @param rssSource the RSS article source.
     * @return the configuration parameters for an RSS article source.
     */
    private String getRssParameters(RssArticleSource rssSource) {
        String authorName = rssSource.getDefaultAuthor() != null
                ? rssSource.getDefaultAuthor().getName()
                : rssSource.getFeedName();

        return "rss|" + rssSource.getFeedName() + "|" + authorName + "|" +
               rssSource.getFeedUrl() + (rssSource.getCategoryName() != null ? "|" + rssSource.getCategoryName() : "");
    }
}

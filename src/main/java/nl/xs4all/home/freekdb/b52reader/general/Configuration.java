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
@SuppressWarnings("checkstyle:multiplestringliterals")
public class Configuration {
    /**
     * The name and version of the application.
     */
    private static final String APPLICATION_NAME_AND_VERSION = "B52 reader 0.0.6";

    /**
     * Prefix for source ids and source configurations.
     */
    private static final String SOURCE_PREFIX = "source-";

    /**
     * Property key for source ids: the selected article sources that are configured to be read.
     */
    private static final String SOURCE_IDS_KEY = SOURCE_PREFIX + "ids";

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
     * Property key for the database driver class name.
     */
    private static final String DATABASE_DRIVER_CLASS_NAME_KEY = "database-driver-class-name";

    /**
     * Property key for the database URL.
     */
    private static final String DATABASE_URL_KEY = "database-url";

    /**
     * Property key for the window configuration.
     */
    private static final String WINDOWS_CONFIGURATION_KEY = "window-configuration";

    /**
     * Default database driver class name to use for storing data.
     */
    private static final String DEFAULT_DATABASE_DRIVER_CLASS_NAME = "org.h2.Driver";

    /**
     * Default database URL to use for storing data.
     */
    private static final String DEFAULT_DATABASE_URL = "jdbc:h2:./data/b52-reader-settings";

    /**
     * Separator for source ids.
     */
    //@SuppressWarnings("checkstyle:multiplestringliterals")
    private static final String SOURCE_IDS_SEPARATOR = ",";
    //private static final String SOURCE_IDS_SEPARATOR = ",[ignore]".substring(0, 1)

    /**
     * Frame state maximized (in windows configuration).
     */
    private static final String FRAME_STATE_MAXIMIZED = "maximized";

    /**
     * Prefix for bounds (before x-coordinate).
     */
    private static final String BOUNDS_PREFIX = ";";

    /**
     * Separator for bounds (between x-coordinate, y-coordinate, and width).
     */
    //@SuppressWarnings("checkstyle:multiplestringliterals")
    private static final String BOUNDS_SEPARATOR = ",";

    /**
     * Prefix for RSS configuration.
     */
    private static final String RSS_CONFIGURATION_PREFIX = "rss|";

    /**
     * Separator for RSS configuration.
     */
    @SuppressWarnings("EmptyAlternationBranch")
    private static final String RSS_CONFIGURATION_SEPARATOR = "|";

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
     * @throws IOException if an error occurred when reading from the configuration input stream.
     */
    public Configuration(final InputStream configurationInputStream, final PersistencyHandler persistencyHandler)
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
     * @throws IOException if an error occurred when reading from the configuration input stream.
     */
    public Configuration(final InputStream configurationInputStream, final PersistencyHandler persistencyHandler,
                         final boolean articleListWithBrowser)
            throws IOException {
        this.persistencyHandler = persistencyHandler;

        final List<String> sourceIds = new ArrayList<>(Arrays.asList("nrc", "test"));
        allArticleSources = new ArrayList<>();
        selectedArticleSources = new ArrayList<>();
        frameExtendedState = Frame.NORMAL;
        frameBounds = null;
        databaseDriverClassName = DEFAULT_DATABASE_DRIVER_CLASS_NAME;
        databaseUrl = DEFAULT_DATABASE_URL;

        try {
            final Properties configuration = new Properties();

            configuration.load(configurationInputStream);

            final String sourceIdsProperty = configuration.getProperty(SOURCE_IDS_KEY, "nrc,test");
            sourceIds.clear();
            sourceIds.addAll(Arrays.asList(sourceIdsProperty.split(SOURCE_IDS_SEPARATOR)));

            addConfiguredSources(configuration, articleListWithBrowser);

            final String windowConfiguration = configuration.getProperty(WINDOWS_CONFIGURATION_KEY);

            if (windowConfiguration != null) {
                frameExtendedState = windowConfiguration.startsWith(FRAME_STATE_MAXIMIZED)
                    ? Frame.MAXIMIZED_BOTH
                    : Frame.NORMAL;

                if (windowConfiguration.contains(BOUNDS_PREFIX)) {
                    final String boundsConfiguration = windowConfiguration.substring(windowConfiguration.indexOf(';') + 1);
                    frameBounds = getBoundsFromConfiguration(boundsConfiguration);
                }
            }

            databaseDriverClassName = configuration.getProperty(DATABASE_DRIVER_CLASS_NAME_KEY,
                                                                DEFAULT_DATABASE_DRIVER_CLASS_NAME);

            databaseUrl = configuration.getProperty(DATABASE_URL_KEY, DEFAULT_DATABASE_URL);

            selectedArticleSources = allArticleSources.stream()
                .filter(articleSource -> sourceIds.contains(articleSource.getSourceId()))
                .collect(Collectors.toList());
        } catch (final IOException e) {
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
    public void injectBackgroundBrowsers(final BackgroundBrowsers backgroundBrowsers) {
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
    public boolean writeConfiguration(final OutputStream configurationOutputStream, final int frameExtendedState,
                                      final Rectangle frameBounds) {
        boolean result = true;

        try {
            final Properties configuration = new Properties();

            final String sourceIds = selectedArticleSources.stream()
                .map(ArticleSource::getSourceId)
                .collect(Collectors.joining(SOURCE_IDS_SEPARATOR));

            configuration.setProperty(SOURCE_IDS_KEY, sourceIds);

            for (ArticleSource articleSource : allArticleSources) {
                final String parameters = articleSource instanceof RssArticleSource
                    ? getRssParameters((RssArticleSource) articleSource)
                    : articleSource.getClass().getName();

                configuration.setProperty(SOURCE_PREFIX + articleSource.getSourceId(), parameters);
            }

            final String windowConfiguration
                = (frameExtendedState != Frame.MAXIMIZED_BOTH ? "normal" : FRAME_STATE_MAXIMIZED)
                  + (frameBounds != null ? getFrameBoundsConfiguration(frameBounds) : "");

            configuration.setProperty(WINDOWS_CONFIGURATION_KEY, windowConfiguration);

            configuration.setProperty(DATABASE_DRIVER_CLASS_NAME_KEY, databaseDriverClassName);
            configuration.setProperty(DATABASE_URL_KEY, databaseUrl);

            configuration.store(configurationOutputStream, getConfigurationHeader());
        } catch (final IOException e) {
            logger.error("Exception while writing the configuration data.", e);

            result = false;
        }

        return result;
    }

    /**
     * Get the frame bounds in a format suitable for storing the configuration.
     *
     * @param frameBounds the application window bounds.
     * @return the frame bounds in a format suitable for storing the configuration.
     */
    private String getFrameBoundsConfiguration(final Rectangle frameBounds) {
        return BOUNDS_PREFIX + frameBounds.x + BOUNDS_SEPARATOR + frameBounds.y + BOUNDS_SEPARATOR
               + frameBounds.width + "x" + frameBounds.height;
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
    private void addConfiguredSources(final Properties configuration, final boolean articleListWithBrowser) {
        Collections.list(configuration.propertyNames()).forEach(name -> {
            final String propertyName = (String) name;

            if (propertyName.startsWith(SOURCE_PREFIX) && !propertyName.equals(SOURCE_IDS_KEY)) {
                final String sourceId = propertyName.substring(SOURCE_PREFIX.length());
                final String sourceConfiguration = configuration.getProperty(propertyName);

                final ArticleSource articleSource = createArticleSource(sourceId, sourceConfiguration,
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
    private ArticleSource createArticleSource(final String sourceId, final String sourceConfiguration,
                                              final boolean articleListWithBrowser) {
        ArticleSource articleSource = null;

        try {
            if (sourceConfiguration.startsWith(RSS_CONFIGURATION_PREFIX)) {
                final String[] configurationItems = sourceConfiguration.split("\\" + RSS_CONFIGURATION_SEPARATOR);
                final int minimumRssConfigurationItems = 4;

                if (configurationItems.length >= minimumRssConfigurationItems) {
                    articleSource = (ArticleSource) constructRssArticleSource(configurationItems, sourceId);
                }
            } else {
                final Class<?> sourceClass = Class.forName(sourceConfiguration);

                if (sourceClass.equals(NrcScienceArticleSource.class)) {
                    // This configuration needs to become more generic: WebSiteArticleSource as a base class?

                    final Constructor<?> constructor
                        = NrcScienceArticleSource.class.getConstructor(ArticleListFetcher.class, Configuration.class);

                    final String url = getNrcMainUrl() + "sectie/wetenschap/";

                    final BackgroundBrowsers backgroundBrowsersHandler = articleListWithBrowser
                        ? backgroundBrowsers
                        : null;

                    final ArticleListFetcher fetcher = new ArticleListFetcher(url, articleListWithBrowser,
                                                                              backgroundBrowsersHandler,
                                                                              new HtmlHelper());

                    articleSource = (ArticleSource) constructor.newInstance(fetcher, this);
                } else {
                    articleSource = (ArticleSource) sourceClass.getConstructor().newInstance();
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
            | InvocationTargetException e) {
            logger.error("Exception while initializing article source " + sourceId + ".", e);
        }

        return articleSource;
    }

    /**
     * Construct an RSS article source using the specified configuration items and source id. The RSS feed is also
     * created; network access is required to do this.
     *
     * @param configurationItems the configuration items to use.
     * @param sourceId           the source id.
     * @return the new RSS article source.
     */
    private Object constructRssArticleSource(final String[] configurationItems, final String sourceId) {
        Object source = null;

        try {
            final int feedNameIndex = 1;
            final int authorNameIndex = 2;
            final int feedUrlIndex = 3;
            final int categoryNameIndex = 4;

            final String feedName = configurationItems[feedNameIndex];
            final Author defaultAuthor = persistencyHandler.getOrCreateAuthor(configurationItems[authorNameIndex]);
            final URL feedUrl = new URL(configurationItems[feedUrlIndex]);

            final String categoryName = configurationItems.length > categoryNameIndex
                ? configurationItems[categoryNameIndex]
                : null;

            final SyndFeed feed = new SyndFeedInput().build(new XmlReader(feedUrl));

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
    private Rectangle getBoundsFromConfiguration(final String boundsConfiguration) {
        final int xIndex = 0;
        final int yIndex = 1;
        final int widthIndex = 2;
        final int heightIndex = 3;

        final int[] bounds = Arrays.stream(boundsConfiguration.split("[" + BOUNDS_SEPARATOR + "x]"))
            .mapToInt(Integer::parseInt)
            .toArray();

        return new Rectangle(bounds[xIndex], bounds[yIndex], bounds[widthIndex], bounds[heightIndex]);
    }

    /**
     * Get the configuration parameters for an RSS article source.
     *
     * @param rssSource the RSS article source.
     * @return the configuration parameters for an RSS article source.
     */
    private String getRssParameters(final RssArticleSource rssSource) {
        final String authorName = rssSource.getDefaultAuthor() != null
            ? rssSource.getDefaultAuthor().getName()
            : rssSource.getFeedName();

        return RSS_CONFIGURATION_PREFIX + rssSource.getFeedName() + RSS_CONFIGURATION_SEPARATOR + authorName
               + RSS_CONFIGURATION_SEPARATOR + rssSource.getFeedUrl()
               + (rssSource.getCategoryName() != null ? RSS_CONFIGURATION_SEPARATOR + rssSource.getCategoryName() : "");
    }
}

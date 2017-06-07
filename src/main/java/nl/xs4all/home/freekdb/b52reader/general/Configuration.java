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

import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.RssArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.nrc.NrcScienceArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.website.ArticleListFetcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration functionality (most settings are stored in and read from the configuration file).
 */
public class Configuration {
    /**
     * Property key for source-ids: the selected article sources that are configured to be read.
     */
    private static final String SOURCE_IDS_KEY = "source-ids";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

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
     * Initialize by reading the configuration data and filling the <code>selectedArticleSources</code> and
     * <code>allArticleSources</code> lists.
     *
     * @param configurationInputStream the input stream that contains the configuration data.
     */
    public Configuration(InputStream configurationInputStream) throws IOException {
        List<String> sourceIds = new ArrayList<>(Arrays.asList("nrc", "test"));
        allArticleSources = new ArrayList<>();
        selectedArticleSources = new ArrayList<>();
        frameExtendedState = Frame.NORMAL;
        frameBounds = null;

        try {
            Properties configuration = new Properties();

            configuration.load(configurationInputStream);

            String sourceIdsProperty = configuration.getProperty(SOURCE_IDS_KEY, "nrc,test");
            sourceIds.clear();
            sourceIds.addAll(Arrays.asList(sourceIdsProperty.split(",")));

            addConfiguredSources(configuration);

            String windowConfiguration = configuration.getProperty("window-configuration");

            if (windowConfiguration != null) {
                frameExtendedState = windowConfiguration.startsWith("maximized") ? Frame.MAXIMIZED_BOTH : Frame.NORMAL;

                if (windowConfiguration.contains(";")) {
                    String boundsConfiguration = windowConfiguration.substring(windowConfiguration.indexOf(';') + 1);
                    frameBounds = getBoundsFromConfiguration(boundsConfiguration);
                }
            }

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

            configuration.store(configurationOutputStream, Constants.CONFIGURATION_HEADER);
        } catch (IOException e) {
            logger.error("Exception while reading the configuration data.", e);

            result = false;
        }

        return result;
    }

    /**
     * Add the configured article sources to the <code>allArticleSources</code> list.
     *
     * @param configuration the configuration properties.
     */
    private void addConfiguredSources(Properties configuration) {
        String sourcePrefix = "source-";

        Collections.list(configuration.propertyNames()).forEach(name -> {
            String propertyName = (String) name;

            if (propertyName.startsWith(sourcePrefix) && !propertyName.equals(SOURCE_IDS_KEY)) {
                String sourceId = propertyName.substring(sourcePrefix.length());
                String sourceConfiguration = configuration.getProperty(propertyName);

                ArticleSource articleSource = createArticleSource(sourceId, sourceConfiguration);

                if (articleSource != null) {
                    allArticleSources.add(articleSource);
                }
            }
        });
    }

    /**
     * Create an article source object from the source id and the source configuration.
     *
     * @param sourceId            the source id.
     * @param sourceConfiguration the source configuration.
     * @return the new article source object.
     */
    private ArticleSource createArticleSource(String sourceId, String sourceConfiguration) {
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
                    Constructor<?> constructor = sourceClass.getConstructor(ArticleListFetcher.class);
                    String url = Constants.NRC_MAIN_URL + "sectie/wetenschap/";
                    ArticleListFetcher fetcher = new ArticleListFetcher(url, Constants.GET_ARTICLE_LIST_WITH_BROWSER);
                    articleSource = (ArticleSource) constructor.newInstance(fetcher);
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
            Author defaultAuthor = ObjectHub.getPersistencyHandler().getOrCreateAuthor(configurationItems[2]);
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

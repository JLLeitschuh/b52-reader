/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration functionality.
 */
public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    private static List<ArticleSource> allArticleSources;
    private static List<ArticleSource> selectedArticleSources;

    public static List<ArticleSource> getSelectedArticleSources() {
        if (selectedArticleSources == null) {
            initialize();
        }

        return selectedArticleSources;
    }

    private static void initialize() {
        URL configurationUrl = Configuration.class.getClassLoader().getResource("b52-reader.configuration");

        List<String> sourceIds = new ArrayList<>(Arrays.asList("nrc", "test"));
        allArticleSources = new ArrayList<>();

        try {
            Properties configuration = new Properties();

            if (configurationUrl != null) {
                configuration.load(new FileReader(configurationUrl.getFile()));

                String sourceIdsProperty = configuration.getProperty("sourceIds", "nrc,test");
                sourceIds.clear();
                sourceIds.addAll(Arrays.asList(sourceIdsProperty.split(",")));

                addConfiguredSources(configuration);
            }
        } catch (IOException e) {
            logger.error("Exception while reading the configuration file " + configurationUrl, e);
        }

        selectedArticleSources = allArticleSources.stream()
                .filter(articleSource -> sourceIds.contains(articleSource.getSourceId()))
                .collect(Collectors.toList());
    }

    private static void addConfiguredSources(Properties configuration) {
        String sourcePrefix = "source-";

        Collections.list(configuration.propertyNames()).forEach(name -> {
            if (name instanceof String) {
                String propertyName = (String) name;
                if (propertyName.startsWith(sourcePrefix)) {
                    String sourceId = propertyName.substring(sourcePrefix.length());
                    String articleSourceConfiguration = configuration.getProperty(propertyName);

                    ArticleSource articleSource = createArticleSource(sourceId, articleSourceConfiguration);

                    if (articleSource != null) {
                        allArticleSources.add(articleSource);
                    }
                }
            }
        });
    }

    private static ArticleSource createArticleSource(String sourceId, String articleSourceConfiguration) {
        ArticleSource articleSource = null;

        try {
            Object source = null;

            if (articleSourceConfiguration.startsWith("rss|")) {
                String[] configurationItems = articleSourceConfiguration.split("\\|");

                if (configurationItems.length >= 4) {
                    String feedName = configurationItems[1];
                    Author defaultAuthor = ObjectHub.getPersistencyHandler().getOrCreateAuthor(configurationItems[2]);
                    URL feedUrl = new URL(configurationItems[3]);

                    source = new RssArticleSource(sourceId, feedName, defaultAuthor, feedUrl);
                }
            } else {
                Class<?> sourceClass = Class.forName(articleSourceConfiguration);
                source = sourceClass.getConstructor().newInstance();
            }

            if (source instanceof ArticleSource) {
                articleSource = (ArticleSource) source;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                InvocationTargetException | MalformedURLException e) {
            logger.error("Exception while initializing article source " + sourceId + ".", e);
        }

        return articleSource;
    }
}

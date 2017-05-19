/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.io.FileReader;
import java.io.IOException;
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

                String sourcePrefix = "source-";
                Collections.list(configuration.propertyNames()).forEach(name -> {
                    if (name instanceof String) {
                        String propertyName = (String) name;
                        if (propertyName.startsWith(sourcePrefix)) {
                            String sourceId = propertyName.substring(sourcePrefix.length());
                            String articleSourceClassName = configuration.getProperty(propertyName);
                            allArticleSources.add(createArticleSource(sourceId, articleSourceClassName));
                        }
                    }
                });

                // todo: Add these other article sources to the configuration file as well (including parameters).
                allArticleSources.add(new NrcScienceArticleSource());

                allArticleSources.add(new RssArticleSource("acm",
                                                           "ACM Software",
                                                           new URL("https://cacm.acm.org/browse-by-subject/software.rss"),
                                                           new Author(4, "ACM")));

                allArticleSources.add(new RssArticleSource("verge",
                                                           "The Verge",
                                                           new URL("https://www.theverge.com/rss/index.xml"),
                                                           new Author(5, "The Verge")));
            }
        } catch (IOException e) {
            logger.error("Exception while reading the configuration file " + configurationUrl, e);
        }

        selectedArticleSources = allArticleSources.stream()
                .filter(articleSource -> sourceIds.contains(articleSource.getSourceId()))
                .collect(Collectors.toList());
    }

    private static ArticleSource createArticleSource(String sourceId, String articleSourceClassName) {
        ArticleSource articleSource = null;

        try {
            Class<?> sourceClass = Class.forName(articleSourceClassName);
            Object source = sourceClass.getConstructor().newInstance();

            if (source instanceof ArticleSource) {
                articleSource = (ArticleSource) source;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                InvocationTargetException e) {
            logger.error("Exception while initializing article source " + sourceId + ".", e);
        }

        return articleSource;
    }
}

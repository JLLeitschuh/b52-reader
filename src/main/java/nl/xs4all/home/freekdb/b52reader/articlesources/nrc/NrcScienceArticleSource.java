/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources.nrc;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.articlesources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.articlesources.website.ArticleListFetcher;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.RequiredArgsConstructor;

/**
 * Article source for the science section of NRC Handelsblad (a Dutch newspaper).
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@RequiredArgsConstructor
public class NrcScienceArticleSource implements ArticleSource {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Fetcher to get list of science articles from NRC website.
     */
    private final ArticleListFetcher articleListFetcher;

    /**
     * Configuration with settings.
     */
    private final Configuration configuration;

    @Override
    public String getSourceId() {
        return Constants.NRC_SOURCE_ID;
    }

    @Override
    public List<Article> getArticles(final PersistencyHandler persistencyHandler,
                                     final Map<String, Article> previousArticlesMap,
                                     final Map<String, Author> previousAuthorsMap) {
        final List<Article> newArticles = new ArrayList<>();

        final Document articleListDocument = articleListFetcher.getArticleListDocument();

        if (articleListDocument != null) {
            parseArticles(newArticles, articleListDocument, previousArticlesMap, previousAuthorsMap,
                          persistencyHandler.getOrCreateAuthor("NRC science"));
        }

        logger.info("Fetched {} from the NRC website.",
                    Utilities.countAndWord(newArticles.size(), "article"));

        return newArticles;
    }

    /**
     * Parse the fetched html document with science article data and add all articles to the specified list, while using
     * previously found articles and authors.
     *
     * @param newArticles         list to add articles to.
     * @param articleListDocument html document with article data.
     * @param previousArticlesMap previously available articles.
     * @param previousAuthorsMap  previously available authors.
     * @param defaultAuthor       default author to use for NRC science articles.
     */
    private void parseArticles(final List<Article> newArticles, final Document articleListDocument,
                               final Map<String, Article> previousArticlesMap,
                               final Map<String, Author> previousAuthorsMap, final Author defaultAuthor) {
        final Elements articleElements = articleListDocument.select(".nmt-item__link");

        for (final Element articleElement : articleElements) {
            final String url = configuration.getNrcMainUrl() + articleElement.attr("href");
            final String title = articleElement.getElementsByClass("nmt-item__headline").text();
            final ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
            final String text = articleElement.getElementsByClass("nmt-item__teaser").text();

            // We create a new article object even if it is already stored, because we want to be able to compare the
            // articles in memory to the stored article to see whether an update of a stored article is needed.
            final int dummyLikeCount = 1234;
            final Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
            final Article article = Article.builder().url(url).sourceId(Constants.NRC_SOURCE_ID).author(author)
                .title(title).dateTime(zonedDateTime).text(text).likes(dummyLikeCount).recordId(-1 - newArticles.size())
                .build();

            Utilities.copyPreviousDataIfAvailable(article, previousArticlesMap.get(url));

            newArticles.add(article);
        }
    }
}

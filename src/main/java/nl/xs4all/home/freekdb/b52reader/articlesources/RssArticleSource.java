/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Generic article source that fetches data from an rss feed.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@Data
public class RssArticleSource implements ArticleSource {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Source id that identifies this article source.
     */
    private final String sourceId;

    /**
     * RSS feed for reading articles from this source.
     */
    @Getter(AccessLevel.NONE)
    private final SyndFeed feed;

    /**
     * Name of the RSS feed.
     */
    private final String feedName;

    /**
     * Default author name in case no author is found for an article (often equal to the feed name).
     */
    private final String defaultAuthorName;

    /**
     * URL of the RSS feed.
     */
    private final URL feedUrl;

    /**
     * Optional category name that can be used to only get a subset of the RSS feed articles.
     */
    private final String categoryName;

    @Override
    public List<Article> getArticles(final PersistencyHandler persistencyHandler,
                                     final Map<String, Article> previousArticlesMap,
                                     final Map<String, Author> previousAuthorsMap) {
        final List<Article> newArticles = new ArrayList<>();

        for (SyndEntry entry : feed.getEntries()) {
            if (categoryMatches(entry)) {
                newArticles.add(createArticle(entry, -1 - newArticles.size(), previousArticlesMap,
                                              previousAuthorsMap, persistencyHandler));
            }
        }

        logger.info("Fetched {} from the {} rss feed.",
                    Utilities.countAndWord(newArticles.size(), "article"),
                    feedName);

        return newArticles;
    }

    /**
     * Test whether the configured category name is not set or matches one of the category names associated with the
     * specified RSS feed entry.
     *
     * @param entry RSS feed entry to test.
     * @return whether the configured category name is not set or matches the specified RSS feed entry.
     */
    private boolean categoryMatches(final SyndEntry entry) {
        return categoryName == null
               || entry.getCategories().stream().anyMatch(category -> category.getName().equalsIgnoreCase(categoryName));
    }

    /**
     * Create an article for an RSS feed entry.
     *
     * @param entry               RSS feed entry that is the basis for this article.
     * @param articleId           article id to use.
     * @param previousArticlesMap previously available articles.
     * @param previousAuthorsMap  previously available authors.
     * @param persistencyHandler  persistency handler that provides access to database.
     * @return article for an RSS feed entry.
     */
    private Article createArticle(final SyndEntry entry, final int articleId,
                                  final Map<String, Article> previousArticlesMap,
                                  final Map<String, Author> previousAuthorsMap,
                                  final PersistencyHandler persistencyHandler) {
        final String url = entry.getLink();
        final String title = entry.getTitle();

        final String text = entry.getDescription() != null
            ? entry.getDescription().getValue()
            // The Verge: titleEx == title
            // : entry.getTitleEx() != null ? entry.getTitleEx().getValue() : ""
            : "";

        final Author entryAuthor = entry.getAuthor() != null && entry.getAuthor().length() > 0
            ? persistencyHandler.getOrCreateAuthor(entry.getAuthor())
            : null;

        final Date dateTime = entry.getPublishedDate() != null ? entry.getPublishedDate() : new Date();
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC);

        // We create new article objects, because we want to be able to compare the articles in memory to the
        // stored articles to see whether an article is an update of a stored article or a new article.
        final Author author = entryAuthor != null
            ? entryAuthor
            : previousAuthorsMap.getOrDefault(defaultAuthorName, persistencyHandler.getOrCreateAuthor(defaultAuthorName));

        final int dummyLikeCount = 1234;
        final Article article = Article.builder().url(url).sourceId(sourceId).author(author).title(title)
            .dateTime(zonedDateTime).text(text).likes(dummyLikeCount).recordId(articleId)
            .build();

        Utilities.copyPreviousDataIfAvailable(article, previousArticlesMap.get(url));

        return article;
    }
}

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

/**
 * Generic article source that fetches data from an rss feed.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class RssArticleSource implements ArticleSource {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    private final String sourceId;
    private final SyndFeed feed;
    private final String feedName;
    private final String defaultAuthorName;
    private final URL feedUrl;
    private final String categoryName;

    public RssArticleSource(final String sourceId, final SyndFeed feed, final String feedName,
                            final String defaultAuthorName, final URL feedUrl, final String categoryName) {
        this.sourceId = sourceId;
        this.feed = feed;
        this.feedName = feedName;
        this.defaultAuthorName = defaultAuthorName;
        this.feedUrl = feedUrl;
        this.categoryName = categoryName;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    public String getFeedName() {
        return feedName;
    }

    public String getDefaultAuthorName() {
        return defaultAuthorName;
    }

    public URL getFeedUrl() {
        return feedUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public List<Article> getArticles(final PersistencyHandler persistencyHandler,
                                     final Map<String, Article> previousArticlesMap,
                                     final Map<String, Author> previousAuthorsMap) {
        final List<Article> newArticles = new ArrayList<>();

        for (SyndEntry entry : feed.getEntries()) {
            if (categoryMatches(entry)) {
                newArticles.add(createArticle(previousArticlesMap, previousAuthorsMap, persistencyHandler, entry,
                                              -1 - newArticles.size()));
            }
        }

        logger.info("Fetched {} from the {} rss feed.",
                    Utilities.countAndWord(newArticles.size(), "article"),
                    feedName);

        return newArticles;
    }

    private boolean categoryMatches(final SyndEntry entry) {
        return categoryName == null
               || entry.getCategories().stream().anyMatch(category -> category.getName().equalsIgnoreCase(categoryName));
    }

    private Article createArticle(final Map<String, Article> previousArticlesMap,
                                  final Map<String, Author> previousAuthorsMap,
                                  final PersistencyHandler persistencyHandler,
                                  final SyndEntry entry,
                                  final int articleId) {
        final String url = entry.getLink();
        final String title = entry.getTitle();

        final String text = entry.getDescription() != null
            ? entry.getDescription().getValue()
            // The Verge: titleEx == title
            // : entry.getTitleEx() != null ? entry.getTitleEx().getValue() : ""
            : "";

        final Author entryAuthor = entry.getAuthor() != null
            ? persistencyHandler.getOrCreateAuthor(entry.getAuthor())
            : null;

        final Date dateTime = entry.getPublishedDate() != null ? entry.getPublishedDate() : new Date();
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC);

        // We create new article objects, because we want to be able to compare the articles in memory to the
        // stored articles to see whether an update of a stored article is needed.
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

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
    private final Author defaultAuthor;
    private final URL feedUrl;
    private final String categoryName;

    private PersistencyHandler persistencyHandler;

    public RssArticleSource(String sourceId, SyndFeed feed, String feedName, Author defaultAuthor, URL feedUrl,
                            String categoryName) {
        this.sourceId = sourceId;
        this.feed = feed;
        this.feedName = feedName;
        this.defaultAuthor = defaultAuthor;
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

    public Author getDefaultAuthor() {
        return defaultAuthor;
    }

    public URL getFeedUrl() {
        return feedUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public List<Article> getArticles(PersistencyHandler persistencyHandler, Map<String, Article> previousArticlesMap,
                                     Map<String, Author> previousAuthorsMap) {
        this.persistencyHandler = persistencyHandler;
        List<Article> newArticles = new ArrayList<>();

        for (SyndEntry entry : feed.getEntries()) {
            if (categoryMatches(entry)) {
                newArticles.add(createArticle(previousArticlesMap, previousAuthorsMap, entry,
                                              -1 - newArticles.size()));
            }
        }

        logger.info("Fetched {} from the {} rss feed.",
                    Utilities.countAndWord(newArticles.size(), "article"),
                    feedName);

        return newArticles;
    }

    private boolean categoryMatches(SyndEntry entry) {
        return categoryName == null ||
               entry.getCategories().stream().anyMatch(category -> category.getName().equalsIgnoreCase(categoryName));
    }

    private Article createArticle(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap,
                                  SyndEntry entry, int articleId) {
        String url = entry.getLink();
        String title = entry.getTitle();

        String text = entry.getDescription() != null
                ? entry.getDescription().getValue()
                // The Verge: titleEx == title
                // : entry.getTitleEx() != null ? entry.getTitleEx().getValue() : ""
                : "";

        Author entryAuthor = entry.getAuthor() != null
                ? persistencyHandler.getOrCreateAuthor(entry.getAuthor())
                : null;

        Date dateTime = entry.getPublishedDate() != null ? entry.getPublishedDate() : new Date();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC);

        // We create new article objects, because we want to be able to compare the articles in memory to the
        // stored articles to see whether an update of a stored article is needed.
        final Author author = entryAuthor != null
                ? entryAuthor
                : previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);

        final Article article = Article.builder().url(url).sourceId(sourceId).author(author).title(title)
                .dateTime(zonedDateTime).text(text).likes(1234).recordId(articleId)
                .build();

        Utilities.copyPreviousDataIfAvailable(article, previousArticlesMap.get(url));

        return article;
    }
}

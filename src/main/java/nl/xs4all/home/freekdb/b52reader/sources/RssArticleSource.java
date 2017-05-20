/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generic article source that fetches data from an rss feed.
 */
public class RssArticleSource implements ArticleSource {
    private static final Logger logger = LogManager.getLogger(RssArticleSource.class);

    private final String sourceId;
    private final String feedName;
    private final Author defaultAuthor;
    private final URL feedUrl;

    public RssArticleSource(String sourceId, String feedName, Author defaultAuthor, URL feedUrl) {
        this.sourceId = sourceId;
        this.feedName = feedName;
        this.defaultAuthor = defaultAuthor;
        this.feedUrl = feedUrl;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public List<Article> getArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap) {
        List<Article> newArticles = new ArrayList<>();

        try {
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(feedUrl));

            for (SyndEntry entry : feed.getEntries()) {
                String url = entry.getLink();
                String title = entry.getTitle();

                String text = entry.getDescription() != null
                        ? entry.getDescription().getValue()
                        // The Verge: titleEx == title
                        // : entry.getTitleEx() != null ? entry.getTitleEx().getValue() : "";
                        : "";

                Author entryAuthor = entry.getAuthor() != null
                        ? ObjectHub.getPersistencyHandler().getOrCreateAuthor(entry.getAuthor())
                        : null;

                Date dateTime = entry.getPublishedDate() != null ? entry.getPublishedDate() : new Date();

                // We create new article objects, because we want to be able to compare the articles in memory to the
                // stored articles to see whether an update of a stored article is needed.
                Author author = entryAuthor != null
                        ? entryAuthor
                        : previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);

                Article article = new Article(-1 - newArticles.size(), url, null, author, title, dateTime,
                                              text, 1234);

                // If there is previous data available about this article, copy the fields that are managed by the
                // B52 reader.
                if (previousArticlesMap.containsKey(url)) {
                    Article previousArticle = previousArticlesMap.get(url);

                    article.setStarred(previousArticle.isStarred());
                    article.setRead(previousArticle.isRead());
                    article.setArchived(previousArticle.isArchived());
                }

                newArticles.add(article);
            }
        } catch (FeedException | IOException e) {
            logger.error("Exception while fetching articles from an RSS feed.", e);
        }

        logger.info("Fetched {} from the {} rss feed.",
                    Utilities.countAndWord(newArticles.size(), "article"),
                    feedName);

        return newArticles;
    }
}

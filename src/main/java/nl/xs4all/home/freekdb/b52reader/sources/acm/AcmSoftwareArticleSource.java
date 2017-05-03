/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.acm;

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

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Communications of the ACM: Software article source (https://cacm.acm.org/).
 */
public class AcmSoftwareArticleSource implements ArticleSource {
    private static final Logger logger = LogManager.getLogger(AcmSoftwareArticleSource.class);

    @Override
    public List<Article> getArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap) {
        List<Article> newArticles = new ArrayList<>();

        try {
            URL feedUrl = new URL("https://cacm.acm.org/browse-by-subject/software.rss");
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(feedUrl));

            Author defaultAuthor = new Author(4, "ACM");

            for (SyndEntry entry : feed.getEntries()) {
                String url = entry.getLink();
                String title = entry.getTitle();
                String text = entry.getDescription().getValue();
                // The author and date fields seem to be empty for all articles.

                // We create new article objects, because we want to be able to compare the articles in memory to the
                // stored articles to see whether an update of a stored article is needed.
                Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
                Article article = new Article(-1 - newArticles.size(), url, author, title, new Date(), text, 1234);

                // If there is previous data available for this article, copy the fields that are managed by the B52 reader.
                if (previousArticlesMap.containsKey(url)) {
                    Article previousArticle = previousArticlesMap.get(url);

                    article.setStarred(previousArticle.isStarred());
                    article.setRead(previousArticle.isRead());
                    article.setArchived(previousArticle.isArchived());
                }

                newArticles.add(article);
            }
        } catch (FeedException | IOException e) {
            e.printStackTrace();
        }

        logger.info("Fetched {} from the ACM Software rss feed.", Utilities.countAndWord(newArticles.size(), "article"));

        return newArticles;
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.nrc;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.sources.website.ArticleListFetcher;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Article source for the science section of NRC Handelsblad (a Dutch newspaper).
 */
public class NrcScienceArticleSource implements ArticleSource {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    private final ArticleListFetcher articleListFetcher;

    private final Configuration configuration;

    private PersistencyHandler persistencyHandler;

    public NrcScienceArticleSource(ArticleListFetcher articleListFetcher, Configuration configuration) {
        this.articleListFetcher = articleListFetcher;
        this.configuration = configuration;
    }

    @Override
    public String getSourceId() {
        return Constants.NRC_SOURCE_ID;
    }

    @Override
    public List<Article> getArticles(PersistencyHandler persistencyHandler, Map<String, Article> previousArticlesMap,
                                     Map<String, Author> previousAuthorsMap) {
        this.persistencyHandler = persistencyHandler;
        List<Article> newArticles = new ArrayList<>();

        Document articleListDocument = articleListFetcher.getArticleListDocument();

        if (articleListDocument != null) {
            parseArticles(previousArticlesMap, previousAuthorsMap, newArticles, articleListDocument);
        }

        logger.info("Fetched {} from the NRC website.",
                    Utilities.countAndWord(newArticles.size(), "article"));

        return newArticles;
    }

    private void parseArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap,
                               List<Article> newArticles, Document articleListDocument) {
        Elements articleElements = articleListDocument.select(".nmt-item__link");
        Author defaultAuthor = persistencyHandler.getOrCreateAuthor("NRC science");

        for (Element articleElement : articleElements) {
            String url = configuration.getNrcMainUrl() + articleElement.attr("href");
            String title = articleElement.getElementsByClass("nmt-item__headline").text();
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
            String text = articleElement.getElementsByClass("nmt-item__teaser").text();

            // We create a new article object even if it is already stored, because we want to be able to compare the
            // articles in memory to the stored article to see whether an update of a stored article is needed.
            Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
            Article article = new Article.Builder(url, Constants.NRC_SOURCE_ID, author, title, zonedDateTime, text)
                    .likes(1234)
                    .recordId(-1 - newArticles.size())
                    .build();

            Utilities.copyPreviousDataIfAvailable(article, previousArticlesMap.get(url));

            newArticles.add(article);
        }
    }
}

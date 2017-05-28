/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.nrc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
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

    private ArticleListFetcher articleListFetcher;

    public NrcScienceArticleSource(ArticleListFetcher articleListFetcher) {
        this.articleListFetcher = articleListFetcher;
    }

    @Override
    public String getSourceId() {
        return Constants.NRC_SOURCE_ID;
    }

    @Override
    public List<Article> getArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap) {
        List<Article> newArticles = new ArrayList<>();

        Document articleListDocument = articleListFetcher.getArticleListDocument();

        if (articleListDocument != null) {
            parseArticles(previousArticlesMap, previousAuthorsMap, newArticles, articleListDocument);
        }

        logger.info("Fetched {} from the NRC website.", Utilities.countAndWord(newArticles.size(), "article"));

        return newArticles;
    }

    private void parseArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap,
                               List<Article> newArticles, Document articleListDocument) {
        Elements articleElements = articleListDocument.select(".nmt-item__link");
        Author defaultAuthor = ObjectHub.getPersistencyHandler().getOrCreateAuthor("NRC science");

        for (Element articleElement : articleElements) {
            String url = Constants.NRC_MAIN_URL + articleElement.attr("href");
            String title = articleElement.getElementsByClass("nmt-item__headline").text();
            String text = articleElement.getElementsByClass("nmt-item__teaser").text();

            // We create a new article object even if it is already stored, because we want to be able to compare the
            // articles in memory to the stored article to see whether an update of a stored article is needed.
            Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
            Article article = new Article(-1 - newArticles.size(), url, Constants.NRC_SOURCE_ID, author, title, new Date(),
                                          text, 1234);

            Utilities.copyPreviousDataIfAvailable(article, previousArticlesMap.get(url));

            newArticles.add(article);
        }
    }
}

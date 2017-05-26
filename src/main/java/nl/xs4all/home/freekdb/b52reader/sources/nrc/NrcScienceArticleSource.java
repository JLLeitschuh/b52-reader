/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.nrc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Article source for the science section of NRC Handelsblad (a Dutch newspaper).
 */
public class NrcScienceArticleSource implements ArticleSource {
    private static final String SOURCE_ID = "nrc";
    private static final String MAIN_NRC_URL = "https://www.nrc.nl/";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger(NrcScienceArticleSource.class);

    @Override
    public String getSourceId() {
        return SOURCE_ID;
    }

    @Override
    public List<Article> getArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap) {
        boolean useBackgroundBrowser = !previousArticlesMap.isEmpty();
    
        return getArticlesWithJsoupConnect(useBackgroundBrowser, previousArticlesMap, previousAuthorsMap);
    }

    private List<Article> getArticlesWithJsoupConnect(boolean useBackgroundBrowser,
                                                      Map<String, Article> previousArticlesMap,
                                                      Map<String, Author> previousAuthorsMap) {
        List<Article> newArticles = new ArrayList<>();
        Document articleListDocument = null;

        try {
            if (useBackgroundBrowser) {
                String htmlContent = ObjectHub.getBackgroundBrowsers().getHtmlContent(MAIN_NRC_URL + "sectie/wetenschap/");

                if (htmlContent != null) {
                    articleListDocument = Jsoup.parse(htmlContent);
                }
            }
            else {
                articleListDocument = Jsoup.connect(MAIN_NRC_URL + "sectie/wetenschap/").get();
            }
        } catch (IOException e) {
            logger.error("Exception while fetching articles from web site.", e);
        }

        if (articleListDocument != null) {
            Elements articleElements = articleListDocument.select(".nmt-item__link");

            Author defaultAuthor = ObjectHub.getPersistencyHandler().getOrCreateAuthor("NRC science");

            for (Element articleElement : articleElements) {
                String url = MAIN_NRC_URL + articleElement.attr("href");
                String title = articleElement.getElementsByClass("nmt-item__headline").text();
                String text = articleElement.getElementsByClass("nmt-item__teaser").text();

                // We create new article objects, because we want to be able to compare the articles in memory to the
                // stored articles to see whether an update of a stored article is needed.
                Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
                Article article = new Article(-1 - newArticles.size(), url, SOURCE_ID, author, title, new Date(),
                                              text, 1234);

                Utilities.copyPreviousDataIfAvailable(article, previousArticlesMap.get(url));

                newArticles.add(article);
            }
        }

        logger.info("Fetched {} from the NRC website.", Utilities.countAndWord(newArticles.size(), "article"));

        return newArticles;
    }

}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.website;

import java.io.IOException;

import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Fetcher for getting the html from a website with a list of articles for an article source.
 */
public class ArticleListFetcher {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    private String url;
    private boolean useBackgroundBrowser;

    public ArticleListFetcher(String url, boolean useBackgroundBrowser) {
        this.url = url;
        this.useBackgroundBrowser = useBackgroundBrowser;
    }

    public Document getArticleListDocument() {
        Document articleListDocument = null;

        try {
            if (useBackgroundBrowser) {
                String htmlContent = ObjectHub.getBackgroundBrowsers().getHtmlContent(url);

                if (htmlContent != null) {
                    articleListDocument = Jsoup.parse(htmlContent);
                }
            }
            else {
                articleListDocument = Jsoup.connect(url).get();
            }
        } catch (IOException e) {
            logger.error("Exception while fetching list of articles from web site " + url + ".", e);
        }

        return articleListDocument;
    }
}

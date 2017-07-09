/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources.website;

import java.io.IOException;

import nl.xs4all.home.freekdb.b52reader.browsers.BackgroundBrowsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

/**
 * Fetcher for getting the html from a website with a list of articles for an article source.
 */
public class ArticleListFetcher {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The url of the website with a list of articles.
     */
    private final String url;

    /**
     * Whether it is necessary to use a background browser (true; for sites with dynamically generated html) or whether
     * Jsoup can directly get the html from the site.
     */
    private final boolean useBackgroundBrowser;

    /**
     * The background browsers object to use when <code>useBackgroundBrowser</code> is <code>true</code> (it can be
     * <code>null</code> otherwise).
     */
    private final BackgroundBrowsers backgroundBrowsers;

    /**
     * The html helper for getting and parsing html.
     */
    private final HtmlHelper htmlHelper;

    /**
     * Construct a fetcher for the specified URL using the specified background browser setting.
     *
     * @param url                  the url of the website with a list of articles.
     * @param useBackgroundBrowser whether it is necessary to use a background browser (true; for sites with dynamically
     *                             generated html) or whether Jsoup can directly get the html from the site.
     * @param backgroundBrowsers   the background browsers object to use when <code>useBackgroundBrowser</code> is
     *                             <code>true</code> (it can be <code>null</code> otherwise).
     * @param htmlHelper           the html helper for getting and parsing html.
     */
    public ArticleListFetcher(String url, boolean useBackgroundBrowser, BackgroundBrowsers backgroundBrowsers,
                              HtmlHelper htmlHelper) {
        this.url = url;
        this.useBackgroundBrowser = useBackgroundBrowser;
        this.backgroundBrowsers = backgroundBrowsers;
        this.htmlHelper = htmlHelper;
    }

    /**
     * Get the Jsoup document with the list of article elements.
     *
     * @return the Jsoup document with the list of article elements.
     */
    public Document getArticleListDocument() {
        Document articleListDocument = null;

        try {
            if (useBackgroundBrowser) {
                String htmlContent = backgroundBrowsers.getHtmlContent(url);

                if (htmlContent != null) {
                    articleListDocument = htmlHelper.parseHtml(htmlContent);
                }
            } else {
                articleListDocument = htmlHelper.getHtmlAsDocument(url);
            }
        } catch (IOException e) {
            logger.error("Exception while fetching list of articles from web site " + url + ".", e);
        }

        return articleListDocument;
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources.website;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * This class uses the Jsoup library to provide some html fetching functions. Having these methods in non static form
 * improves testability.
 */
public class HtmlHelper {
    /**
     * Parse the provided html content into a Jsoup document.
     *
     * @param htmlContent the html content to parse.
     * @return the parsed Jsoup document.
     */
    public Document parseHtml(String htmlContent) {
        return Jsoup.parse(htmlContent);
    }

    /**
     * Get the html content from the specified URL and parse it into a Jsoup document.
     *
     * @param url the URL to get the html content from.
     * @return the parsed Jsoup document.
     * @throws IOException when getting the html content fails.
     */
    public Document getHtmlAsDocument(String url) throws IOException {
        return Jsoup.connect(url).get();
    }
}

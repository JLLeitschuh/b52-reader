/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources;

import java.io.IOException;

import nl.xs4all.home.freekdb.b52reader.browsers.BackgroundBrowsers;
import nl.xs4all.home.freekdb.b52reader.articlesources.website.ArticleListFetcher;
import nl.xs4all.home.freekdb.b52reader.articlesources.website.HtmlHelper;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ArticleListFetcherTest {
    private static final String URL = "https://www.nrc.nl/sectie/wetenschap";

    @Test
    public void testGetArticleListDocumentDirectly() throws IOException {
        HtmlHelper mockHtmlHelper = Mockito.mock(HtmlHelper.class);
        int articleCount = 28;

        Mockito.when(mockHtmlHelper.getHtmlAsDocument(Mockito.anyString()))
                .thenReturn(new HtmlHelper().parseHtml(getArticleListHtml(articleCount)));

        Document articleListDocument
                = new ArticleListFetcher(URL, false, null, mockHtmlHelper)
                .getArticleListDocument();

        Elements articleElements = articleListDocument.select(".nmt-item__link");

        assertEquals(articleCount, articleElements.size());
    }

    @Test
    public void testGetArticleListDocumentViaBrowser() {
        BackgroundBrowsers mockBackgroundBrowsers = Mockito.mock(BackgroundBrowsers.class);
        HtmlHelper htmlHelper = new HtmlHelper();
        int articleCount = 6;
        String articleListHtml = getArticleListHtml(articleCount);

        Mockito.when(mockBackgroundBrowsers.getHtmlContent(Mockito.anyString())).thenReturn(articleListHtml);

        Document articleListDocument
                = new ArticleListFetcher(URL, true, mockBackgroundBrowsers, htmlHelper)
                .getArticleListDocument();

        Elements articleElements = articleListDocument.select(".nmt-item__link");

        assertEquals(articleCount, articleElements.size());
    }

    @Test
    public void testGetArticleListDocumentViaBrowserHtmlNull() {
        BackgroundBrowsers mockBackgroundBrowsers = Mockito.mock(BackgroundBrowsers.class);
        HtmlHelper htmlHelper = new HtmlHelper();

        Mockito.when(mockBackgroundBrowsers.getHtmlContent(Mockito.anyString())).thenReturn(null);

        Document articleListDocument
                = new ArticleListFetcher(URL, true, mockBackgroundBrowsers, htmlHelper)
                .getArticleListDocument();

        assertNull(articleListDocument);
    }

    @Test
    public void testGetArticleListDocumentException() throws IOException {
        HtmlHelper mockHtmlHelper = Mockito.mock(HtmlHelper.class);

        Mockito.when(mockHtmlHelper.getHtmlAsDocument(Mockito.anyString()))
                .thenThrow(new IOException("Something went wrong getting the html."));

        Document articleListDocument
                = new ArticleListFetcher(URL, false, null, mockHtmlHelper)
                .getArticleListDocument();

        assertNull(articleListDocument);
    }

    private String getArticleListHtml(int articleCount) {
        StringBuilder htmlBuilder = new StringBuilder("<html><body>");

        for (int articleIndex = 0; articleIndex < articleCount; articleIndex++) {
            htmlBuilder.append("<p class=\"nmt-item__link\"/>");
        }

        htmlBuilder.append("</body></html>");

        return htmlBuilder.toString();
    }
}

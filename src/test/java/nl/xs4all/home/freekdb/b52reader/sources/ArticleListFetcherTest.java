/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources;

import nl.xs4all.home.freekdb.b52reader.sources.website.ArticleListFetcher;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArticleListFetcherTest {
    @Test
    public void testGetArticleListDocument() {
        Document articleListDocument = new ArticleListFetcher("https://www.nrc.nl/sectie/wetenschap",
                                                              false)
                .getArticleListDocument();

        Elements articleElements = articleListDocument.select(".nmt-item__link");

        // todo: Make this test less reliant on external factors.
        assertEquals(29, articleElements.size());
    }
}

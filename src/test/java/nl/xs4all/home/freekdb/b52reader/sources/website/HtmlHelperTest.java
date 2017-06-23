/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.website;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link HtmlHelper} class.
 */
public class HtmlHelperTest {
    @Test
    public void testGetHtmlAsDocumentAndParseHtml() throws IOException {
        String url = "https://freekdb.home.xs4all.nl/index.html";
        HtmlHelper htmlHelper = new HtmlHelper();

        Document document1 = htmlHelper.getHtmlAsDocument(url);
        Document document2 = htmlHelper.parseHtml(getHtmlContent(url));

        // Note: the Document.equals (= Node.equals) checks for instance equality, which is a bit too much here.
        assertEquals(document1.toString(), document2.toString());
    }

    private String getHtmlContent(String url) throws IOException {
        URLConnection urlConnection = new URL(url).openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder htmlContent = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            htmlContent.append(line);
            htmlContent.append("\n");
        }

        bufferedReader.close();

        return htmlContent.toString();
    }
}

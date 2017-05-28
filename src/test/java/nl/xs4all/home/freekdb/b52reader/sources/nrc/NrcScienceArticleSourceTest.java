/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.nrc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.website.ArticleListFetcher;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class NrcScienceArticleSourceTest {
    @Test
    public void testGetSourceId() {
        NrcScienceArticleSource nrcScienceArticleSource = new NrcScienceArticleSource(null);

        assertEquals(Constants.NRC_SOURCE_ID, nrcScienceArticleSource.getSourceId());
    }

    @Test
    public void testGetArticlesNullDocument() {
        ArticleListFetcher mockFetcher = Mockito.mock(ArticleListFetcher.class);
        Mockito.when(mockFetcher.getArticleListDocument()).thenReturn(null);

        NrcScienceArticleSource nrcScienceArticleSource = new NrcScienceArticleSource(mockFetcher);
        List<Article> articles = nrcScienceArticleSource.getArticles(new HashMap<>(), new HashMap<>());

        assertEquals(0, articles.size());
    }

    @Test
    public void testGetArticlesNormal() {
        PersistencyHandler mockedPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Author testAuthor = new Author(6, "Test Author");
        Mockito.when(mockedPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(testAuthor);
        ObjectHub.injectPersistencyHandler(mockedPersistencyHandler);

        ArticleListFetcher mockFetcher = Mockito.mock(ArticleListFetcher.class);
        Mockito.when(mockFetcher.getArticleListDocument()).thenReturn(prepareArticleListDocument());

        NrcScienceArticleSource nrcScienceArticleSource = new NrcScienceArticleSource(mockFetcher);
        List<Article> actualArticles = nrcScienceArticleSource.getArticles(new HashMap<>(), new HashMap<>());

        assertEquals(prepareExpectedArticles(testAuthor, actualArticles), actualArticles);
    }

    private Document prepareArticleListDocument() {
        Document articleListDocument = new Document(Constants.NRC_MAIN_URL);

        createArticle(articleListDocument, "article-1", "title-1", "text-1");
        createArticle(articleListDocument, "article-2", "title-2", "text-2");

        return articleListDocument;
    }

    private void createArticle(Document articleListDocument, String relativeUrl, String title, String text) {
        Element article = articleListDocument.appendElement("article").addClass("nmt-item__link");
        article.attr("href", relativeUrl);
        article.appendElement("title").addClass("nmt-item__headline").text(title);
        article.appendElement("text").addClass("nmt-item__teaser").text(text);
    }

    // todo: Pass a Clock object to the NrcScienceArticleSource class instead of copying date/times here.
    private List<Article> prepareExpectedArticles(Author testAuthor, List<Article> actualArticles) {
        Article article1 = new Article(-1, Constants.NRC_MAIN_URL + "article-1", "nrc", testAuthor,
                                       "title-1", actualArticles.get(0).getDateTime(), "text-1", 1234);

        Article article2 = new Article(-2, Constants.NRC_MAIN_URL + "article-2", "nrc", testAuthor,
                                       "title-2", actualArticles.get(1).getDateTime(), "text-2", 1234);

        return new ArrayList<>(Arrays.asList(article1, article2));
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources.nrc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.articlesources.website.ArticleListFetcher;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class NrcScienceArticleSourceTest {
    private Configuration configuration;

    @Before
    public void setUp() throws IOException {
        byte[] configurationLinesBytes = "".getBytes("UTF-8");

        configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes));
    }

    @Test
    public void testGetSourceId() {
        NrcScienceArticleSource nrcScienceArticleSource = new NrcScienceArticleSource(null, configuration);

        assertEquals(Constants.NRC_SOURCE_ID, nrcScienceArticleSource.getSourceId());
    }

    @Test
    public void testGetArticlesNullDocument() {
        ArticleListFetcher mockFetcher = Mockito.mock(ArticleListFetcher.class);
        Mockito.when(mockFetcher.getArticleListDocument()).thenReturn(null);

        NrcScienceArticleSource nrcScienceArticleSource = new NrcScienceArticleSource(mockFetcher, configuration);

        List<Article> articles = nrcScienceArticleSource.getArticles(Mockito.mock(PersistencyHandler.class),
                                                                     new HashMap<>(), new HashMap<>());

        assertEquals(0, articles.size());
    }

    @Test
    public void testGetArticlesNormal() {
        Author testAuthor = new Author("Test Author", 6);

        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(testAuthor);

        ArticleListFetcher mockFetcher = Mockito.mock(ArticleListFetcher.class);
        Mockito.when(mockFetcher.getArticleListDocument()).thenReturn(prepareArticleListDocument());

        NrcScienceArticleSource nrcScienceArticleSource = new NrcScienceArticleSource(mockFetcher, configuration);

        List<Article> actualArticles = nrcScienceArticleSource.getArticles(mockPersistencyHandler,
                                                                           new HashMap<>(), new HashMap<>());

        assertEquals(prepareExpectedArticles(testAuthor, actualArticles), actualArticles);
    }

    private Document prepareArticleListDocument() {
        Document articleListDocument = new Document(configuration.getNrcMainUrl());

        addArticleElement(articleListDocument, "article-1", "title-1", "text-1");
        addArticleElement(articleListDocument, "article-2", "title-2", "text-2");

        return articleListDocument;
    }

    private void addArticleElement(Document articleListDocument, String relativeUrl, String title, String text) {
        Element articleElement = articleListDocument.appendElement("article").addClass("nmt-item__link");
        articleElement.attr("href", relativeUrl);
        articleElement.appendElement("title").addClass("nmt-item__headline").text(title);
        articleElement.appendElement("text").addClass("nmt-item__teaser").text(text);
    }

    // todo: Pass a Clock object to the NrcScienceArticleSource class instead of copying date/times here.
    private List<Article> prepareExpectedArticles(Author testAuthor, List<Article> actualArticles) {
        Article article1 = Article.builder().url(configuration.getNrcMainUrl() + "article-1").sourceId("nrc")
                .author(testAuthor).title("title-1").dateTime(actualArticles.get(0).getDateTime()).text("text-1")
                .likes(1234).recordId(-1)
                .build();

        Article article2 = Article.builder().url(configuration.getNrcMainUrl() + "article-2").sourceId("nrc")
                .author(testAuthor).title("title-2").dateTime(actualArticles.get(1).getDateTime()).text("text-2")
                .likes(1234).recordId(-2)
                .build();

        return new ArrayList<>(Arrays.asList(article1, article2));
    }
}

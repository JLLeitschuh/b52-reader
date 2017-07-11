/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources.testdata;

import java.util.List;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class TestDataArticleSourceTest {
    @Test
    public void testGetSourceId() {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(null);

        TestDataArticleSource testDataArticleSource = new TestDataArticleSource();

        assertEquals("test", testDataArticleSource.getSourceId());
    }

    @Test
    public void testGetArticles() {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(null);

        TestDataArticleSource testDataArticleSource = new TestDataArticleSource();

        List<Article> articles = testDataArticleSource.getArticles(mockPersistencyHandler, null,
                                                                   null);

        String expectedArticle1 =
                "Article(url=http://www.huffingtonpost.com/2012/12/17/superstring-theory_n_2296195.html, " +
                "sourceId=test, author=null, title=WTF Is String Theory?, normalizedTitle=wtf is string theory?, " +
                "titleWordCount=4, dateTime=2012-12-17T00:00Z, text=Have you ever heard the term string theory " +
                "and wondered WTF it means? When it comes to theoretical physics, it seems like there are a lot " +
                "of larger-than-life concepts that have made their way into our everyday conversations., " +
                "textWordCount=38, likes=28, recordId=1, starred=false, read=false, archived=false)";

        String expectedArticle2 =
                "Article(url=http://www.haydenplanetarium.org/tyson/read/2007/04/02/the-cosmic-perspective, " +
                "sourceId=test, author=null, title=The Cosmic Perspective, normalizedTitle=the cosmic perspective, " +
                "titleWordCount=3, dateTime=2007-04-02T00:00Z, text=Long before anyone knew that the universe had a " +
                "beginning, before we knew that the nearest large galaxy lies two and a half million light-years " +
                "from Earth, before we knew how stars work or whether atoms exist, James Ferguson's enthusiastic " +
                "introduction to his favorite science rang true., textWordCount=47, likes=6, recordId=2, " +
                "starred=false, read=false, archived=false)";

        assertEquals(2, articles.size());
        assertEquals(expectedArticle1, articles.get(0).toString());
        assertEquals(expectedArticle2, articles.get(1).toString());
    }
}

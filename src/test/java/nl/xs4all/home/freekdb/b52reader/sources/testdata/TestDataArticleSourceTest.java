/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.testdata;

import java.util.List;

import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class TestDataArticleSourceTest {
    @Test
    public void testGetSourceId() {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(null);
        ObjectHub.injectPersistencyHandler(mockPersistencyHandler);

        TestDataArticleSource testDataArticleSource = new TestDataArticleSource();

        assertEquals("test", testDataArticleSource.getSourceId());
    }

    @Test
    public void testGetArticles() {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(Mockito.anyString())).thenReturn(null);
        ObjectHub.injectPersistencyHandler(mockPersistencyHandler);

        TestDataArticleSource testDataArticleSource = new TestDataArticleSource();
        List<Article> articles = testDataArticleSource.getArticles(null, null);

        assertEquals(2, articles.size());
        assertEquals("[1] Article \"WTF Is String Theory?\" by null", articles.get(0).toString());
        assertEquals("[2] Article \"The Cosmic Perspective\" by null", articles.get(1).toString());
    }
}

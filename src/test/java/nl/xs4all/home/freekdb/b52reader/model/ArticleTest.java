/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model;

import java.time.Month;
import java.util.Date;

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArticleTest {
    @Test
    public void testGetters() {
        String url = "www.test.org";
        Date date = Utilities.createDate(2000, Month.JANUARY, 1);

        Article article = new Article(6, url, "test", null, "Title",
                                      date, "text", 1024);

        assertEquals(6, article.getId());
        assertEquals(url, article.getUrl());
        assertEquals("test", article.getSourceId());
        assertNull(article.getAuthor());
        assertEquals("Title", article.getTitle());
        assertEquals("title", article.getNormalizedTitle());
        assertEquals(date, article.getDateTime());
        assertEquals("text", article.getText());
        assertEquals(2, article.getWordCount());
        assertEquals(1024, article.getLikes());
        assertFalse(article.isStarred());
        assertFalse(article.isRead());
        assertFalse(article.isArchived());
    }

    @Test
    public void testSetters() {
        String url = "www.test.org";
        Date date1 = Utilities.createDate(2000, Month.JANUARY, 1);
        Date date2 = Utilities.createDate(2000, Month.JANUARY, 1);

        Article article = new Article(6, url, "test", null, "Title",
                                      date1, "text", 1024);

        article.setId(28);
        article.setUrl("g.cn");
        article.setDateTime(date2);
        article.setStarred(true);
        article.setRead(true);
        article.setArchived(true);

        assertEquals(28, article.getId());
        assertEquals("g.cn", article.getUrl());
        assertEquals(date2, article.getDateTime());

        assertTrue(article.isStarred());
        assertTrue(article.isRead());
        assertTrue(article.isArchived());
    }

    // createArticleFromDatabase
    // equals
    // hashCode
}

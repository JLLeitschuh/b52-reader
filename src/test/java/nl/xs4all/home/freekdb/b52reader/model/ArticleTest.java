/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArticleTest {
    @Test
    public void testGetters() {
        String url = "www.test.org";
        Date date = Utilities.createDate(2000, Month.JANUARY, 1);

        Article article = new Article(url, "test", null, "Title", date, "text", 1024,
                                      6);

        assertArticle(article, url, null, date, false, false);
    }

    @Test
    public void testSetters() {
        String url = "www.test.org";
        Date date = Utilities.createDate(2000, Month.JANUARY, 1);

        Article article = new Article(url, "test", null, "Title", date, "text", 1024,
                                      6);

        article.setRecordId(28);
        article.setStarred(true);
        article.setRead(true);
        article.setArchived(true);

        assertEquals(28, article.getRecordId());
        assertTrue(article.isStarred());
        assertTrue(article.isRead());
        assertTrue(article.isArchived());
    }

    @Test
    public void testCreateArticleFromDatabaseNoAuthor() throws SQLException {
        Date date = Utilities.createDate(2000, Month.JANUARY, 1);
        ResultSet mockResultSet = prepareResultSet(-1, date);

        Article article = Article.createArticleFromDatabase(mockResultSet, new ArrayList<>());

        assertArticle(article, "g.cn", null, date, true, true);
    }

    @Test
    public void testCreateArticleFromDatabaseWithAuthor() throws SQLException {
        Date date = Utilities.createDate(2000, Month.JANUARY, 1);
        ResultSet mockResultSet = prepareResultSet(2, date);

        Author author = new Author(2, "Cara Santa Maria");
        List<Author> authors = Arrays.asList(new Author(1, "Patrick Süskind"), author);

        Article article = Article.createArticleFromDatabase(mockResultSet, authors);

        assertArticle(article, "g.cn", author, date, true, true);
    }

    @Test
    public void testCreateArticleFromDatabaseException() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        SQLException exception = new SQLException("Something exploded in the database!");
        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenThrow(exception);

        Article article = Article.createArticleFromDatabase(mockResultSet, new ArrayList<>());

        assertNull(article);
    }

    private ResultSet prepareResultSet(int authorId, Date date) throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(authorId, 1024, 6);
        Mockito.when(mockResultSet.getString(Mockito.anyString())).thenReturn("g.cn", "test", "Title", "text");
        Mockito.when(mockResultSet.getTimestamp(Mockito.anyString())).thenReturn(new Timestamp(date.getTime()));
        Mockito.when(mockResultSet.getBoolean(Mockito.anyString())).thenReturn(true, false, true);

        return mockResultSet;
    }

    private void assertArticle(Article article, String url, Author author, Date date, boolean starred, boolean archived) {
        assertEquals(url, article.getUrl());
        assertEquals("test", article.getSourceId());
        assertEquals(author, article.getAuthor());
        assertEquals("Title", article.getTitle());
        assertEquals("title", article.getNormalizedTitle());
        assertEquals(date, article.getDateTime());
        assertEquals("text", article.getText());
        assertEquals(2, article.getWordCount());
        assertEquals(1024, article.getLikes());
        assertEquals(6, article.getRecordId());
        assertEquals(starred, article.isStarred());
        assertFalse(article.isRead());
        assertEquals(archived, article.isArchived());
    }

    @Test
    public void testMetadataEquals() {
        Article article1 = new Article("url", "test", null, "Title", null, "text",
                                       1024, 6);

        Article article2 = new Article("url", "test", null, "Title", null, "text",
                                       1024, 6);

        assertTrue(article1.metadataEquals(article2));

        article2.setArchived(true);
        assertFalse(article1.metadataEquals(article2));
        article2.setArchived(false);

        article2.setRead(true);
        assertFalse(article1.metadataEquals(article2));
        article2.setRead(false);

        article2.setStarred(true);
        assertFalse(article1.metadataEquals(article2));
    }

    @Test
    public void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Article.class)
                .withIgnoredFields("normalizedTitle", "titleWordCount", "textWordCount", "recordId",
                                   "starred", "read", "archived")
                .withPrefabValues(Author.class,
                                  new Author(1, "Cara Santa Maria"),
                                  new Author(2, "Neil deGrasse Tyson"))
                .verify();
    }
}

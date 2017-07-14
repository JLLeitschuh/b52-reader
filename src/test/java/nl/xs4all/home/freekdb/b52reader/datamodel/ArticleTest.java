/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.general.Utilities;

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
        ZonedDateTime date = Utilities.createDate(2000, Month.JANUARY, 1);

        Article article = Article.builder().url(url).sourceId("test").title("Title").dateTime(date).text("text")
                .likes(1024).recordId(6)
                .build();

        assertArticle(article, url, null, date, false, false);
    }

    @Test
    public void testSetters() {
        String url = "www.test.org";
        ZonedDateTime date = Utilities.createDate(2000, Month.JANUARY, 1);

        Article article = Article.builder().url(url).sourceId("test").title("Title").dateTime(date).text("text")
                .likes(1024).recordId(6)
                .build();


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
        ZonedDateTime date = Utilities.createDate(2000, Month.JANUARY, 1);
        ResultSet mockResultSet = prepareResultSet(-1, date);

        Article article = Article.createArticleFromDatabase(mockResultSet, new ArrayList<>());

        assertArticle(article, "g.cn", null, date, true, true);
    }

    @Test
    public void testCreateArticleFromDatabaseWithAuthor() throws SQLException {
        ZonedDateTime date = Utilities.createDate(2000, Month.JANUARY, 1);
        ResultSet mockResultSet = prepareResultSet(2, date);

        Author author = new Author("Cara Santa Maria", 2);
        List<Author> authors = Arrays.asList(new Author("Patrick Süskind", 1), author);

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

    private ResultSet prepareResultSet(int authorId, ZonedDateTime date) throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(authorId, 1024, 6);
        Mockito.when(mockResultSet.getString(Mockito.anyString())).thenReturn("g.cn", "test", "Title", "text");
        Mockito.when(mockResultSet.getTimestamp(Mockito.anyString())).thenReturn(Timestamp.from(date.toInstant()));
        Mockito.when(mockResultSet.getBoolean(Mockito.anyString())).thenReturn(true, false, true);

        return mockResultSet;
    }

    private void assertArticle(Article article, String url, Author author, ZonedDateTime date, boolean starred,
                               boolean archived) {
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
        Article article1 = Article.builder().url("url").sourceId("test").title("Title").text("text").likes(1024)
                .recordId(6)
                .build();

        Article article2 = Article.builder().url("url").sourceId("test").title("Title").text("text").likes(1024)
                .recordId(6)
                .build();

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
        // todo: Find out whether EqualsVerifier and Lombok can cooperate.
//        EqualsVerifier.forClass(Article.class)
//                .withIgnoredFields("normalizedTitle", "titleWordCount", "textWordCount", "recordId",
//                                   "starred", "read", "archived")
//                .verify();

        Article article1 = Article.builder().url("url").build();
        Article article2 = Article.builder().url("url").starred(true).build();

        assertTrue(article1.equals(article2));
        assertFalse(article1.metadataEquals(article2));
    }
}

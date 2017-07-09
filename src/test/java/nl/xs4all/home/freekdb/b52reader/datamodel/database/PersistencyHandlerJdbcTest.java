/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel.database;

import com.google.common.collect.ImmutableSet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PersistencyHandlerJdbcTest {
    private Connection mockDatabaseConnection;
    private PersistencyHandler persistencyHandler;
    private Statement mockStatement;
    private Author author;
    private Article article;

    @Before
    public void setUp() throws SQLException {
        mockDatabaseConnection = Mockito.mock(Connection.class);
        mockStatement = Mockito.mock(Statement.class);

        Mockito.when(mockDatabaseConnection.createStatement()).thenReturn(mockStatement);

        persistencyHandler = new PersistencyHandlerJdbc();
    }

    @Test
    public void testInitializeDatabaseConnectionSuccessful() {
        assertTrue(persistencyHandler.initializeDatabaseConnection(mockDatabaseConnection));
    }

    @Test
    public void testInitializeDatabaseConnectionWithException() throws SQLException {
        Mockito.when(mockDatabaseConnection.createStatement()).thenThrow(new SQLException("Create statement failed."));

        assertFalse(persistencyHandler.initializeDatabaseConnection(mockDatabaseConnection));
    }

    @Test
    public void testCreateTablesIfNeeded() throws SQLException {
        DatabaseMetaData mockDatabaseMetaData = Mockito.mock(DatabaseMetaData.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(mockDatabaseConnection.getMetaData()).thenReturn(mockDatabaseMetaData);

        Mockito.when(mockDatabaseMetaData.getTables(Mockito.isNull(), Mockito.isNull(),
                                                    Mockito.anyString(), Mockito.isNull()))
                .thenReturn(mockResultSet);

        assertTrue(persistencyHandler.initializeDatabaseConnection(mockDatabaseConnection));

        persistencyHandler.createTablesIfNeeded();

        Collection<Invocation> statementInvocations = Mockito.mockingDetails(mockStatement).getInvocations();
        assertEquals(2, statementInvocations.size());

        Set<String> expectedTables = ImmutableSet.of("article", "author");
        assertEquals(expectedTables, getCreatedTables(statementInvocations, expectedTables));
    }

    private Set<String> getCreatedTables(Collection<Invocation> statementInvocations, Set<String> expectedTables) {
        Set<String> createdTables = new HashSet<>();

        statementInvocations.forEach(invocation -> {
            String invocationString = invocation.toString();

            if (invocationString.contains("execute")) {
                expectedTables.forEach(tableName -> {
                    if (invocationString.contains("create table " + tableName)) {
                        createdTables.add(tableName);
                    }
                });
            }
        });

        return createdTables;
    }

    @Test
    public void testReadAuthorsAndArticles() throws SQLException {
        createConnectionAndRelatedMocks();

        persistencyHandler.readAuthorsAndArticles();

        assertEquals(Collections.singletonList(author),
                     new ArrayList<>(persistencyHandler.getStoredAuthorsMap().values()));

        assertEquals(Collections.singletonList(article),
                     new ArrayList<>(persistencyHandler.getStoredArticlesMap().values()));
    }

    @Test
    public void testGetOrCreateAuthor() throws SQLException {
        createConnectionAndRelatedMocks();

        persistencyHandler.readAuthorsAndArticles();

        assertEquals(author, persistencyHandler.getOrCreateAuthor(author.getName()));
    }

    @Test
    public void testSaveAuthorsAndArticles() throws SQLException {
        createConnectionAndRelatedMocks();

        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(mockDatabaseConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1});

        persistencyHandler.readAuthorsAndArticles();

        String newAuthorName = "Patrick Süskind";
        int newAuthorId = 496;
        Author newAuthor = new Author(newAuthorName, newAuthorId);

        Article existingArticle =
                new Article.Builder("generic string value", "source-id",
                                    new Author("Cara Santa Maria", 28), "title",
                                    Utilities.createDate(2017, Month.JUNE, 28), "text").build();

        Article newlyFetchedArticle =
                new Article.Builder("url2", "source-id", newAuthor, "title",
                                    Utilities.createDate(2017, Month.JUNE, 28), "text").build();

        ArrayList<Article> currentArticles = new ArrayList<>(Arrays.asList(existingArticle, newlyFetchedArticle));

        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);

        Mockito.when(mockResultSet.next()).thenReturn(true, false);
        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(newAuthorId);
        Mockito.when(mockResultSet.getString(Mockito.anyString())).thenReturn(newAuthorName);

        persistencyHandler.saveAuthorsAndArticles(currentArticles);

        List<Invocation> setStringInvocations = Mockito.mockingDetails(mockPreparedStatement)
                .getInvocations().stream().filter(invocation -> invocation.toString().contains("setString"))
                .collect(Collectors.toList());

        assertEquals(9, setStringInvocations.size());

        Optional<Invocation> optionalAuthorInvocation = setStringInvocations.stream()
                .filter(invocation -> invocation.toString().contains("1") && invocation.toString().contains("Patrick"))
                .findFirst();

        assertTrue(optionalAuthorInvocation.isPresent());
        assertTrue(optionalAuthorInvocation.get().toString().contains(newAuthorName));
    }

    @Test
    public void testCloseDatabaseConnectionSuccessful() throws SQLException {
        createConnectionAndRelatedMocks();

        assertTrue(persistencyHandler.closeDatabaseConnection());
    }

    @Test
    public void testCloseDatabaseConnectionWithException() throws SQLException {
        createConnectionAndRelatedMocks();

        Mockito.doThrow(new SQLException("Create statement failed.")).when(mockDatabaseConnection).close();

        assertFalse(persistencyHandler.closeDatabaseConnection());
    }

    private void createConnectionAndRelatedMocks() throws SQLException {
        String authorName = "Cara Santa Maria";
        int authorId = 28;
        author = new Author(authorName, authorId);

        int articleIntValue = author.getRecordId();
        String articleStringValue = "generic string value";
        ZonedDateTime publishingDateTime = Utilities.createDate(2017, Month.JUNE, 27);

        article = new Article.Builder(articleStringValue, articleStringValue, author, articleStringValue,
                                      publishingDateTime, articleStringValue)
                .recordId(articleIntValue)
                .likes(articleIntValue)
                .build();

        ResultSet mockResultSetAuthor = Mockito.mock(ResultSet.class);
        ResultSet mockResultSetArticle = Mockito.mock(ResultSet.class);

        Mockito.when(mockStatement.executeQuery(Mockito.anyString()))
                .thenReturn(mockResultSetAuthor, mockResultSetArticle);

        Mockito.when(mockResultSetAuthor.next()).thenReturn(true, false);
        Mockito.when(mockResultSetAuthor.getInt(Mockito.anyString())).thenReturn(authorId);
        Mockito.when(mockResultSetAuthor.getString(Mockito.anyString())).thenReturn(authorName);

        Mockito.when(mockResultSetArticle.next()).thenReturn(true, false);
        Mockito.when(mockResultSetArticle.getInt(Mockito.anyString())).thenReturn(articleIntValue);
        Mockito.when(mockResultSetArticle.getString(Mockito.anyString())).thenReturn(articleStringValue);

        Mockito.when(mockResultSetArticle.getTimestamp(Mockito.anyString()))
                .thenReturn(Timestamp.from(publishingDateTime.toInstant()));

        assertTrue(persistencyHandler.initializeDatabaseConnection(mockDatabaseConnection));
    }
}

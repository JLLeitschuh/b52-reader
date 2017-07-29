/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Enable storage of articles and authors in an H2 database.
 * <p>
 * Define more constants for table (and column) names?
 * <p>
 * If performance is not good enough: http://h2database.com/html/performance.html.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class PersistencyHandlerJdbc implements PersistencyHandler {
    /**
     * Word article to use in GUI and logging.
     */
    private static final String ARTICLE_WORD = "article";

    /**
     * Table name article to use in database.
     */
    private static final String ARTICLE_TABLE_NAME = ARTICLE_WORD;

    /**
     * Word author to use in GUI and logging.
     */
    private static final String AUTHOR_WORD = "author";

    /**
     * Table name author to use in database.
     */
    private static final String AUTHOR_TABLE_NAME = AUTHOR_WORD;

    /**
     * Column name of id field in author table (in database).
     */
    private static final String AUTHOR_ID = "id";

    /**
     * Column name of name field in author table (in database).
     */
    private static final String AUTHOR_NAME = "name";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Database connection.
     */
    private Connection databaseConnection;

    /**
     * Statement to execute static SQL queries.
     */
    private Statement statement;

    /**
     * Authors stored in database.
     */
    private List<Author> storedAuthors;

    /**
     * Map of names to authors stored in database.
     */
    private Map<String, Author> storedAuthorsMap;

    /**
     * Map of URLs to articles stored in database.
     */
    private Map<String, Article> storedArticlesMap;

    @Override
    public boolean initializeDatabaseConnection(final Connection databaseConnection) {
        boolean result;

        try {
            this.databaseConnection = databaseConnection;
            this.statement = databaseConnection.createStatement();

            result = this.statement != null;
        } catch (final SQLException e) {
            logger.error("Exception while initializing the database connection.", e);

            result = false;
        }

        logger.info("Initialized the database connection.");

        return result;
    }

    @Override
    public void createTablesIfNeeded() {
        try {
            if (!tableExists(AUTHOR_TABLE_NAME)) {
                createTable(String.format(
                    "create table %s (id int auto_increment primary key, name varchar(100))", AUTHOR_TABLE_NAME
                ), AUTHOR_TABLE_NAME);
            }

            if (!tableExists(ARTICLE_TABLE_NAME)) {
                createTable(String.format(
                    "create table %s (id int auto_increment primary key, url varchar(2800), source_id varchar(42), "
                    + "author_id int not null references %s (id), title varchar(200), date_time timestamp, "
                    + "text varchar(8128), starred boolean, read boolean, archived boolean, likes int)",
                    ARTICLE_TABLE_NAME, AUTHOR_TABLE_NAME
                ), ARTICLE_TABLE_NAME);
            }
        } catch (final SQLException e) {
            logger.error("Exception while creating the database tables.", e);
        }
    }

    /**
     * Check whether a table exists in the database or not.
     *
     * @param tableName database table name.
     * @return whether a table exists in the database or not.
     * @throws SQLException if a database error occurs.
     */
    private boolean tableExists(final String tableName) throws SQLException {
        final boolean result;

        try (ResultSet tables = databaseConnection.getMetaData().getTables(null, null,
                                                                           tableName.toUpperCase(), null)) {
            result = tables.next();
        }

        return result;
    }

    /**
     * Create a database table using the specified create table query.
     *
     * @param createQuery SQL query to create database table.
     * @param tableName   database table name.
     * @throws SQLException if a database error occurs.
     */
    private void createTable(final String createQuery, final String tableName) throws SQLException {
        statement.execute(createQuery);

        logger.info(String.format("Created the %s database table.", tableName));
    }

    @Override
    public void readAuthorsAndArticles() {
        try {
            storedAuthors = new ArrayList<>();
            storedAuthorsMap = new HashMap<>();
            storedArticlesMap = new HashMap<>();

            readObjects(AUTHOR_TABLE_NAME, getAuthorRecordHandler(), AUTHOR_WORD);
            readObjects(ARTICLE_TABLE_NAME, getArticleRecordHandler(), AUTHOR_WORD);
        } catch (final SQLException e) {
            logger.error("Exception while reading authors and articles from the database.", e);
        }
    }

    /**
     * Get a handler for reading authors (via a record set).
     *
     * @return handler for reading authors (via a record set).
     */
    private Consumer<ResultSet> getAuthorRecordHandler() {
        return resultSet -> {
            try {
                final int id = resultSet.getInt(AUTHOR_ID);
                final String name = resultSet.getString(AUTHOR_NAME);
                final Author author = new Author(name, id);

                storedAuthors.add(author);
                storedAuthorsMap.put(name, author);
            } catch (final SQLException e) {
                logger.error("Exception while reading authors from the database.", e);
            }
        };
    }

    /**
     * Get a handler for reading articles (via a record set).
     *
     * @return handler for reading articles (via a record set).
     */
    private Consumer<ResultSet> getArticleRecordHandler() {
        return resultSet -> {
            final Article article = Article.createArticleFromDatabase(resultSet, storedAuthors);

            storedArticlesMap.put(article.getUrl(), article);
        };
    }

    /**
     * Read objects from a database table.
     *
     * @param tableName     database table name.
     * @param recordHandler handler for reading object.
     * @param objectWord    word describing objects for logging.
     * @throws SQLException if a database error occurs.
     */
    private void readObjects(final String tableName, final Consumer<ResultSet> recordHandler, final String objectWord)
            throws SQLException {
        int objectCount = 0;

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement("select distinct * from ?")) {
            preparedStatement.setString(1, tableName);

            try (ResultSet authorsResultSet = preparedStatement.executeQuery()) {
                while (authorsResultSet.next()) {
                    recordHandler.accept(authorsResultSet);
                    objectCount++;
                }
            }
        }

        logger.info("Read {} from the database.", Utilities.countAndWord(objectCount, objectWord));
    }

    @Override
    public Map<String, Author> getStoredAuthorsMap() {
        return storedAuthorsMap;
    }

    @Override
    public Map<String, Article> getStoredArticlesMap() {
        return storedArticlesMap;
    }

    @Override
    public Author getOrCreateAuthor(final String name) {
        final int impossibleRecordId = -28;

        return storedAuthorsMap.getOrDefault(name, new Author(name, impossibleRecordId));
    }

    @Override
    public void saveAuthorsAndArticles(final List<Article> currentArticles) {
        final List<Article> newArticles = currentArticles.stream()
            .filter(article -> !storedArticlesMap.containsKey(article.getUrl()))
            .collect(Collectors.toList());

        if (!newArticles.isEmpty()) {
            final List<Author> newAuthors = newArticles.stream()
                .map(Article::getAuthor)
                .distinct()
                .filter(author -> !storedAuthorsMap.containsKey(author.getName()))
                .collect(Collectors.toList());

            saveNewAuthors(newAuthors);
            updateObjectAuthorIds(newAuthors);
        }

        updateObjectAuthorIds(storedAuthors);

        final List<Article> existingArticles = new ArrayList<>(currentArticles);
        existingArticles.removeAll(newArticles);
        updateExistingArticles(existingArticles);

        if (!newArticles.isEmpty()) {
            saveNewArticles(newArticles);
        }
    }

    /**
     * Save new authors (from memory to database).
     *
     * @param newAuthors new authors.
     */
    private void saveNewAuthors(final List<Author> newAuthors) {
        if (!newAuthors.isEmpty()) {
            final String insertQuery = String.format("insert into %s (name) values (?)", AUTHOR_TABLE_NAME);

            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery)) {
                for (Author newAuthor : newAuthors) {
                    preparedStatement.setString(1, newAuthor.getName());
                    preparedStatement.addBatch();
                }

                final int[] results = preparedStatement.executeBatch();
                for (int authorIndex = 0; authorIndex < results.length; authorIndex++) {
                    final int result = results[authorIndex];
                    if (result != 1) {
                        logger.error("Error writing " + AUTHOR_WORD + " {} to the database.",
                                     newAuthors.get(authorIndex));
                    }
                }
            } catch (final SQLException e) {
                logger.error("Exception while inserting " + AUTHOR_WORD + "s into the database.", e);
            }

            logger.info(String.format("Wrote %s to the database.",
                                      Utilities.countAndWord(newAuthors.size(), "new " + AUTHOR_WORD)));
        }
    }

    /**
     * Update the database record ids for the specified authors.
     *
     * @param authors authors to update the database record id for.
     */
    private void updateObjectAuthorIds(final List<Author> authors) {
        final Map<String, Author> authorsMap = authors.stream()
            .collect(Collectors.toMap(Author::getName, Function.identity()));

        try (ResultSet authorsResultSet = statement.executeQuery("select distinct * from " + AUTHOR_TABLE_NAME)) {
            while (authorsResultSet.next()) {
                final int id = authorsResultSet.getInt(AUTHOR_ID);
                final String name = authorsResultSet.getString(AUTHOR_NAME);

                if (authorsMap.containsKey(name)) {
                    authorsMap.put(name, new Author(name, id));
                }
            }
        } catch (final SQLException e) {
            logger.error("Exception while reading " + AUTHOR_WORD + "s from the database.", e);
        }
    }

    /**
     * Update existing articles from memory to database.
     *
     * @param existingArticles existing articles to update.
     */
    private void updateExistingArticles(final List<Article> existingArticles) {
        final String updateQuery
            = "update " + ARTICLE_TABLE_NAME + " "
              + "set url = ?, source_id = ?, author_id = ?, title = ?, date_time = ?, text = ?, "
              + "likes = ?, starred = ?, read = ?, archived = ? "
              + "where id = ?";

        final List<Article> updateArticles = new ArrayList<>();

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery)) {
            for (Article existingArticle : existingArticles) {
                final Article storedArticle = storedArticlesMap.get(existingArticle.getUrl());

                // Copy the recordId field since this will never match the field of the stored article.
                existingArticle.setRecordId(storedArticle.getRecordId());

                if (!Objects.equals(existingArticle, storedArticle) || !existingArticle.metadataEquals(storedArticle)) {
                    setParameters(preparedStatement, existingArticle.getUrl(), existingArticle.getSourceId(),
                                  existingArticle.getAuthor() != null ? existingArticle.getAuthor().getRecordId() : null,
                                  existingArticle.getTitle(), Timestamp.from(existingArticle.getDateTime().toInstant()),
                                  existingArticle.getText(), existingArticle.getLikes(), existingArticle.isStarred(),
                                  existingArticle.isRead(), existingArticle.isArchived(), storedArticle.getRecordId());

                    preparedStatement.addBatch();

                    updateArticles.add(existingArticle);
                }
            }

            executeArticlesUpdate(updateArticles, preparedStatement);
        } catch (final SQLException e) {
            logger.error("Exception while updating articles in the database.", e);
        }

        if (!updateArticles.isEmpty()) {
            logger.info("Updated {} in the database.", Utilities.countAndWord(updateArticles.size(), ARTICLE_WORD));
        }
    }

    /**
     * Execute article updates in database.
     *
     * @param updateArticles    articles that need to be updated in database.
     * @param preparedStatement prepared statement to execute a batch of updates with.
     * @throws SQLException if a database error occurs.
     */
    private void executeArticlesUpdate(final List<Article> updateArticles, final PreparedStatement preparedStatement)
            throws SQLException {
        if (!updateArticles.isEmpty()) {
            final int[] results = preparedStatement.executeBatch();
            for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                final int result = results[articleIndex];
                if (result != 1) {
                    logger.error("Error updating article {} in the database.", updateArticles.get(articleIndex));
                }
            }
        }
    }

    /**
     * Save new articles (from memory to database).
     *
     * @param newArticles new articles.
     */
    private void saveNewArticles(final List<Article> newArticles) {
        try {
            final String insertQuery = String.format(
                "insert into %s (url, source_id, author_id, title, date_time, text, starred, read, archived, likes) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", ARTICLE_TABLE_NAME);

            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery)) {
                for (Article newArticle : newArticles) {
                    setParameters(preparedStatement, newArticle.getUrl(), newArticle.getSourceId(),
                                  newArticle.getAuthor() != null ? newArticle.getAuthor().getRecordId() : null,
                                  newArticle.getTitle(), Timestamp.from(newArticle.getDateTime().toInstant()),
                                  newArticle.getText(), newArticle.isStarred(), newArticle.isRead(),
                                  newArticle.isArchived(), newArticle.getLikes());

                    preparedStatement.addBatch();
                }

                final int[] results = preparedStatement.executeBatch();
                for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                    final int result = results[articleIndex];
                    if (result != 1) {
                        logger.error("Error writing article {} to the database.", newArticles.get(articleIndex));
                    }
                }
            }

            logger.info("Wrote {} to the database.",
                        Utilities.countAndWord(newArticles.size(), "new article"));
        } catch (final SQLException e) {
            logger.error("Exception while inserting articles into the database.", e);
        }
    }

    /**
     * Set parameters for specified prepared statement.
     *
     * @param preparedStatement prepared statement to set parameters for.
     * @param values            parameter values to set.
     * @throws SQLException if a database error occurs.
     */
    private void setParameters(final PreparedStatement preparedStatement, final Object... values) throws SQLException {
        for (int parameterIndex = 0; parameterIndex < values.length; parameterIndex++) {
            preparedStatement.setObject(parameterIndex + 1, values[parameterIndex]);
        }
    }

    @Override
    public boolean closeDatabaseConnection() {
        boolean result = true;

        try {
            databaseConnection.close();
        } catch (final SQLException e) {
            logger.error("Exception while closing the database connection.", e);
            result = false;
        }

        return result;
    }
}

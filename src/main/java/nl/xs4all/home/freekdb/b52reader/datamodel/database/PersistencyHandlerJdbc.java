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
    private static final String ARTICLE_WORD = "article";
    private static final String ARTICLE_TABLE_NAME = ARTICLE_WORD;

    private static final String AUTHOR_WORD = "author";
    private static final String AUTHOR_TABLE_NAME = AUTHOR_WORD;

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    private Connection databaseConnection;
    private Statement statement;

    private List<Author> storedAuthors;
    private Map<String, Author> storedAuthorsMap;
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
                statement.execute("create table " + AUTHOR_TABLE_NAME + "(id int auto_increment primary key, "
                                  + "name varchar(100))");

                logger.info("Created the " + AUTHOR_TABLE_NAME + " database table.");
            }

            if (!tableExists(ARTICLE_TABLE_NAME)) {
                statement.execute("create table " + ARTICLE_TABLE_NAME + " (id int auto_increment primary key, "
                                  + "url varchar(2800), source_id varchar(42), "
                                  + "author_id int not null references " + AUTHOR_TABLE_NAME + "(id), "
                                  + "title varchar(200), date_time timestamp, text varchar(8128), "
                                  + "starred boolean, read boolean, archived boolean, likes int)");

                logger.info("Created the " + ARTICLE_TABLE_NAME + " database table.");
            }
        } catch (final SQLException e) {
            logger.error("Exception while creating the database tables.", e);
        }
    }

    private boolean tableExists(final String tableName) throws SQLException {
        final boolean result;

        try (ResultSet tables = databaseConnection.getMetaData().getTables(null, null,
                                                                           tableName.toUpperCase(), null)) {
            result = tables.next();
        }

        return result;
    }

    @Override
    public void readAuthorsAndArticles() {
        try {
            storedAuthors = new ArrayList<>();
            storedAuthorsMap = new HashMap<>();

            try (ResultSet authorsResultSet = statement.executeQuery("select distinct * from " + AUTHOR_TABLE_NAME)) {
                while (authorsResultSet.next()) {
                    final int id = authorsResultSet.getInt("id");
                    final String name = authorsResultSet.getString("name");
                    final Author author = new Author(name, id);

                    storedAuthors.add(author);
                    storedAuthorsMap.put(name, author);
                }
            }

            logger.info("Read {} from the database.",
                        Utilities.countAndWord(storedAuthors.size(), AUTHOR_WORD));

            storedArticlesMap = new HashMap<>();

            try (ResultSet articlesResultSet = statement.executeQuery("select distinct * from "
                                                                      + ARTICLE_TABLE_NAME)) {
                while (articlesResultSet.next()) {
                    final Article article = Article.createArticleFromDatabase(articlesResultSet, storedAuthors);

                    storedArticlesMap.put(article.getUrl(), article);
                }
            }

            logger.info("Read {} from the database.",
                        Utilities.countAndWord(storedArticlesMap.size(), ARTICLE_WORD));
        } catch (final SQLException e) {
            logger.error("Exception while reading authors and articles from the database.", e);
        }
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

    private void saveNewAuthors(final List<Author> newAuthors) {
        if (!newAuthors.isEmpty()) {
            final String insertQuery = "insert into " + AUTHOR_TABLE_NAME + "(name) values (?)";

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

            logger.info("Wrote {} to the database.",
                        Utilities.countAndWord(newAuthors.size(), "new " + AUTHOR_WORD));
        }
    }

    private void updateObjectAuthorIds(final List<Author> authors) {
        final Map<String, Author> authorsMap = authors.stream()
            .collect(Collectors.toMap(Author::getName, Function.identity()));

        try (ResultSet authorsResultSet = statement.executeQuery("select distinct * from " + AUTHOR_TABLE_NAME)) {
            while (authorsResultSet.next()) {
                final int id = authorsResultSet.getInt("id");
                final String name = authorsResultSet.getString("name");

                if (authorsMap.containsKey(name)) {
                    authorsMap.put(name, new Author(name, id));
                }
            }
        } catch (final SQLException e) {
            logger.error("Exception while reading " + AUTHOR_WORD + "s from the database.", e);
        }
    }

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
                    preparedStatement.setString(1, existingArticle.getUrl());
                    preparedStatement.setString(2, existingArticle.getSourceId());
                    preparedStatement.setInt(3, existingArticle.getAuthor().getRecordId());
                    preparedStatement.setString(4, existingArticle.getTitle());
                    preparedStatement.setTimestamp(5, Timestamp.from(existingArticle.getDateTime().toInstant()));
                    preparedStatement.setString(6, existingArticle.getText());
                    preparedStatement.setInt(7, existingArticle.getLikes());
                    preparedStatement.setBoolean(8, existingArticle.isStarred());
                    preparedStatement.setBoolean(9, existingArticle.isRead());
                    preparedStatement.setBoolean(10, existingArticle.isArchived());

                    preparedStatement.setInt(11, storedArticle.getRecordId());

                    preparedStatement.addBatch();

                    updateArticles.add(existingArticle);
                }
            }

            if (!updateArticles.isEmpty()) {
                final int[] results = preparedStatement.executeBatch();
                for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                    final int result = results[articleIndex];
                    if (result != 1) {
                        logger.error("Error updating article {} in the database.", updateArticles.get(articleIndex));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error("Exception while updating articles in the database.", e);
        }

        if (!updateArticles.isEmpty()) {
            logger.info("Updated {} in the database.", Utilities.countAndWord(updateArticles.size(), ARTICLE_WORD));
        }
    }

    private void saveNewArticles(final List<Article> newArticles) {
        try {
            final String insertQuery
                = "insert into " + ARTICLE_TABLE_NAME
                  + " (url, source_id, author_id, title, date_time, text, starred, read, archived, likes) "
                  + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery)) {
                for (Article newArticle : newArticles) {
                    preparedStatement.setString(1, newArticle.getUrl());
                    preparedStatement.setString(2, newArticle.getSourceId());
                    preparedStatement.setInt(3, newArticle.getAuthor().getRecordId());
                    preparedStatement.setString(4, newArticle.getTitle());
                    preparedStatement.setTimestamp(5, Timestamp.from(newArticle.getDateTime().toInstant()));
                    preparedStatement.setString(6, newArticle.getText());
                    preparedStatement.setBoolean(7, newArticle.isStarred());
                    preparedStatement.setBoolean(8, newArticle.isRead());
                    preparedStatement.setBoolean(9, newArticle.isArchived());
                    preparedStatement.setInt(10, newArticle.getLikes());

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

            if (!newArticles.isEmpty()) {
                logger.info("Wrote {} to the database.",
                            Utilities.countAndWord(newArticles.size(), "new article"));
            }
        } catch (final SQLException e) {
            logger.error("Exception while inserting articles into the database.", e);
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

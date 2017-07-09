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
 * Define more constants for table (and column) names?
 * <p>
 * Enable storage of articles and authors in an H2 database.
 * <p>
 * If performance is not good enough: http://h2database.com/html/performance.html.
 */
public class PersistencyHandlerJdbc implements PersistencyHandler {
    private static final String ARTICLE_TABLE_NAME = "article";
    private static final String ARTICLE_WORD = "article";

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
    public boolean initializeDatabaseConnection(Connection databaseConnection) {
        boolean result;

        try {
            this.databaseConnection = databaseConnection;
            this.statement = databaseConnection.createStatement();

            result = this.statement != null;
        } catch (SQLException e) {
            logger.error("Exception while initializing the database connection.", e);

            result = false;
        }

        logger.info("Initialized the database connection.");

        return result;
    }

    @Override
    public void createTablesIfNeeded() {
        try {
            if (!tableExists("author")) {
                statement.execute("create table author(id int auto_increment primary key, " +
                                  "name varchar(100))");

                logger.info("Created the author database table.");
            }

            if (!tableExists(ARTICLE_TABLE_NAME)) {
                statement.execute("create table " + ARTICLE_TABLE_NAME + " (id int auto_increment primary key, " +
                                  "url varchar(2800), source_id varchar(42), " +
                                  "author_id int not null references author(id), " +
                                  "title varchar(200), date_time timestamp, text varchar(8128), " +
                                  "starred boolean, read boolean, archived boolean, likes int)");

                logger.info("Created the " + ARTICLE_TABLE_NAME + " database table.");
            }
        } catch (SQLException e) {
            logger.error("Exception while creating the database tables.", e);
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        boolean result;

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

            try (ResultSet authorsResultSet = statement.executeQuery("select distinct * from author")) {
                while (authorsResultSet.next()) {
                    int id = authorsResultSet.getInt("id");
                    String name = authorsResultSet.getString("name");
                    Author author = new Author(name, id);

                    storedAuthors.add(author);
                    storedAuthorsMap.put(name, author);
                }
            }

            logger.info("Read {} from the database.",
                        Utilities.countAndWord(storedAuthors.size(), "author"));

            storedArticlesMap = new HashMap<>();

            try (ResultSet articlesResultSet = statement.executeQuery("select distinct * from " +
                                                                      ARTICLE_TABLE_NAME)) {
                while (articlesResultSet.next()) {
                    Article article = Article.createArticleFromDatabase(articlesResultSet, storedAuthors);

                    storedArticlesMap.put(article.getUrl(), article);
                }
            }

            logger.info("Read {} from the database.",
                        Utilities.countAndWord(storedArticlesMap.size(), ARTICLE_WORD));
        } catch (SQLException e) {
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
    public Author getOrCreateAuthor(String name) {
        return storedAuthorsMap.getOrDefault(name, new Author(name, -28));
    }

    @Override
    public void saveAuthorsAndArticles(List<Article> currentArticles) {
        List<Article> newArticles = currentArticles.stream()
                .filter(article -> !storedArticlesMap.containsKey(article.getUrl()))
                .collect(Collectors.toList());

        if (!newArticles.isEmpty()) {
            List<Author> newAuthors = newArticles.stream()
                    .map(Article::getAuthor)
                    .distinct()
                    .filter(author -> !storedAuthorsMap.containsKey(author.getName()))
                    .collect(Collectors.toList());

            saveNewAuthors(newAuthors);
            updateObjectAuthorIds(newAuthors);
        }

        updateObjectAuthorIds(storedAuthors);

        List<Article> existingArticles = new ArrayList<>(currentArticles);
        existingArticles.removeAll(newArticles);
        updateExistingArticles(existingArticles);

        if (!newArticles.isEmpty()) {
            saveNewArticles(newArticles);
        }
    }

    private void saveNewAuthors(List<Author> newAuthors) {
        if (!newAuthors.isEmpty()) {
            final String insertQuery = "insert into author(name) values (?)";

            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery)) {
                for (Author newAuthor : newAuthors) {
                    preparedStatement.setString(1, newAuthor.getName());
                    preparedStatement.addBatch();
                }

                int[] results = preparedStatement.executeBatch();
                for (int authorIndex = 0; authorIndex < results.length; authorIndex++) {
                    int result = results[authorIndex];
                    if (result != 1) {
                        logger.error("Error writing author {} to the database.", newAuthors.get(authorIndex));
                    }
                }
            } catch (SQLException e) {
                logger.error("Exception while inserting authors into the database.", e);
            }

            logger.info("Wrote {} to the database.", Utilities.countAndWord(newAuthors.size(), "new author"));
        }
    }

    private void updateObjectAuthorIds(List<Author> authors) {
        Map<String, Author> authorsMap = authors.stream()
                .collect(Collectors.toMap(Author::getName, Function.identity()));

        try (ResultSet authorsResultSet = statement.executeQuery("select distinct * from author")) {
            while (authorsResultSet.next()) {
                int id = authorsResultSet.getInt("id");
                String name = authorsResultSet.getString("name");

                if (authorsMap.containsKey(name)) {
                    authorsMap.get(name).setRecordId(id);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception while reading authors from the database.", e);
        }
    }

    private void updateExistingArticles(List<Article> existingArticles) {
        String updateQuery = "update " + ARTICLE_TABLE_NAME + " " +
                             "set url = ?, source_id = ?, author_id = ?, title = ?, date_time = ?, text = ?, " +
                             "likes = ?, starred = ?, read = ?, archived = ? " +
                             "where id = ?";

        List<Article> updateArticles = new ArrayList<>();

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery)) {
            for (Article existingArticle : existingArticles) {
                Article storedArticle = storedArticlesMap.get(existingArticle.getUrl());

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
                int[] results = preparedStatement.executeBatch();
                for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                    int result = results[articleIndex];
                    if (result != 1) {
                        logger.error("Error updating article {} in the database.", updateArticles.get(articleIndex));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Exception while updating articles in the database.", e);
        }

        if (!updateArticles.isEmpty()) {
            logger.info("Updated {} in the database.", Utilities.countAndWord(updateArticles.size(), ARTICLE_WORD));
        }
    }

    private void saveNewArticles(List<Article> newArticles) {
        try {
            String insertQuery = "insert into " + ARTICLE_TABLE_NAME +
                                 " (url, source_id, author_id, title, date_time, text, starred, read, archived, likes) " +
                                 "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

                int[] results = preparedStatement.executeBatch();
                for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                    int result = results[articleIndex];
                    if (result != 1) {
                        logger.error("Error writing article {} to the database.", newArticles.get(articleIndex));
                    }
                }
            }

            if (!newArticles.isEmpty()) {
                logger.info("Wrote {} to the database.",
                            Utilities.countAndWord(newArticles.size(), "new article"));
            }
        } catch (SQLException e) {
            logger.error("Exception while inserting articles into the database.", e);
        }
    }

    @Override
    public boolean closeDatabaseConnection() {
        boolean result = true;

        try {
            databaseConnection.close();
        } catch (SQLException e) {
            logger.error("Exception while closing the database connection.", e);
            result = false;
        }

        return result;
    }
}

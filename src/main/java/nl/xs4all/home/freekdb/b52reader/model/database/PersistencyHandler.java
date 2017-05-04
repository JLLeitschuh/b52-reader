/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Enable storage of articles and authors in an H2 database.
 * <p>
 * If performance is not good enough: http://h2database.com/html/performance.html.
 */
public class PersistencyHandler {
    private static final Logger logger = LogManager.getLogger(PersistencyHandler.class);

    private Connection databaseConnection;
    private Statement statement;

    private List<Author> storedAuthors;
    private Map<String, Author> storedAuthorsMap;
    private List<Article> storedArticles;
    private Map<String, Article> storedArticlesMap;

    public Map<String, Author> getStoredAuthorsMap() {
        return storedAuthorsMap;
    }

    public Map<String, Article> getStoredArticlesMap() {
        return storedArticlesMap;
    }

    public boolean initializeDatabaseConnection() {
        boolean result = true;

        try {
            Class.forName("org.h2.Driver");
            String databaseUrl = "jdbc:h2:./data/b52-reader-settings";
            databaseConnection = DriverManager.getConnection(databaseUrl, "b52", "reader");

            statement = databaseConnection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Exception while initializing the database connection.", e);
            result = false;
        }

        logger.info("Initialized the database connection.");

        return result;
    }

    public void createTablesIfNeeded() {
        try {
            if (!tableExists("author")) {
                statement.execute("create table author(id int auto_increment primary key, " +
                                  "name varchar(100))");

                logger.info("Created the author database table.");
            }

            if (!tableExists("article")) {
                statement.execute("create table article(id int auto_increment primary key, " +
                                  "url varchar(2800), author_id int not null references author(id), " +
                                  "title varchar(200), date_time timestamp, text varchar(8128), " +
                                  "starred boolean, read boolean, archived boolean, likes int)");

                logger.info("Created the article database table.");
            }
        } catch (SQLException e) {
            logger.error("Exception while creating the database tables.", e);
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData metaData = databaseConnection.getMetaData();

        return metaData.getTables(null, null, tableName.toUpperCase(), null).next();
    }

    public void readAuthorsAndArticles() {
        try {
            storedAuthors = new ArrayList<>();
            storedAuthorsMap = new HashMap<>();
            ResultSet authorsResultSet = statement.executeQuery("select distinct * from author");
            while (authorsResultSet.next()) {
                int id = authorsResultSet.getInt("id");
                String name = authorsResultSet.getString("name");
                Author author = new Author(id, name);

                storedAuthors.add(author);
                storedAuthorsMap.put(name, author);
            }

            logger.info("Read {} from the database.", Utilities.countAndWord(storedAuthors.size(), "author"));

            storedArticles = new ArrayList<>();
            storedArticlesMap = new HashMap<>();
            ResultSet articlesResultSet = statement.executeQuery("select distinct * from article");
            while (articlesResultSet.next()) {
                Article article = Article.createArticleFromDatabase(articlesResultSet, storedAuthors);

                storedArticles.add(article);
                storedArticlesMap.put(article.getUrl(), article);
            }

            logger.info("Read {} from the database.", Utilities.countAndWord(storedArticles.size(), "article"));
        } catch (SQLException e) {
            logger.error("Exception while reading authors and articles from the database.", e);
        }
    }

    public void saveAuthorsAndArticles(List<Article> currentArticles) {
        List<Article> newArticles = currentArticles.stream()
                .filter(article -> !storedArticlesMap.containsKey(article.getUrl()))
                .collect(Collectors.toList());

        if (newArticles.size() > 0) {
            List<Author> newAuthors = newArticles.stream()
                    .map(Article::getAuthor)
                    .distinct()
                    .filter(author -> !storedAuthorsMap.containsKey(author.getName()))
                    .collect(Collectors.toList());

            saveNewAuthors(newAuthors);
            updateObjectAuthorIds(newAuthors);
        }

        List<Article> existingArticles = new ArrayList<>(currentArticles);
        existingArticles.removeAll(newArticles);
        updateExistingArticles(existingArticles);
        saveNewArticles(newArticles);
    }

    private void saveNewAuthors(List<Author> newAuthors) {
        if (newAuthors.size() > 0) {
            try {
                String insertQuery = "insert into author(name) values (?)";
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery);

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

                preparedStatement.close();

                logger.info("Wrote {} to the database.", Utilities.countAndWord(newAuthors.size(), "new author"));
            } catch (SQLException e) {
                logger.error("Exception while inserting authors into the database.", e);
            }
        }
    }

    private void updateObjectAuthorIds(List<Author> newAuthors) {
        Map<String, Author> newAuthorsMap = newAuthors.stream()
                .collect(Collectors.toMap(Author::getName, Function.identity()));

        try {
            ResultSet authorsResultSet = statement.executeQuery("select distinct * from author");
            while (authorsResultSet.next()) {
                int id = authorsResultSet.getInt("id");
                String name = authorsResultSet.getString("name");

                if (newAuthorsMap.containsKey(name)) {
                    newAuthorsMap.get(name).setId(id);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception while reading authors from the database.", e);
        }
    }

    private void updateExistingArticles(List<Article> existingArticles) {
        try {
            String updateQuery = "update article " +
                                 "set url = ?, author_id = ?, title = ?, date_time = ?, text = ?, starred = ?, " +
                                 "read = ?, archived = ?, likes = ? " +
                                 "where id = ?";

            PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery);

            List<Article> updateArticles = new ArrayList<>();

            for (Article existingArticle : existingArticles) {
                Article storedArticle = storedArticlesMap.get(existingArticle.getUrl());

                // Copy the id and date/time fields since these will never match the fields of the stored article.
                existingArticle.setId(storedArticle.getId());
                existingArticle.setDateTime(storedArticle.getDateTime());

                if (!existingArticle.equals(storedArticle)) {
                    preparedStatement.setString(1, existingArticle.getUrl());
                    preparedStatement.setInt(2, existingArticle.getAuthor().getId());
                    preparedStatement.setString(3, existingArticle.getTitle());
                    preparedStatement.setTimestamp(4, new Timestamp(existingArticle.getDateTime().getTime()));
                    preparedStatement.setString(5, existingArticle.getText());
                    preparedStatement.setBoolean(6, existingArticle.isStarred());
                    preparedStatement.setBoolean(7, existingArticle.isRead());
                    preparedStatement.setBoolean(8, existingArticle.isArchived());
                    preparedStatement.setInt(9, existingArticle.getLikes());

                    preparedStatement.setInt(10, storedArticle.getId());

                    preparedStatement.addBatch();

                    updateArticles.add(existingArticle);
                }
            }

            if (updateArticles.size() > 0) {
                int[] results = preparedStatement.executeBatch();
                for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                    int result = results[articleIndex];
                    if (result != 1) {
                        logger.error("Error updating article {} in the database.", updateArticles.get(articleIndex));
                    }
                }
            }

            preparedStatement.close();

            logger.info("Updated {} in the database.", Utilities.countAndWord(updateArticles.size(), "article"));
        } catch (SQLException e) {
            logger.error("Exception while updating authors in the database.", e);
        }
    }

    private void saveNewArticles(List<Article> newArticles) {
        try {
            String insertQuery = "insert into " +
                                 "article(url, author_id, title, date_time, text, starred, read, archived, likes) " +
                                 "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery);

            for (Article newArticle : newArticles) {
                preparedStatement.setString(1, newArticle.getUrl());
                preparedStatement.setInt(2, newArticle.getAuthor().getId());
                preparedStatement.setString(3, newArticle.getTitle());
                preparedStatement.setTimestamp(4, new Timestamp(newArticle.getDateTime().getTime()));
                preparedStatement.setString(5, newArticle.getText());
                preparedStatement.setBoolean(6, newArticle.isStarred());
                preparedStatement.setBoolean(7, newArticle.isRead());
                preparedStatement.setBoolean(8, newArticle.isArchived());
                preparedStatement.setInt(9, newArticle.getLikes());

                preparedStatement.addBatch();
            }

            int[] results = preparedStatement.executeBatch();
            for (int articleIndex = 0; articleIndex < results.length; articleIndex++) {
                int result = results[articleIndex];
                if (result != 1) {
                    logger.error("Error writing article {} to the database.", newArticles.get(articleIndex));
                }
            }

            preparedStatement.close();

            logger.info("Wrote {} to the database.", Utilities.countAndWord(newArticles.size(), "new article"));
        } catch (SQLException e) {
            logger.error("Exception while inserting articles into the database.", e);
        }
    }

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

    @SuppressWarnings("unused")
    public void insertTestData() {
        try {
            statement.execute("insert into author(name) values ('Cara Santa Maria')");
            statement.execute("insert into author(name) values ('Neil deGrasse Tyson')");

            statement.execute("insert into " +
                              "article(url, author_id, title, date_time, text, starred, read, archived, likes) " +
                              "values (" +
                              "'http://www.huffingtonpost.com/2012/12/17/superstring-theory_n_2296195.html', " +
                              "1, 'WTF Is String Theory?', null, " +
                              "'Have you ever heard the term string theory and wondered WTF it means? When it comes " +
                              "to theoretical physics, it seems like there are a lot of larger-than-life concepts that " +
                              "have made their way into our everyday conversations.', " +
                              "false, true, true, 28" +
                              ")");

            statement.execute("insert into " +
                              "article(url, author_id, title, date_time, text, starred, read, archived, likes) " +
                              "values (" +
                              "'http://www.haydenplanetarium.org/tyson/read/2007/04/02/the-cosmic-perspective', " +
                              "2, 'The Cosmic Perspective', null, " +
                              "'Long before anyone knew that the universe had a beginning, before we knew that the " +
                              "nearest large galaxy lies two and a half million light-years from Earth, before we knew " +
                              "how stars work or whether atoms exist, James Ferguson''s enthusiastic introduction to his " +
                              "favorite science rang true.', " +
                              "true, false, false, 6" +
                              ")");
        } catch (SQLException e) {
            logger.error("Exception while inserting test data into the database.", e);
        }
    }

    @SuppressWarnings("unused")
    public void deleteAllAuthorsAndArticles() {
        try {
            statement.execute("delete from article");
            if (storedArticles != null && storedArticlesMap != null) {
                storedArticles.clear();
                storedArticlesMap.clear();
            }

            statement.execute("delete from author");
            if (storedAuthors != null && storedAuthorsMap != null) {
                storedAuthors.clear();
                storedAuthorsMap.clear();
            }
        } catch (SQLException e) {
            logger.error("Exception while deleting authors and articles from the database.", e);
        }

        logger.info("Removed all authors and articles from the database.");
    }

    @SuppressWarnings("unused")
    public void readAndPrintAuthorsAndArticles() {
        try {
            ResultSet authorsResultSet = statement.executeQuery("select * from author");
            if (authorsResultSet.next()) {
                logger.info("");
                logger.info("Authors:");
                printResultSet(authorsResultSet);
            }

            ResultSet articlesResultSet = statement.executeQuery("select * from article");
            if (articlesResultSet.next()) {
                logger.info("Articles:");
                printResultSet(articlesResultSet);
            }
        } catch (SQLException e) {
            logger.error("Exception while reading authors and articles from the database.", e);
        }
    }

    private void printResultSet(ResultSet resultSet) {
        try {
            boolean firstRecord = true;
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (firstRecord || resultSet.next()) {
                for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                    String columnName = metaData.getColumnName(columnIndex).toLowerCase();
                    String value = resultSet.getString(columnIndex);

                    logger.info("{}: {}", columnName, value);
                }
                logger.info("");
                firstRecord = false;
            }
        } catch (SQLException e) {
            logger.error("Exception while printing database records.", e);
        }
    }
}

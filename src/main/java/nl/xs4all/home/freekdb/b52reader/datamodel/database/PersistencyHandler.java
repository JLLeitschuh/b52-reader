/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel.database;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;

/**
 * Interface for storing of articles and authors in a database.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public interface PersistencyHandler {
    /**
     * Set and initialize the database connection.
     *
     * @param databaseConnection database connection to use.
     * @return whether initialization was successful.
     */
    boolean initializeDatabaseConnection(Connection databaseConnection);

    /**
     * Create any required tables for storing articles and authors.
     */
    void createTablesIfNeeded();

    /**
     * Read previously stored authors and articles.
     */
    void readAuthorsAndArticles();

    /**
     * Get the map with previously stored authors.
     *
     * @return map with previously stored authors.
     */
    Map<String, Author> getStoredAuthorsMap();

    /**
     * Get the map with previously stored articles.
     *
     * @return map with previously stored articles.
     */
    Map<String, Article> getStoredArticlesMap();

    /**
     * Get the previously stored author with the specified name or create a new author object if it does not exist yet.
     *
     * @param name author name.
     * @return previously stored author with the specified name or create a new author object if it does not exist yet.
     */
    Author getOrCreateAuthor(String name);

    /**
     * Save all changed and new authors and articles.
     *
     * @param currentArticles current articles in memory.
     */
    void saveAuthorsAndArticles(List<Article> currentArticles);

    /**
     * Close the database connection.
     *
     * @return whether closing was successful.
     */
    boolean closeDatabaseConnection();
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model.database;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;

public interface PersistencyHandler {
    boolean initializeDatabaseConnection(Connection databaseConnection);

    void createTablesIfNeeded();

    void readAuthorsAndArticles();

    Map<String, Author> getStoredAuthorsMap();

    Map<String, Article> getStoredArticlesMap();

    Author getOrCreateAuthor(String name);

    void saveAuthorsAndArticles(List<Article> currentArticles);

    boolean closeDatabaseConnection();

    @SuppressWarnings("unused")
    void readAndPrintAuthorsAndArticles();
}

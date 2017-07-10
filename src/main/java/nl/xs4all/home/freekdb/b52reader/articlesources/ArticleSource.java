/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources;

import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;

/**
 * Interface that describes what is needed to implement different sources of articles.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public interface ArticleSource {
    /**
     * Get identifier of this article source.
     *
     * @return identifier of this article source.
     */
    String getSourceId();

    /**
     * Get all articles that are currently available at this article source.
     *
     * @param persistencyHandler  persistency handler that provides access to database.
     * @param previousArticlesMap previously available articles.
     * @param previousAuthorsMap  previously available authors.
     * @return all articles that are currently available at this article source.
     */
    List<Article> getArticles(PersistencyHandler persistencyHandler, Map<String, Article> previousArticlesMap,
                              Map<String, Author> previousAuthorsMap);
}

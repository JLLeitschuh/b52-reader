/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;

public class CombinationArticleSource implements ArticleSource {
    private final List<ArticleSource> articleSources;
    private final List<Article> articles;

    public CombinationArticleSource(List<ArticleSource> articleSources) {
        this.articleSources = articleSources;
        this.articles = new ArrayList<>();
    }

    @Override
    public String getSourceId() {
        // We could add the source IDs of the underlying article sources here as well.
        return "combination";
    }

    @Override
    public List<Article> getArticles(PersistencyHandler persistencyHandler, Map<String, Article> previousArticlesMap,
                                     Map<String, Author> previousAuthorsMap) {
        articles.clear();

        for (ArticleSource articleSource : articleSources) {
            articles.addAll(articleSource.getArticles(persistencyHandler, previousArticlesMap, previousAuthorsMap));
        }

        articles.sort(Comparator.comparing(Article::getDateTime).reversed());

        return articles;
    }
}

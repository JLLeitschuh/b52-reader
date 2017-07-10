/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;

/**
 * Special article source that combines articles from multiple sources and sorts them most recent first.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class CombinationArticleSource implements ArticleSource {
    /**
     * Article sources to combine.
     */
    private final List<ArticleSource> articleSources;

    /**
     * Articles from all article sources.
     */
    private final List<Article> articles;

    /**
     * Construct an article source that combines articles from other sources.
     *
     * @param articleSources article sources to combine.
     */
    public CombinationArticleSource(final List<ArticleSource> articleSources) {
        this.articleSources = articleSources;
        this.articles = new ArrayList<>();
    }

    @Override
    public String getSourceId() {
        final List<String> sourceIds = articleSources.stream()
                .map(ArticleSource::getSourceId)
                .collect(Collectors.toList());

        return "combination: " + String.join(", ", sourceIds);
    }

    @Override
    public List<Article> getArticles(final PersistencyHandler persistencyHandler,
                                     final Map<String, Article> previousArticlesMap,
                                     final Map<String, Author> previousAuthorsMap) {
        articles.clear();

        for (ArticleSource articleSource : articleSources) {
            articles.addAll(articleSource.getArticles(persistencyHandler, previousArticlesMap, previousAuthorsMap));
        }

        articles.sort(Comparator.comparing(Article::getDateTime).reversed());

        return articles;
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources;

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class CombinationArticleSourceTest {
    @Test
    public void testGetSourceId() {
        CombinationArticleSource combinationArticleSource = new CombinationArticleSource(new ArrayList<>());

        assertEquals("combination: ", combinationArticleSource.getSourceId());
    }

    @Test
    public void testGetArticlesEmpty() {
        CombinationArticleSource combinationArticleSource = new CombinationArticleSource(new ArrayList<>());

        List<Article> noArticles = combinationArticleSource.getArticles(Mockito.mock(PersistencyHandler.class),
                                                                        null, null);
        assertEquals(0, noArticles.size());
    }

    @Test
    public void testGetArticlesRegular() {
        Author testAuthor = new Author("Test Author", 6);
        int articleCount1 = 2;
        int articleCount2 = 4;
        ArticleSource articleSource1 = createTestArticleSource("test-source-1", articleCount1, testAuthor);
        ArticleSource articleSource2 = createTestArticleSource("test-source-2", articleCount2, testAuthor);
        List<ArticleSource> articleSources = Arrays.asList(articleSource1, articleSource2);

        CombinationArticleSource combinationArticleSource = new CombinationArticleSource(articleSources);

        List<Article> articles = combinationArticleSource.getArticles(Mockito.mock(PersistencyHandler.class),
                                                                      null, null);

        assertEquals(articleCount1 + articleCount2, articles.size());
    }

    private ArticleSource createTestArticleSource(String sourceId, int articleCount, Author testAuthor) {
        List<Article> articles = new ArrayList<>();

        for (int articleIndex = 0; articleIndex < articleCount; articleIndex++) {
            Article article = new Article.Builder("https://test.org/article-" + articleIndex, sourceId, testAuthor,
                                                  "title-" + articleIndex,
                                                  Utilities.createDate(2017, Month.JUNE, articleIndex + 1),
                                                  "text-" + articleIndex)
                    .likes(6 * articleIndex)
                    .recordId(articleIndex)
                    .build();

            articles.add(article);
        }

        return createTestArticleSource(sourceId, articles);
    }

    private ArticleSource createTestArticleSource(String sourceId, List<Article> articles) {
        return new ArticleSource() {
            @Override
            public String getSourceId() {
                return sourceId;
            }

            @Override
            public List<Article> getArticles(PersistencyHandler persistencyHandler,
                                             Map<String, Article> previousArticlesMap,
                                             Map<String, Author> previousAuthorsMap) {
                return articles;
            }
        };
    }
}

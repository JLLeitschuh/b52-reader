/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources;

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CombinationArticleSourceTest {
    @Test
    public void testGetSourceId() {
        CombinationArticleSource combinationArticleSource = new CombinationArticleSource(new ArrayList<>());

        assertEquals("combination", combinationArticleSource.getSourceId());
    }

    @Test
    public void testGetArticlesEmpty() {
        CombinationArticleSource combinationArticleSource = new CombinationArticleSource(new ArrayList<>());

        List<Article> noArticles = combinationArticleSource.getArticles(null, null);
        assertEquals(0, noArticles.size());
    }

    @Test
    public void testGetArticlesRegular() {
        Author testAuthor = new Author(6, "Test Author");
        int articleCount1 = 2;
        int articleCount2 = 4;
        ArticleSource articleSource1 = createTestArticleSource("test-source-1", articleCount1, testAuthor);
        ArticleSource articleSource2 = createTestArticleSource("test-source-2", articleCount2, testAuthor);
        List<ArticleSource> articleSources = Arrays.asList(articleSource1, articleSource2);

        CombinationArticleSource combinationArticleSource = new CombinationArticleSource(articleSources);

        List<Article> articles = combinationArticleSource.getArticles(null, null);
        assertEquals(articleCount1 + articleCount2, articles.size());
    }

    private ArticleSource createTestArticleSource(String sourceId, int articleCount, Author testAuthor) {
        List<Article> articles = new ArrayList<>();

        for (int articleIndex = 0; articleIndex < articleCount; articleIndex++) {
            articles.add(new Article("https://test.org/article-" + articleIndex, sourceId, testAuthor,
                                     "title-" + articleIndex,
                                     Utilities.createDate(2017, Month.JUNE, articleIndex + 1),
                                     "text-" + articleIndex, 6 * articleIndex, articleIndex));
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
            public List<Article> getArticles(Map<String, Article> previousArticlesMap,
                                             Map<String, Author> previousAuthorsMap) {
                return articles;
            }
        };
    }
}

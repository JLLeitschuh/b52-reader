/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link ArticleFilter} class.
 */
public class ArticleFilterTest {
    private Article article;

    @Before
    public void setUp() {
        Author author = new Author("Cara Santa Maria", 1);

        article = new Article.Builder("url", "test", author, "Title of an article", null,
                                      "Some of the text of the article. Just a few lines.")
                .likes(496)
                .build();

        article.setStarred(true);
    }

    @Test
    public void testFilterByAuthor() {
        assertTrue(new ArticleFilter("author:").test(article));
        assertTrue(new ArticleFilter("author:Cara").test(article));
        //noinspection SpellCheckingInspection
        assertTrue(new ArticleFilter("author:anta").test(article));

        assertFalse(new ArticleFilter("author:Albert").test(article));
    }

    @Test
    public void testFilterByTitle() {
        assertTrue(new ArticleFilter("title:").test(article));
        //noinspection SpellCheckingInspection
        assertTrue(new ArticleFilter("title:Titl").test(article));
        assertTrue(new ArticleFilter("title:art").test(article));

        assertFalse(new ArticleFilter("title:Amazing").test(article));
    }

    @Test
    public void testFilterByState() {
        assertTrue(new ArticleFilter("is:").test(article));
        assertTrue(new ArticleFilter("is:starred").test(article));
        assertTrue(new ArticleFilter("is:unread").test(article));
        assertTrue(new ArticleFilter("is:nonsense").test(article));

        assertFalse(new ArticleFilter("is:unstarred").test(article));
        assertFalse(new ArticleFilter("is:read").test(article));
    }

    @Test
    public void testFilterCornerCases() {
        Article articleTwo = new Article.Builder("url", "test", null, null, null,
                                                 "Text")
                .build();

        articleTwo.setStarred(true);

        assertTrue(new ArticleFilter("").test(articleTwo));
        assertTrue(new ArticleFilter("nonsense").test(articleTwo));
        assertTrue(new ArticleFilter("author:Albert title:Relativity is:starred").test(articleTwo));
    }
}

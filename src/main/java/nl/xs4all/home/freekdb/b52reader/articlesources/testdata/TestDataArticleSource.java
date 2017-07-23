/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.articlesources.testdata;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.articlesources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

/**
 * Article source with test data.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class TestDataArticleSource implements ArticleSource {
    /**
     * Source id for this article source.
     */
    private static final String SOURCE_ID = "test";

    /**
     * The articles from this article source.
     */
    private List<Article> articles;

    @Override
    public String getSourceId() {
        return SOURCE_ID;
    }

    @Override
    public List<Article> getArticles(final PersistencyHandler persistencyHandler,
                                     final Map<String, Article> previousArticlesMap,
                                     final Map<String, Author> previousAuthorsMap) {
        if (articles == null) {
            articles = new ArrayList<>();

            final int year1 = 2012;
            final int dayOfMonth1 = 17;
            final int likes1 = 28;

            articles.add(
                Article.builder().url("http://www.huffingtonpost.com/2012/12/17/superstring-theory_n_2296195.html")
                    .sourceId(SOURCE_ID).author(persistencyHandler.getOrCreateAuthor("Cara Santa Maria"))
                    .title("WTF Is String Theory?")
                    .dateTime(Utilities.createDate(year1, Month.DECEMBER, dayOfMonth1))
                    .text("Have you ever heard the term string theory and wondered WTF it means? When it "
                          + "comes to theoretical physics, it seems like there are a lot of larger-than-life "
                          + "concepts that have made their way into our everyday conversations.")
                    .likes(likes1).recordId(1)
                    .build()
            );

            final int year2 = 2007;
            final int likes2 = 6;

            articles.add(
                Article.builder().url("http://www.haydenplanetarium.org/tyson/read/2007/04/02/the-cosmic-perspective")
                    .sourceId(SOURCE_ID).author(persistencyHandler.getOrCreateAuthor("Neil deGrasse Tyson"))
                    .title("The Cosmic Perspective")
                    .dateTime(Utilities.createDate(year2, Month.APRIL, 2))
                    .text("Long before anyone knew that the universe had a beginning, before we knew that "
                          + "the nearest large galaxy lies two and a half million light-years from Earth, "
                          + "before we knew how stars work or whether atoms exist, James Ferguson's "
                          + "enthusiastic introduction to his favorite science rang true.")
                    .likes(likes2).recordId(2)
                    .build()
            );
        }

        return articles;
    }
}

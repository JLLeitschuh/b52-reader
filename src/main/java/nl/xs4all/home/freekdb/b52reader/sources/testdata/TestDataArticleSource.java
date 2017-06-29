/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.testdata;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

public class TestDataArticleSource implements ArticleSource {
    private static final String SOURCE_ID = "test";

    private List<Article> articles;

    @Override
    public String getSourceId() {
        return SOURCE_ID;
    }

    @Override
    public List<Article> getArticles(PersistencyHandler persistencyHandler, Map<String, Article> previousArticlesMap,
                                     Map<String, Author> previousAuthorsMap) {
        if (articles == null) {
            articles = new ArrayList<>();

            articles.add(
                    new Article.Builder("http://www.huffingtonpost.com/2012/12/17/superstring-theory_n_2296195.html",
                                        SOURCE_ID,
                                        persistencyHandler.getOrCreateAuthor("Cara Santa Maria"),
                                        "WTF Is String Theory?",
                                        Utilities.createDate(2012, Month.DECEMBER, 17),
                                        "Have you ever heard the term string theory and wondered WTF it means? When it comes " +
                                        "to theoretical physics, it seems like there are a lot of larger-than-life concepts that " +
                                        "have made their way into our everyday conversations.")
                            .likes(28)
                            .recordId(1)
                            .build()
            );

            articles.add(
                    new Article.Builder("http://www.haydenplanetarium.org/tyson/read/2007/04/02/the-cosmic-perspective",
                                        SOURCE_ID,
                                        persistencyHandler.getOrCreateAuthor("Neil deGrasse Tyson"),
                                        "The Cosmic Perspective",
                                        Utilities.createDate(2007, Month.APRIL, 2),
                                        "Long before anyone knew that the universe had a beginning, before we knew that the " +
                                        "nearest large galaxy lies two and a half million light-years from Earth, before we knew " +
                                        "how stars work or whether atoms exist, James Ferguson's enthusiastic introduction to his " +
                                        "favorite science rang true.")
                            .likes(6)
                            .recordId(2)
                            .build()
            );
        }

        return articles;
    }
}

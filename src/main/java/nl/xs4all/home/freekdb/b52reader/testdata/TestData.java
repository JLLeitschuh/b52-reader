/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.testdata;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;

public class TestData {
    public static List<Article> getTestArticles() {
        return Arrays.asList(
                new Article("1", "http://www.huffingtonpost.com/2012/12/17/superstring-theory_n_2296195.html",
                            new Author("Cara Santa Maria"), "WTF Is String Theory?", new Date(),
                            "Have you ever heard the term string theory and wondered WTF it means? When it comes " +
                            "to theoretical physics, it seems like there are a lot of larger-than-life concepts that " +
                            "have made their way into our everyday conversations."),

                new Article("2", "http://www.haydenplanetarium.org/tyson/read/2007/04/02/the-cosmic-perspective",
                            new Author("Neil deGrasse Tyson"), "The Cosmic Perspective", new Date(),
                            "Long before anyone knew that the universe had a beginning, before we knew that the " +
                            "nearest large galaxy lies two and a half million light-years from Earth, before we knew " +
                            "how stars work or whether atoms exist, James Ferguson's enthusiastic introduction to his " +
                            "favorite science rang true.")
        );
    }
}

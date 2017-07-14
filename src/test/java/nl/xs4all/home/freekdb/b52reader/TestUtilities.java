/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader;

import java.util.ArrayList;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;

/**
 * Class with a generic method to create six articles for testing.
 */
public class TestUtilities {
    /**
     * Create a list with six articles for testing.
     *
     * @return a list with six articles for testing.
     */
    public static List<Article> getSixTestArticles() {
        List<Article> articles = new ArrayList<>();

        for (int articleIndex = 1; articleIndex <= 6; articleIndex++) {
            Article article = Article.builder()
                    .url("u" + articleIndex)
                    .sourceId("s" + articleIndex)
                    .title("Title" + articleIndex)
                    .text("Text " + articleIndex + ".")
                    .build();

            if (articleIndex == 1 || articleIndex == 3) {
                article.setStarred(true);
                article.setRead(true);
            }

            if (articleIndex == 3) {
                article.setArchived(true);
            }

            articles.add(article);
        }

        return articles;
    }
}

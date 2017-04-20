/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources;

import java.util.List;

import nl.xs4all.home.freekdb.b52reader.model.Article;

public interface ArticleSource {
    List<Article> getArticles();
}

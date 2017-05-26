/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

public class Author {
    private Integer id;
    private final String name;
    private final String normalizedName;

    private final List<Article> articles = new ArrayList<>();

    private long totalWordCount;

    public Author(int id, String name) {
        this.id = id;
        this.name = name;
        this.normalizedName = Utilities.normalize(name);
    }

    @SuppressWarnings("unused")
    public Author addArticle(Article article) {
        articles.add(article);
        totalWordCount += article.getWordCount();

        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    @SuppressWarnings("unused")
    public List<Article> getArticles() {
        return articles;
    }

    @SuppressWarnings("unused")
    public long getTotalWordCount() {
        return totalWordCount;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Author other = (Author) obj;

        return Objects.equals(id, other.id) &&
               Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

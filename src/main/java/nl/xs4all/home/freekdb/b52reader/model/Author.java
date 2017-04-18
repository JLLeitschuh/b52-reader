package nl.xs4all.home.freekdb.b52reader.model;

import java.util.ArrayList;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

public class Author {
    private final String name;
    private final String normalizedName;

    private final List<Article> articles = new ArrayList<>();

    private long totalWordCount;

    public Author(String name) {
        this.name = name;
        this.normalizedName = Utilities.normalize(name);
    }

    public Author addArticle(Article article) {
        articles.add(article);
        totalWordCount += article.getWordCount();

        return this;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public long getTotalWordCount() {
        return totalWordCount;
    }
}

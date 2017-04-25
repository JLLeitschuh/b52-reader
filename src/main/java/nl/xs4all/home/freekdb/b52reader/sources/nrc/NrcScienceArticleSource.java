/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.nrc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.sources.ArticleSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * NRC Handelsblad (a Dutch newspaper) article source.
 */
public class NrcScienceArticleSource implements ArticleSource {
    @Override
    public List<Article> getArticles(Map<String, Article> previousArticlesMap, Map<String, Author> previousAuthorsMap) {
        List<Article> newArticles = new ArrayList<>();

        try {
            Document articleListDocument = Jsoup.connect("https://www.nrc.nl/sectie/wetenschap/").get();
            Elements articleElements = articleListDocument.select(".nmt-item__link");

            Author defaultAuthor = new Author(3, "NRC science");

            for (Element articleElement : articleElements) {
                String url = "https://www.nrc.nl/" + articleElement.attr("href");
                String title = articleElement.getElementsByClass("nmt-item__headline").text();
                String text = articleElement.getElementsByClass("nmt-item__teaser").text();

//                Article article;
//                if (previousArticlesMap.containsKey(url)) {
//                    article = previousArticlesMap.get(url);
//                    // todo: Does the previous article need an update?
//                    // todo: Use the author object from the database or the new one?
//                }
//                else {
//                    Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
//                    article = new Article(-1 - newArticles.size(), url, author, title, new Date(), text, 1234);
//                }

                // For the moment: do not use the article objects that were created from the database, because we want
                // to be able to compare the articles in memory to the stored articles to see whether an update of a
                // stored article is needed.

                Author author = previousAuthorsMap.getOrDefault(defaultAuthor.getName(), defaultAuthor);
                Article article = new Article(-1 - newArticles.size(), url, author, title, new Date(), text, 1234);

                newArticles.add(article);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newArticles;
    }
}

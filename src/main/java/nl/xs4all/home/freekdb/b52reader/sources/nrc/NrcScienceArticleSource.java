/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources.nrc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public List<Article> getArticles() {
        List<Article> articles = new ArrayList<>();

        try {
            Document articleListDocument = Jsoup.connect("https://www.nrc.nl/sectie/wetenschap/").get();
            Elements articleElements = articleListDocument.select(".nmt-item__link");

            Author author = new Author(3, "NRC science");

            for (Element articleElement : articleElements) {
                String url = "https://www.nrc.nl/" + articleElement.attr("href");
                String title = articleElement.getElementsByClass("nmt-item__headline").text();
                String text = articleElement.getElementsByClass("nmt-item__teaser").text();

                articles.add(new Article(articles.size(), url, author, title, new Date(), text, 1234));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return articles;
    }
}

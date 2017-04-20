/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.Arrays;
import java.util.function.Predicate;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

public class ArticleFilter implements Predicate<Article> {
    private String normalizedAuthorName;
    private String normalizedTitle;
    private Boolean starred;
    private Boolean read;

    ArticleFilter(String filterText) {
        Arrays.stream(filterText.split(" ")).forEach(filterPart -> {
            if (filterPart.startsWith("from:") && filterPart.length() > 5) {
                normalizedAuthorName = Utilities.normalize(filterPart.substring(5));
                System.out.println("Filter on author: " + normalizedAuthorName);
            } else if (filterPart.startsWith("title:") && filterPart.length() > 6) {
                normalizedTitle = Utilities.normalize(filterPart.substring(6));
                System.out.println("Filter on title: " + normalizedTitle);
            } else if (filterPart.startsWith("is:") && filterPart.length() > 3) {
                String state = filterPart.substring(3).toLowerCase();
                if ("starred".equals(state) || "unstarred".equals(state))
                    starred = "starred".equals(state);
                if (state.contains("read") || "unread".equals(state))
                    read = "read".equals(state);
                System.out.println("Filter on state: " + state);
            } else if (!"".equals(filterPart.trim())) {
                // Incomplete filter parts will be ignored.
                System.out.println("Filter not understood: " + filterPart);
            }
        });
    }

    @Override
    public boolean test(Article article) {
        boolean authorOk = normalizedAuthorName == null
                           || article.getAuthor() == null
                           || article.getAuthor().getNormalizedName().contains(normalizedAuthorName);

        boolean titleOk = normalizedTitle == null
                          || article.getTitle() == null
                          || article.getNormalizedTitle().contains(normalizedTitle);

        boolean stateOk = (starred == null || starred == article.isStarred())
                          && (read == null || read == article.isRead());

        return authorOk && titleOk && stateOk;
    }
}

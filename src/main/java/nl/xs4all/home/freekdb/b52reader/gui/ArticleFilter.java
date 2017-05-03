/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.Arrays;
import java.util.function.Predicate;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArticleFilter implements Predicate<Article> {
    private static final String AUTHOR_PREFIX = "author:";
    private static final String TITLE_PREFIX = "title:";
    private static final String IS_PREFIX = "is:";

    private static final String STARRED_STATE = "starred";
    private static final String UNSTARRED_STATE = "unstarred";
    private static final String READ_STATE = "read";
    private static final String UNREAD_STATE = "unread";

    private static final Logger logger = LogManager.getLogger(ArticleFilter.class);

    private String normalizedAuthorName;
    private String normalizedTitle;
    private Boolean starred;
    private Boolean read;

    ArticleFilter(String filterText) {
        Arrays.stream(filterText.split(" ")).forEach(filterPart -> {
            if (filterPart.startsWith(AUTHOR_PREFIX) && filterPart.length() > AUTHOR_PREFIX.length()) {
                normalizedAuthorName = Utilities.normalize(filterPart.substring(AUTHOR_PREFIX.length()));
                logger.debug("Filter on author: {}", normalizedAuthorName);
            } else if (filterPart.startsWith(TITLE_PREFIX) && filterPart.length() > TITLE_PREFIX.length()) {
                normalizedTitle = Utilities.normalize(filterPart.substring(TITLE_PREFIX.length()));
                logger.debug("Filter on title: {}", normalizedTitle);
            } else if (filterPart.startsWith(IS_PREFIX) && filterPart.length() > IS_PREFIX.length()) {
                String state = filterPart.substring(IS_PREFIX.length()).toLowerCase();
                if (STARRED_STATE.equals(state) || UNSTARRED_STATE.equals(state))
                    starred = STARRED_STATE.equals(state);
                if (state.equals(READ_STATE) || UNREAD_STATE.equals(state))
                    read = READ_STATE.equals(state);
                logger.debug("Filter on state: {}", state);
            } else if (!"".equals(filterPart.trim())) {
                // Incomplete filter parts will be ignored.
                logger.debug("Filter not understood: {}", filterPart);
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

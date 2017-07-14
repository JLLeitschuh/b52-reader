/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.Arrays;
import java.util.function.Predicate;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filters articles using a query syntax inspired by Gmail. The following options are supported:
 * <ul>
 * <li>"author:Cara" matches (part of) an author's name (case insensitive);</li>
 * <li>"title:Cosmic" matches (part of) a title (case insensitive);</li>
 * <li>"is:starred" matches starred articles, and "is:" also works with unstarred, read, and unread.</li>
 * </ul>
 * You can also combine them: "author:Cara is:starred is:read" will show articles by Cara that are starred and read.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class ArticleFilter implements Predicate<Article> {
    /**
     * Prefix for filtering on an author's name (case insensitive).
     */
    private static final String AUTHOR_PREFIX = "author:";

    /**
     * Prefix for filtering on a title (case insensitive).
     */
    private static final String TITLE_PREFIX = "title:";

    /**
     * Prefix for filtering on the article's state: starred or unstarred, and read or unread.
     */
    private static final String STATE_PREFIX = "is:";

    /**
     * Keyword to filter for starred articles.
     */
    private static final String STARRED_STATE = "starred";

    /**
     * Keyword to filter for unstarred articles.
     */
    private static final String UNSTARRED_STATE = "unstarred";

    /**
     * Keyword to filter for read articles.
     */
    private static final String READ_STATE = "read";

    /**
     * Keyword to filter for unread articles.
     */
    private static final String UNREAD_STATE = "unread";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The normalized author name (lower case and accents removed) or null (to match all authors).
     */
    private String normalizedAuthorName;

    /**
     * The normalized title (lower case and accents removed) or null (to match all titles).
     */
    private String normalizedTitle;

    /**
     * Whether articles should be starred (true), unstarred (false), or both states are good (null).
     */
    private Boolean starred;

    /**
     * Whether articles should be read (true), unread (false), or both states are good (null).
     */
    private Boolean read;

    /**
     * Construct and initialize an article filter.
     *
     * @param filterText the filter text that will be parsed for one or more filter parts.
     */
    public ArticleFilter(final String filterText) {
        Arrays.stream(filterText.split(" ")).forEach(filterPart -> {
            if (isFilterType(AUTHOR_PREFIX, filterPart)) {
                normalizedAuthorName = Utilities.normalize(filterPart.substring(AUTHOR_PREFIX.length()));
                logger.debug("Filter on author: {}", normalizedAuthorName);
            } else if (isFilterType(TITLE_PREFIX, filterPart)) {
                normalizedTitle = Utilities.normalize(filterPart.substring(TITLE_PREFIX.length()));
                logger.debug("Filter on title: {}", normalizedTitle);
            } else if (isFilterType(STATE_PREFIX, filterPart)) {
                handleStateFilter(filterPart);
            } else if (!"".equals(filterPart.trim())) {
                // Incomplete filter parts will be ignored.
                logger.debug("Filter not understood: {}", filterPart);
            }
        });
    }

    /**
     * Check whether this filter part matches the specified type of filter.
     *
     * @param typePrefix the type prefix that should be matched (like <code>AUTHOR_PREFIX</code>).
     * @param filterPart the part of the filter that is being matched.
     * @return whether this filter part matches the specified type of filter.
     */
    private boolean isFilterType(final String typePrefix, final String filterPart) {
        return filterPart.startsWith(typePrefix) && filterPart.length() > typePrefix.length();
    }

    /**
     * Handle filtering related to the state of the article (starred or read).
     *
     * @param filterPart the part of the filter related to state.
     */
    private void handleStateFilter(final String filterPart) {
        final String state = filterPart.substring(STATE_PREFIX.length()).toLowerCase();

        if (STARRED_STATE.equals(state) || UNSTARRED_STATE.equals(state)) {
            starred = STARRED_STATE.equals(state);
        } else if (READ_STATE.equals(state) || UNREAD_STATE.equals(state)) {
            read = READ_STATE.equals(state);
        }

        logger.debug("Filter on state: {}", state);
    }

    /**
     * Test whether a specified article matches the filter.
     *
     * @param article the article to match against the filter.
     * @return whether the article matches the filter.
     */
    @Override
    public boolean test(final Article article) {
        return isAuthorOk(article) && isTitleOk(article) && isStateOk(article);
    }

    /**
     * Test whether a specified article matches the author filter.
     *
     * @param article the article to match against the author filter.
     * @return whether the article matches the author filter.
     */
    private boolean isAuthorOk(final Article article) {
        return normalizedAuthorName == null
                           || article.getAuthor() == null
                           || article.getAuthor().getNormalizedName().contains(normalizedAuthorName);
    }

    /**
     * Test whether a specified article matches the title filter.
     *
     * @param article the article to match against the title filter.
     * @return whether the article matches the title filter.
     */
    private boolean isTitleOk(final Article article) {
        return normalizedTitle == null
               || article.getTitle() == null
               || article.getNormalizedTitle().contains(normalizedTitle);
    }

    /**
     * Test whether a specified article matches the state filter.
     *
     * @param article the article to match against the state filter.
     * @return whether the article matches the state filter.
     */
    private boolean isStateOk(final Article article) {
        return (starred == null || starred == article.isStarred())
               && (read == null || read == article.isRead());
    }
}

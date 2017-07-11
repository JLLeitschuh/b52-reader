/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Builder;
import lombok.Data;

/**
 * Class containing all relevant (meta) data about articles.
 *
 * Sonar check S2065 (fields in non-serializable classes should not be "transient") is disabled because by marking the
 * starred, read, and archived fields as transient, these fields are excluded for the equals and hashCode methods.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@Data()
@Builder
@SuppressWarnings("squid:S2065")
public class Article {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * URL where the article can be found.
     */
    private final String url;

    /**
     * Identifier of the article source.
     */
    private final String sourceId;

    /**
     * Main author of the article.
     */
    private final Author author;

    /**
     * Title of the article.
     */
    private final String title;

    /**
     * Normalized title which is used for searching.
     */
    private final String normalizedTitle;

    /**
     * Number of words in the title.
     */
    private final long titleWordCount;

    /**
     * Date/time of publication.
     */
    private final ZonedDateTime dateTime;

    /**
     * First part of the article text.
     */
    private final String text;

    /**
     * Number of words in the first part of the article text.
     */
    private final long textWordCount;

    /**
     * Number of likes for the article.
     */
    private final int likes;

    /**
     * Database record id where this object is stored.
     */
    @SuppressWarnings("squid:S1450")
    private int recordId;

    /**
     * Whether the user has starred the article.
     */
    private transient boolean starred;

    /**
     * Whether the user has marked the article as read.
     */
    private transient boolean read;

    /**
     * Whether the user has archived the article.
     */
    private transient boolean archived;


    // todo: Replace this hand written Builder class below by the one Lombok creates.

    /**
     * Builder for article objects.
     */
    public static class Builder {
        /**
         * URL where the article can be found.
         */
        private final String url;

        /**
         * Identifier of the article source.
         */
        private final String sourceId;

        /**
         * Main author of the article.
         */
        private final Author author;

        /**
         * Title of the article.
         */
        private final String title;

        /**
         * Date/time of publication.
         */
        private final ZonedDateTime dateTime;

        /**
         * First part of the article text.
         */
        private final String text;

        /**
         * Number of likes for the article.
         */
        private int likes;

        /**
         * Database record id where the article object is stored.
         */
        private int recordId;

        /**
         * Whether the user has starred the article.
         */
        private boolean starred;

        /**
         * Whether the user has marked the article as read.
         */
        private boolean read;

        /**
         * Whether the user has archived the article.
         */
        private boolean archived;

        /**
         * Create an article builder.
         *
         * @param url      URL where the article can be found.
         * @param sourceId identifier of the article source.
         * @param author   main author of the article.
         * @param title    title of the article.
         * @param dateTime date/time of publication.
         * @param text     first part of the article text.
         */
        public Builder(final String url, final String sourceId, final Author author, final String title,
                       final ZonedDateTime dateTime, final String text) {
            this.url = url;
            this.sourceId = sourceId;
            this.author = author;
            this.title = title;
            this.dateTime = dateTime;
            this.text = text;
        }

        /**
         * Add the number of likes.
         *
         * @param likes the number of likes.
         * @return the updated builder.
         */
        public Builder likes(final int likes) {
            this.likes = likes;

            return this;
        }

        /**
         * Add the database record id.
         *
         * @param recordId the database record id.
         * @return the updated builder.
         */
        public Builder recordId(final int recordId) {
            this.recordId = recordId;

            return this;
        }

        /**
         * Set the starred flag.
         *
         * @param starred the starred flag.
         * @return the updated builder.
         */
        public Builder starred(final boolean starred) {
            this.starred = starred;

            return this;
        }

        /**
         * Set the read flag.
         *
         * @param read the read flag.
         * @return the updated builder.
         */
        public Builder read(final boolean read) {
            this.read = read;

            return this;
        }

        /**
         * Set the archived flag.
         *
         * @param archived the archived flag.
         * @return the updated builder.
         */
        public Builder archived(final boolean archived) {
            this.archived = archived;

            return this;
        }

        /**
         * Build the new article.
         *
         * @return the article.
         */
        public Article build() {
            return new Article(url, sourceId, author, title, Utilities.normalize(title),
                               Utilities.calculateWordCount(title), dateTime, text, Utilities.calculateWordCount(text),
                               likes, recordId, starred, read, archived);
        }
    }


    /**
     * Create an article based on the database record.
     *
     * @param resultSet database record.
     * @param authors known authors.
     * @return the article.
     */
    public static Article createArticleFromDatabase(final ResultSet resultSet, final List<Author> authors) {
        Article article = null;

        try {
            final String url = resultSet.getString("url");
            final String sourceId = resultSet.getString("source_id");

            final int authorId = resultSet.getInt("author_id");
            final Author author = authors.stream()
                    .filter(anAuthor -> anAuthor.getRecordId() == authorId)
                    .findFirst()
                    .orElse(null);

            final String title = resultSet.getString("title");
            final Timestamp timestamp = resultSet.getTimestamp("date_time");
            final ZonedDateTime dateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC);
            final String text = resultSet.getString("text");
            final int likes = resultSet.getInt("likes");

            final int recordId = resultSet.getInt("id");

            article = new Article.Builder(url, sourceId, author, title, dateTime, text)
                    .likes(likes)
                    .recordId(recordId)
                    .build();

            article.setStarred(resultSet.getBoolean("starred"));
            article.setRead(resultSet.getBoolean("read"));
            article.setArchived(resultSet.getBoolean("archived"));
        } catch (final SQLException e) {
            logger.error("Exception while creating an article from a database record.", e);
        }

        return article;
    }

    /**
     * Get the number of words in the title and the first part of the article text.
     *
     * @return the number of words in the title and the first part of the article text.
     */
    long getWordCount() {
        return titleWordCount + textWordCount;
    }

    /**
     * Determine whether the meta data flags starred, read, and archived are equal for two articles.
     *
     * @param other the article to compare with.
     * @return whether the meta data flags starred, read, and archived are equal.
     */
    public boolean metadataEquals(final Article other) {
        return Objects.equals(starred, other.starred) &&
               Objects.equals(read, other.read) &&
               Objects.equals(archived, other.archived);
    }
}

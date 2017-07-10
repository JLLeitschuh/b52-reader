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

/**
 * Class containing all relevant (meta) data about articles.
 */
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
        public Builder(String url, String sourceId, Author author, String title, ZonedDateTime dateTime, String text) {
            this.url = url;
            this.sourceId = sourceId;
            this.author = author;
            this.title = title;
            this.dateTime = dateTime;
            this.text = text;
        }

        public Builder likes(int likes) {
            this.likes = likes;

            return this;
        }

        public Builder recordId(int recordId) {
            this.recordId = recordId;

            return this;
        }

        public Builder starred(boolean starred) {
            this.starred = starred;

            return this;
        }

        public Builder read(boolean read) {
            this.read = read;

            return this;
        }

        public Builder archived(boolean archived) {
            this.archived = archived;

            return this;
        }

        public Article build() {
            return new Article(this);
        }
    }


    private Article(Builder builder) {
        this.url = builder.url;
        this.sourceId = builder.sourceId;
        this.author = builder.author;
        this.title = builder.title;
        this.normalizedTitle = Utilities.normalize(title);
        this.titleWordCount = Utilities.calculateWordCount(title);
        this.dateTime = builder.dateTime;
        this.text = builder.text;
        this.textWordCount = Utilities.calculateWordCount(text);
        this.likes = builder.likes;

        this.recordId = builder.recordId;

        this.setStarred(builder.starred);
        this.setRead(builder.read);
        this.setArchived(builder.archived);
    }

    public static Article createArticleFromDatabase(ResultSet resultSet, List<Author> authors) {
        Article article = null;

        try {
            String url = resultSet.getString("url");
            String sourceId = resultSet.getString("source_id");

            int authorId = resultSet.getInt("author_id");
            Author author = authors.stream()
                    .filter(anAuthor -> anAuthor.getRecordId() == authorId)
                    .findFirst()
                    .orElse(null);

            String title = resultSet.getString("title");
            Timestamp timestamp = resultSet.getTimestamp("date_time");
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC);
            String text = resultSet.getString("text");
            int likes = resultSet.getInt("likes");

            int recordId = resultSet.getInt("id");

            article = new Article.Builder(url, sourceId, author, title, dateTime, text)
                    .likes(likes)
                    .recordId(recordId)
                    .build();

            article.setStarred(resultSet.getBoolean("starred"));
            article.setRead(resultSet.getBoolean("read"));
            article.setArchived(resultSet.getBoolean("archived"));
        } catch (SQLException e) {
            logger.error("Exception while creating an article from a database record.", e);
        }

        return article;
    }

    public String getUrl() {
        return url;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Author getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public String getText() {
        return text;
    }

    long getWordCount() {
        return titleWordCount + textWordCount;
    }

    public int getLikes() {
        return likes;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public String toString() {
        return "[" + recordId + "] Article \"" + title + "\" by " + author;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Article)) {
            return false;
        }

        Article other = (Article) obj;

        return Objects.equals(url, other.url) &&
               Objects.equals(sourceId, other.sourceId) &&
               Objects.equals(author, other.author) &&
               Objects.equals(title, other.title) &&
               Objects.equals(dateTime, other.dateTime) &&
               Objects.equals(text, other.text) &&
               Objects.equals(likes, other.likes);
    }

    /**
     * Determine whether the meta data flags starred, read, and archived are equal for two articles.
     *
     * @param other the article to compare with.
     * @return whether the meta data flags starred, read, and archived are equal.
     */
    public boolean metadataEquals(Article other) {
        return Objects.equals(starred, other.starred) &&
               Objects.equals(read, other.read) &&
               Objects.equals(archived, other.archived);
    }

    /**
     * Generate a hash code value for this article.
     *
     * @return a hash code value for this article.
     */
    @Override
    public final int hashCode() {
        return Objects.hash(url, sourceId, author, title, dateTime, text, likes);
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Article {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger();

    private final String url;
    private final String sourceId;
    private final Author author;
    private final String title;
    private final String normalizedTitle;
    private final long titleWordCount;
    private final Date dateTime;
    private final String text;
    private final long textWordCount;
    private final int likes;

    private int recordId;

    private boolean starred;
    private boolean read;
    private boolean archived;


    public static class Builder {
        private String url;
        private String sourceId;
        private Author author;
        private String title;
        private Date dateTime;
        private String text;
        private int likes;
        private int recordId;

        public Builder(String url, String sourceId, Author author, String title, Date dateTime, String text) {
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
            Date dateTime = resultSet.getTimestamp("date_time");
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

    public Date getDateTime() {
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

    public boolean metadataEquals(Article other) {
        return Objects.equals(starred, other.starred) &&
               Objects.equals(read, other.read) &&
               Objects.equals(archived, other.archived);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(url, sourceId, author, title, dateTime, text, likes);
    }
}

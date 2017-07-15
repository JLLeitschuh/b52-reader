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
 * <p>
 * Sonar check S2065 (fields in non-serializable classes should not be "transient") is disabled because by marking the
 * starred, read, and archived fields as transient, these fields are excluded for the equals and hashCode methods.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@Data
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
     *
     * todo: make this field final as well and replace objects when this field needs to change?
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

    /**
     * Construct an article.
     *
     * @param url      URL where the article can be found.
     * @param sourceId identifier of the article source.
     * @param author   main author of the article.
     * @param title    title of the article.
     * @param dateTime date/time of publication.
     * @param text     first part of the article text.
     * @param likes    number of likes for the article.
     * @param recordId database record id where this object is stored.
     * @param starred  whether the user has starred the article.
     * @param read     whether the user has marked the article as read.
     * @param archived whether the user has archived the article.
     */
    @Builder
    @SuppressWarnings("squid:S00107")
    public Article(final String url, final String sourceId, final Author author, final String title,
                   final ZonedDateTime dateTime, final String text, final int likes, final int recordId,
                   final boolean starred, final boolean read, final boolean archived) {
        this.url = url;
        this.sourceId = sourceId;
        this.author = author;
        this.title = title;
        this.normalizedTitle = Utilities.normalize(title);
        this.titleWordCount = Utilities.calculateWordCount(title);
        this.dateTime = dateTime;
        this.text = text;
        this.textWordCount = Utilities.calculateWordCount(text);
        this.likes = likes;
        this.recordId = recordId;
        this.starred = starred;
        this.read = read;
        this.archived = archived;
    }

    /**
     * Create an article based on the database record.
     *
     * @param resultSet database record.
     * @param authors   known authors.
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

            article = Article.builder().url(url).sourceId(sourceId).author(author).title(title).dateTime(dateTime)
                    .text(text).likes(likes).recordId(recordId)
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
        return Objects.equals(starred, other.starred)
               && Objects.equals(read, other.read)
               && Objects.equals(archived, other.archived);
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for the entire application.
 */
public class Utilities {
    /**
     * Private constructor to hide the implicit public one, since this class is not meant to be instantiated.
     */
    private Utilities() {
        // Should not be called.
    }

    /**
     * Normalize a string by stripping all accents and converting it to lowercase.
     *
     * @param text the text to normalized.
     * @return the normalized version of text.
     */
    public static String normalize(final String text) {
        return text != null ? StringUtils.stripAccents(text).toLowerCase() : null;
    }

    public static int calculateWordCount(String text) {
        return text == null || text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }

    public static Icon getIconResource(String iconFileName) {
        URL iconFileUrl = Utilities.class.getClassLoader().getResource(iconFileName);

        return (iconFileUrl != null) ? new ImageIcon(iconFileUrl.getFile()) : null;
    }

    public static String countAndWord(int count, String word) {
        return count + " " + word + (count != 1 ? "s" : "");
    }

    public static ZonedDateTime createDate(int year, Month month, int dayOfMonth) {
        return ZonedDateTime.of(year, month.getValue(), dayOfMonth, 0, 0, 0, 0,
                                ZoneOffset.UTC);
    }

    public static void ignoreStandardErrorStream() {
        // Ignore characters written to the standard error stream, since the dj-nativeswing library sometimes has
        // difficulties with the contents of the clipboard, resulting in ClassNotFoundException-s.
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Ignore it.
            }
        }));
    }
    
    /**
     * If there is previous data available for this article, copy the fields that are managed by the B52 reader.
     *
     * @param article         the new article.
     * @param previousArticle the previous article (or null if not available).
     */
    public static void copyPreviousDataIfAvailable(Article article, Article previousArticle) {
        if (previousArticle != null) {
            article.setStarred(previousArticle.isStarred());
            article.setRead(previousArticle.isRead());
            article.setArchived(previousArticle.isArchived());
        }
    }
}

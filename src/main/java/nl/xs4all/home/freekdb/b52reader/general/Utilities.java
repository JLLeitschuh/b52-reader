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
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
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

    /**
     * Estimate the number of words in a specific text.
     *
     * @param text text to estimate the word count for.
     * @return estimation of number of words in a text.
     */
    public static int estimateWordCount(final String text) {
        return text == null || text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }

    /**
     * Get an icon from a specific resource file.
     *
     * @param iconFileName file name of icon resource.
     * @return icon from a resource file.
     */
    public static Icon getIconResource(final String iconFileName) {
        final URL iconFileUrl = Utilities.class.getClassLoader().getResource(iconFileName);

        return (iconFileUrl != null) ? new ImageIcon(iconFileUrl.getFile()) : null;
    }

    /**
     * Create a string with a specific count, a space, and a specific word. If count is not equal to one, an 's'
     * character is added to the word. Some examples: "0 bikes", "1 programmer", and "6 guitarists".
     *
     * @param count number of times that applies to the word.
     * @param word word to include in the string (the plural form should require the addition of an 's' character).
     * @return "[count] [word](s)" (if count is not equal to one, an 's' character is added).
     */
    public static String countAndWord(final int count, final String word) {
        return count + " " + word + (count != 1 ? "s" : "");
    }

    /**
     * Create a zoned date(/time). The time part is empty.
     *
     * @param year year.
     * @param month month.
     * @param dayOfMonth day of the month.
     * @return <code>ZonedDateTime</code> object.
     */
    public static ZonedDateTime createDate(final int year, final Month month, final int dayOfMonth) {
        return ZonedDateTime.of(year, month.getValue(), dayOfMonth, 0, 0, 0, 0,
                                ZoneOffset.UTC);
    }

    /**
     * Ignore characters written to the standard error stream. This is used because the dj-nativeswing library sometimes
     * has difficulties with the contents of the clipboard, resulting in <code>ClassNotFoundException</code>s.
     */
    public static void ignoreStandardErrorStream() {
        // Ignore characters written to the standard error stream, since the dj-nativeswing library sometimes has
        // difficulties with the contents of the clipboard, resulting in ClassNotFoundException-s.
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
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
    public static void copyPreviousDataIfAvailable(final Article article, final Article previousArticle) {
        if (previousArticle != null) {
            article.setStarred(previousArticle.isStarred());
            article.setRead(previousArticle.isRead());
            article.setArchived(previousArticle.isArchived());
        }
    }
}

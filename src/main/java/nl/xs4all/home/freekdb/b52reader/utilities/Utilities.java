/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import nl.xs4all.home.freekdb.b52reader.model.Article;
import org.apache.commons.lang3.StringUtils;

public class Utilities {
    /**
     * Private constructor to hide the implicit public one.
     */
    private Utilities() {
        // Should not be called.
    }
    
    public static String normalize(String text) {
        return StringUtils.stripAccents(text).toLowerCase();
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

    public static Date createDate(int year, Month month, int dayOfMonth) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, 0, 0);

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
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

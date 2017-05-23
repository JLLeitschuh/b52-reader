/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.utilities;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;

public class Utilities {
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
}

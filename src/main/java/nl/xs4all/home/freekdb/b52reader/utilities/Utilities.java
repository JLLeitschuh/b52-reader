package nl.xs4all.home.freekdb.b52reader.utilities;

import org.apache.commons.lang3.StringUtils;

public class Utilities {
    public static String normalize(String text) {
        return StringUtils.stripAccents(text).toLowerCase();
    }

    public static int calculateWordCount(String text) {
        return text == null || text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }
}

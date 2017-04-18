package nl.xs4all.home.freekdb.b52reader.utilities;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Utilities {
    // $NON-NLS-1$
    private static final Pattern ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String normalize(String text) {
        //return Normalizer.normalize(text, Normalizer.Form.NFD).toLowerCase();

        return stripAccents(text).toLowerCase();
    }

    // Modified from:
    // https://commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/StringUtils.html#L847
    private static String stripAccents(String text) {
        String result = null;

        if (text != null) {
            StringBuilder decomposed = new StringBuilder(Normalizer.normalize(text, Normalizer.Form.NFD));

            convertRemainingAccentCharacters(decomposed);

            // Note that this doesn't correctly remove ligatures...
            result = ACCENTS_PATTERN.matcher(decomposed).replaceAll("");
        }

        return result;
    }

    private static void convertRemainingAccentCharacters(StringBuilder decomposed) {
        for (int characterIndex = 0; characterIndex < decomposed.length(); characterIndex++) {
            if (decomposed.charAt(characterIndex) == '\u0141') {
                decomposed.setCharAt(characterIndex, 'L');
            } else if (decomposed.charAt(characterIndex) == '\u0142') {
                decomposed.setCharAt(characterIndex, 'l');
            }
        }
    }

    public static int calculateWordCount(String text) {
        return text == null || text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }
}

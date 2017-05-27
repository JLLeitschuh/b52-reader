/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.utilities;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.time.Month;

import javax.swing.Icon;

import nl.xs4all.home.freekdb.b52reader.model.Article;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the {@link Utilities} class.
 */
public class UtilitiesTest {
    @Test
    public void testPrivateConstructor() throws ReflectiveOperationException {
        Constructor<Utilities> constructor = Utilities.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Utilities instance = constructor.newInstance();

        assertNotNull(instance);
        assertEquals(Utilities.class, instance.getClass());
    }

    @Test
    public void testNormalize() {
        assertEquals("bogdan", Utilities.normalize("Bogdán"));
    }

    @Test
    public void testCalculateWordCount() {
        assertEquals(6, Utilities.calculateWordCount("This line is a simple test..._;-)"));
        assertEquals(0, Utilities.calculateWordCount(null));
        assertEquals(0, Utilities.calculateWordCount(""));
    }

    @Test
    public void testGetIconResource() {
        Icon starIcon = Utilities.getIconResource("32x32-Full_Star_Yellow.png");
        assertNotNull(starIcon);
        assertEquals(32, starIcon.getIconHeight());

        assertEquals(null, Utilities.getIconResource("nonsense"));
    }

    @Test
    public void testCountAndWord() {
        assertEquals("1 programmer", Utilities.countAndWord(1, "programmer"));
        assertEquals("6 guitarists", Utilities.countAndWord(6, "guitarist"));
    }

    @Test
    public void testCreateDate() {
        assertEquals(-2761606408000L, Utilities.createDate(1882, Month.JUNE, 28).getTime());
    }

    @Test
    public void testIgnoreStandardErrorStream() {
        PrintStream regularSystemErrorStream = System.err;

        try {
            Utilities.ignoreStandardErrorStream();
            System.err.println("This will be ignored.");

            assertNotEquals(regularSystemErrorStream, System.err);
        } finally {
            System.setErr(regularSystemErrorStream);
        }
    }

    @Test
    public void testCopyPreviousDataIfAvailableNull() {
        Article originalArticle = new Article(6, "http://www.nrc.nl/", "nrc", null, "title",
                                              null, "text", 496);

        Article article = new Article(6, "http://www.nrc.nl/", "nrc", null, "title",
                                      null, "text", 496);

        Utilities.copyPreviousDataIfAvailable(article, null);
        assertEquals(originalArticle, article);
    }

    @Test
    public void testCopyPreviousDataIfAvailableObject() {
        Article article = new Article(6, "http://www.nrc.nl/", "nrc", null, "title",
                                      null, "text", 496);

        Article previousArticle = new Article(6, "http://www.nrc.nl/", "nrc", null, "title",
                                      null, "text", 496);

        previousArticle.setStarred(!article.isStarred());
        previousArticle.setRead(!article.isRead());
        previousArticle.setArchived(!article.isArchived());

        Utilities.copyPreviousDataIfAvailable(article, previousArticle);
        assertEquals(previousArticle, article);
    }
}

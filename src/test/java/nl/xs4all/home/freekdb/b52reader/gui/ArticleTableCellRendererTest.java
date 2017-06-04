/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.Color;
import java.awt.Component;
import java.time.Month;
import java.time.ZonedDateTime;

import javax.swing.JTable;

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link ObjectHub} class.
 */
public class ArticleTableCellRendererTest {
    @Test
    public void testGetTableCellRendererComponentEmpty() {
        ArticleTableCellRenderer.setDefaultBackgroundColor(Color.YELLOW);

        String unknownSourceId = "unknown source id";
        Article article = new Article.Builder("url", unknownSourceId, null, "title", null,
                                              "text")
                .likes(1)
                .build();

        Component rendererComponent =
                new ArticleTableCellRenderer().getTableCellRendererComponent(new JTable(), article, false,
                                                                             false, 0, 0);

        assertEquals(Color.YELLOW, rendererComponent.getBackground());
    }

    @Test
    public void testGetTableCellRendererComponentFilledIn() {
        Author author = new Author("Test Author", 1);
        ZonedDateTime date = Utilities.createDate(2002, Month.FEBRUARY, 20);

        Article article = new Article.Builder("url", "test", author, "title", date, "text")
                .likes(1)
                .build();

        article.setStarred(true);
        article.setRead(true);

        Component rendererComponent =
                new ArticleTableCellRenderer().getTableCellRendererComponent(new JTable(), article, true,
                                                                             false, 0, 0);

        assertEquals(Constants.NICE_LIGHT_BLUE, rendererComponent.getBackground());
    }
}

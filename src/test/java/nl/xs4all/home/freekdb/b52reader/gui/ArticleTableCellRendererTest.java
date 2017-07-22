/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.awt.Color;
import java.awt.Component;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTable;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link ArticleTableCellRenderer} class.
 */
public class ArticleTableCellRendererTest {
    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = Mockito.mock(Configuration.class);
    }

    @Test
    public void testGetTableCellRendererComponentEmpty() {
        ArticleTableCellRenderer.setDefaultBackgroundColor(Color.YELLOW);

        String unknownSourceId = "unknown source id";
        Article article = Article.builder().url("url").sourceId(unknownSourceId).title("title").text("text").likes(1)
            .build();

        Component rendererComponent =
            new ArticleTableCellRenderer(configuration).getTableCellRendererComponent(new JTable(), article,
                                                                                      false, false,
                                                                                      0, 0);

        assertEquals(Color.YELLOW, rendererComponent.getBackground());
    }

    @Test
    public void testGetTableCellRendererComponentFilledIn() {
        Mockito.when(configuration.getNiceLightBlue()).thenReturn(Color.BLUE);
        Mockito.when(configuration.getDateTimeFormatLonger()).thenReturn(DateTimeFormatter.ofPattern("EEE dd-MMM HH:mm"));

        Author author = new Author("Test Author", 1);
        ZonedDateTime date = Utilities.createDate(2002, Month.FEBRUARY, 20);

        Article article = Article.builder().url("url").sourceId("test").author(author).title("title").dateTime(date)
            .text("text").likes(1)
            .build();

        article.setStarred(true);
        article.setRead(true);

        Component rendererComponent =
            new ArticleTableCellRenderer(configuration).getTableCellRendererComponent(new JTable(), article,
                                                                                      true, false,
                                                                                      0, 0);

        assertEquals(configuration.getNiceLightBlue(), rendererComponent.getBackground());
    }
}

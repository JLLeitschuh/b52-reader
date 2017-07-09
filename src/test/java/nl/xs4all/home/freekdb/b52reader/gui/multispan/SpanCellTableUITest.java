/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTextField;

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.datamodel.Author;
import nl.xs4all.home.freekdb.b52reader.datamodel.database.PersistencyHandler;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link SpanCellTableUI} class.
 */
public class SpanCellTableUITest {
    @Test
    public void testPaint() throws IOException {
        Graphics mockGraphics1 = getMockGraphics();
        Graphics mockGraphics2 = getMockGraphics();
        Graphics mockGraphics3 = getMockGraphics();
        Graphics mockGraphics4 = getMockGraphics();

        Mockito.when(mockGraphics1.getClipBounds()).thenReturn(new Rectangle(10, 10, 200, 200));
        Mockito.when(mockGraphics1.create(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(mockGraphics2);
        Mockito.when(mockGraphics2.create()).thenReturn(mockGraphics3);
        Mockito.when(mockGraphics3.create()).thenReturn(mockGraphics4);

        SpanCellTableUI spanCellTableUI = new SpanCellTableUI();
        SpanCellTable table = new SpanCellTable(createTableModel());
        SpanCellTable spyTable = Mockito.spy(table);

        Mockito.when(spyTable.getEditorComponent()).thenReturn(new JTextField());
        Mockito.when(spyTable.isEditing()).thenReturn(true);
        Mockito.when(spyTable.getEditingRow()).thenReturn(0);
        Mockito.when(spyTable.getEditingColumn()).thenReturn(0);

        spanCellTableUI.installUI(spyTable);

        spanCellTableUI.paint(mockGraphics1, null);

        // We expect five "fetched" strings (one cell is edited) and four "read" strings to be drawn (two of the six
        // articles are read).
        assertEquals(5, getDrawStringCount(mockGraphics4, "fetched"));
        assertEquals(4, getDrawStringCount(mockGraphics4, "read"));
    }

    private long getDrawStringCount(Graphics mockGraphics4, String text) {
        return Mockito.mockingDetails(mockGraphics4).getInvocations().stream()
                .filter(invocation -> invocation.toString().startsWith("graphics.drawString") &&
                                      invocation.toString().contains(text))
                .count();
    }

    private Graphics getMockGraphics() {
        Graphics mockGraphics = Mockito.mock(Graphics.class);

        Mockito.when(mockGraphics.getFont()).thenReturn(new Font("Default", Font.PLAIN, 12));

        return mockGraphics;
    }

    private SpanCellTableModel createTableModel() throws IOException {
        List<Article> articles = getSixTestArticles();

        byte[] configurationLinesBytes = "".getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes),
                                                        Mockito.mock(PersistencyHandler.class));

        List<String> columnNames = Arrays.asList("fetched", "starred", "read", "title", "author", "date/time");

        List<Class<?>> columnClasses = Arrays.asList(
                String.class, Icon.class, String.class, String.class, Author.class, String.class
        );

        SpanCellTableModel tableModel = new SpanCellTableModel(articles, 6, configuration);
        tableModel.setColumnsAndData(columnNames, columnClasses, articles, article -> true);

        return tableModel;
    }

    private List<Article> getSixTestArticles() {
        return Arrays.asList(
                new Article.Builder("u1", "s1", null, "Title1", null, "Text 1.")
                        .starred(true).read(true)
                        .build(),
                new Article.Builder("u2", "s2", null, "Title2", null, "Text 2.")
                        .build(),
                new Article.Builder("u3", "s3", null, "Title3", null, "Text 3.")
                        .starred(true).read(true).archived(true)
                        .build(),
                new Article.Builder("u4", "s4", null, "Title4", null, "Text 4.")
                        .build(),
                new Article.Builder("u5", "s5", null, "Title5", null, "Text 5.")
                        .build(),
                new Article.Builder("u6", "s6", null, "Title6", null, "Text 6.")
                        .build()
        );
    }
}

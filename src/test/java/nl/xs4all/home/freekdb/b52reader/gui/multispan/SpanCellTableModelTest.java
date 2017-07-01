/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the {@link SpanCellTableModel} class.
 */
public class SpanCellTableModelTest {
    private static final int COLUMN_COUNT = 6;

    private List<Article> articles;
    private SpanCellTableModel tableModel;

    @Before
    public void setUp() throws IOException {
        articles = getSixTestArticles();

        byte[] configurationLinesBytes = "".getBytes("UTF-8");

        Configuration configuration = new Configuration(new ByteArrayInputStream(configurationLinesBytes),
                                                        Mockito.mock(PersistencyHandler.class));

        tableModel = new SpanCellTableModel(articles, COLUMN_COUNT, configuration);
    }

    @Test
    public void testAddColumnRegular() {
        String addedColumnName = "added column";
        String value1 = "value 1";
        String value2 = "value 2";

        tableModel.addColumn(addedColumnName, new Vector<>(Arrays.asList(value1, value2)));

        int addedColumnIndex = tableModel.getColumnCount() - 1;
        assertEquals(COLUMN_COUNT + 1, tableModel.getColumnCount());
        assertEquals(addedColumnName, tableModel.getColumnName(addedColumnIndex));
        assertEquals(value1, tableModel.getValueAt(0, addedColumnIndex));
        assertEquals(value2, tableModel.getValueAt(1, addedColumnIndex));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddColumnException() throws IOException {
        tableModel.addColumn(null);
    }

    @Test
    public void testAddRowWithData() throws IOException {
        String value1 = "value 1";
        String value2 = "value 2";
        Vector rowData = new Vector<>(Arrays.asList(value1, value2));
        tableModel.addRow(rowData);

        assertEquals(2 * (articles.size() + 1), tableModel.getRowCount());
        assertEquals(value1, tableModel.getValueAt(2 * articles.size(), 0));
        assertEquals(value2, tableModel.getValueAt(2 * articles.size(), 1));
    }

    @Test
    public void testAddRowWithNull() throws IOException {
        tableModel.addRow((Vector) null);

        assertEquals(2 * (articles.size() + 1), tableModel.getRowCount());
    }

    @Test
    public void testInsertRowWithData() throws IOException {
        String value1 = "value 1";
        String value2 = "value 2";
        Vector rowData = new Vector<>(Arrays.asList(value1, value2));
        tableModel.insertRow(0, rowData);

        assertEquals(2 * (articles.size() + 1), tableModel.getRowCount());
        assertEquals(value1, tableModel.getValueAt(0, 0));
        assertEquals(value2, tableModel.getValueAt(0, 1));
    }

    @Test
    public void testInsertRowWithNull() throws IOException {
        tableModel.insertRow(0, (Vector) null);

        assertEquals(2 * (articles.size() + 1), tableModel.getRowCount());
    }

    @Test
    public void testGetArticleRegular() throws IOException {
        assertEquals(articles.get(0), tableModel.getArticle(0));

        assertNull(tableModel.getArticle(-1));
        assertNull(tableModel.getArticle(articles.size()));
    }

    @Test
    public void testGetArticleWithNull() throws IOException {
        tableModel.setColumnsAndData(null, null, null, null);

        assertNull(tableModel.getArticle(0));
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

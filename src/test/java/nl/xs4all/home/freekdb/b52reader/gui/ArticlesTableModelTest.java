/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.ArrayList;
import java.util.Collections;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link ArticlesTableModel} class.
 */
public class ArticlesTableModelTest {
    private boolean listenerIsNotified;

    @Test
    public void testEmpty() {
        ArticlesTableModel tableModel = new ArticlesTableModel(new ArrayList<>());

        assertEquals(0, tableModel.getRowCount());
        assertEquals(Article.class, tableModel.getColumnClass(1));
        assertEquals(1, tableModel.getColumnCount());
        assertNull(tableModel.getColumnName(0));
        assertFalse(tableModel.isCellEditable(0, 0));

        assertNull(tableModel.getValueAt(-6, 0));
        assertNull(tableModel.getValueAt(28, 0));
    }

    @Test
    public void testOneRow() {
        ArticlesTableModel tableModel = new ArticlesTableModel(Collections.singletonList(createArticle("url1")));

        tableModel.addTableModelListener(tableModelEvent -> listenerIsNotified = true);

        Article article2 = createArticle("url2");
        tableModel.setArticles(Collections.singletonList(article2));

        assertTrue(listenerIsNotified);

        tableModel.setValueAt("Model is read only: this should be ignored.", 0, 0);

        assertEquals(article2, tableModel.getValueAt(0, 0));
        assertNull(tableModel.getValueAt(0, 6));
    }

    private Article createArticle(String url) {
        return Article.builder().url(url).sourceId("source-id").title("title")
                .build();
    }
}

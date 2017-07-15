/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;

/**
 * Table model optimized for displaying articles. This class is used by the GUI table.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class ArticlesTableModel extends AbstractTableModel {
    /**
     * Current articles that will be shown in the GUI table.
     */
    private transient List<Article> articles;

    /**
     * Construct a table model for showing articles.
     *
     * @param articles articles to show in the GUI table.
     */
    public ArticlesTableModel(final List<Article> articles) {
        this.articles = articles;
    }

    /**
     * Change the articles to show in the GUI table.
     *
     * @param articles articles to show in the GUI table.
     */
    public void setArticles(final List<Article> articles) {
        this.articles = articles;

        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return articles.size();
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return Article.class;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return null;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        // The columnIndex parameter should always be zero.
        return rowIndex >= 0 && rowIndex < articles.size() && columnIndex == 0 ? articles.get(rowIndex) : null;
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        // This should not happen, since the model is read-only.
    }
}

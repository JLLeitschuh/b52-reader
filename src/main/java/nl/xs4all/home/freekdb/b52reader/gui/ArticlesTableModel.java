/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;

public class ArticlesTableModel extends AbstractTableModel {
    private transient List<Article> articles;

    public ArticlesTableModel(List<Article> articles) {
        this.articles = articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;

        fireTableStructureChanged();
    }

    public int getRowCount() {
        return articles.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Article.class;
    }

    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        // The columnIndex parameter should always be zero.
        return rowIndex >= 0 && rowIndex < articles.size() && columnIndex == 0 ? articles.get(rowIndex) : null;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // This should not happen, since the model is read-only.
    }
}

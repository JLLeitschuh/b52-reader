/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import nl.xs4all.home.freekdb.b52reader.model.Article;

public class ArticlesTableModel extends AbstractTableModel {
    private List<Article> articles;

    ArticlesTableModel(List<Article> articles) {
        this.articles = articles;
    }

    void setArticles(List<Article> articles) {
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

    public String getColumnName(int columnIndex) {
        return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        // The columnIndex parameter should always be zero.
        return rowIndex >= 0 && rowIndex < articles.size() && columnIndex == 0 ? articles.get(rowIndex) : null;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // This should not happen, since the model is read-only.
    }
}

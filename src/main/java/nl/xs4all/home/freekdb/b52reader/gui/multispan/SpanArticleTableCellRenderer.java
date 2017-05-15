/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.model.Article;

public class SpanArticleTableCellRenderer extends DefaultTableCellRenderer {
    private static Color defaultBackgroundColor;

    public static void setDefaultBackgroundColor(Color defaultBackgroundColor) {
        SpanArticleTableCellRenderer.defaultBackgroundColor = defaultBackgroundColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Article article = ((SpanCellTableModel) table.getModel()).getArticle(row / 2);
        rendererComponent.setBackground(isSelected ? Constants.NICE_LIGHT_BLUE : getBackgroundColor(article));

        return rendererComponent;
    }

    private Color getBackgroundColor(Article article) {
        return "nrc".equals(article.getSourceId()) ? new Color(178, 34, 34)
                : "test".equals(article.getSourceId()) ? Color.ORANGE
                : defaultBackgroundColor;
    }
}

/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import com.google.common.collect.ImmutableMap;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import nl.xs4all.home.freekdb.b52reader.general.Constants;
import nl.xs4all.home.freekdb.b52reader.model.Article;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class SpanArticleTableCellRenderer extends DefaultTableCellRenderer {
    private static final Map<String, Color> COLOR_MAP = ImmutableMap.of(
            "nrc", new Color(144, 238, 144),
            "test", Color.ORANGE
    );

    private static Color defaultBackgroundColor;

    public static void setDefaultBackgroundColor(Color defaultBackgroundColor) {
        SpanArticleTableCellRenderer.defaultBackgroundColor = defaultBackgroundColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                                                          column);

        Article article = ((SpanCellTableModel) table.getModel()).getArticle(row / 2);

        rendererComponent.setBackground(isSelected
                                                ? Constants.NICE_LIGHT_BLUE
                                                : getBackgroundColor(article.getSourceId()));

        return rendererComponent;
    }

    private Color getBackgroundColor(String sourceId) {
        return COLOR_MAP.getOrDefault(sourceId, defaultBackgroundColor);
    }
}

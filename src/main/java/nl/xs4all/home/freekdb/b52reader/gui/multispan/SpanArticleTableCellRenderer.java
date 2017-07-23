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

import nl.xs4all.home.freekdb.b52reader.datamodel.Article;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;
import nl.xs4all.home.freekdb.b52reader.general.Constants;

/**
 * Custom renderer for the <code>{@link SpanCellTable}</code> GUI table.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class SpanArticleTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * Map with background colors for specific source ids.
     */
    private static final Map<String, Color> COLOR_MAP = ImmutableMap.of(
        Constants.NRC_SOURCE_ID, new Color(144, 238, 144),
        Constants.TEST_SOURCE_ID, Color.ORANGE
    );

    /**
     * Default background color for this renderer.
     */
    private static Color defaultBackgroundColor;

    /**
     * Configuration object.
     */
    private final transient Configuration configuration;

    /**
     * Construct a cell renderer for a <code>{@link SpanCellTable}</code>.
     *
     * @param configuration the configuration object.
     */
    public SpanArticleTableCellRenderer(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Set the default background color for GUI table cells that do not have a background color specified for their
     * source id.
     *
     * @param defaultBackgroundColor the default background color.
     */
    public static void setDefaultBackgroundColor(final Color defaultBackgroundColor) {
        SpanArticleTableCellRenderer.defaultBackgroundColor = defaultBackgroundColor;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                                                                column);

        final Article article = ((SpanCellTableModel) table.getModel()).getArticle(row / 2);

        rendererComponent.setBackground(isSelected
                                            ? configuration.getNiceLightBlue()
                                            : getBackgroundColor(article.getSourceId()));

        return rendererComponent;
    }

    /**
     * Get the background color that is configured for the specified source id.
     *
     * @param sourceId the source id to get the background color for.
     * @return the background color that is configured for the specified source id.
     */
    private Color getBackgroundColor(final String sourceId) {
        return COLOR_MAP.getOrDefault(sourceId, defaultBackgroundColor);
    }
}

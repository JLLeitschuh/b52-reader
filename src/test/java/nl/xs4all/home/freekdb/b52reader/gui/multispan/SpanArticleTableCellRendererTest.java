/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

import nl.xs4all.home.freekdb.b52reader.TestUtilities;
import nl.xs4all.home.freekdb.b52reader.general.Configuration;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link SpanArticleTableCellRenderer} class.
 */
public class SpanArticleTableCellRendererTest {
    private enum RendererComponentResult { UNSELECTED, SELECTED, EXCEPTION }

    @Test
    public void testGetTableCellRendererComponent() {
        for (RendererComponentResult result : RendererComponentResult.values()) {
            doTestGetTableCellRendererComponent(result);
        }
    }

    private void doTestGetTableCellRendererComponent(RendererComponentResult result) {
        final Configuration mockConfiguration = Mockito.mock(Configuration.class);

        Mockito.when(mockConfiguration.getNiceLightBlue()).thenReturn(Color.BLUE);

        final SpanArticleTableCellRenderer renderer = new SpanArticleTableCellRenderer(mockConfiguration);

        try {
            final SpanCellTableModel tableModel = new SpanCellTableModel(TestUtilities.getOneTestArticle(),
                                                                         2, mockConfiguration);

            final boolean validTable = !result.equals(RendererComponentResult.EXCEPTION);
            final JTable table = validTable ? new SpanCellTable(tableModel) : new JTable();

            final boolean selected = result.equals(RendererComponentResult.SELECTED);

            final Component rendererComponent = renderer.getTableCellRendererComponent(table, "6",
                                                                                       selected, false,
                                                                                       0, 0);

            final Color expectedColor = result.equals(RendererComponentResult.UNSELECTED) ? Color.ORANGE : Color.BLUE;
            assertEquals(result.name(), expectedColor, rendererComponent.getBackground());
        } catch (final Exception e) {
            assertEquals(result.name(), RendererComponentResult.EXCEPTION, result);
            assertEquals(result.name(), ClassCastException.class, e.getClass());
        }
    }
}

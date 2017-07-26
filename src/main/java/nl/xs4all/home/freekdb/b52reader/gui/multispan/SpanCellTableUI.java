/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: unknown.
 *
 * This class is based on the MultiSpanCellTableExample class and related classes from Nobuo Tamemasa.
 * Original Java package: jp.gr.java_conf.tame.swing.table
 *
 * In 2008 Andrew Thompson wrote (https://groups.google.com/forum/#!topic/comp.lang.java.gui/WicMvhk1DdM):
 *     "I spent a lot of time researching the possible licensing of the tame Swing codes,
 *      but Nobuo Tamemasa (the author) has proved to be untraceable, and never specified a
 *      license at the time of posting the codes to public forums.  :-("
 *
 * It is also referred to here: http://stackoverflow.com/a/21977825/1694043
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;

/**
 * @version 1.0 11/26/98
 */
public class SpanCellTableUI extends BasicTableUI {
    @Override
    public void paint(final Graphics graphics, final JComponent component) {
        final Rectangle oldClipBounds = graphics.getClipBounds();

        final Rectangle clipBounds = new Rectangle(oldClipBounds);
        final int tableWidth = table.getColumnModel().getTotalColumnWidth();
        clipBounds.width = Math.min(clipBounds.width, tableWidth);

        graphics.setClip(clipBounds);

        final int firstRowIndex = table.rowAtPoint(new Point(0, clipBounds.y));
        final int lastRowIndex = table.getRowCount() - 1;

        final Rectangle rowRect = new Rectangle(0, 0, tableWidth, table.getRowHeight() + table.getRowMargin());
        rowRect.y = firstRowIndex * rowRect.height;

        for (int rowIndex = firstRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
            if (rowRect.intersects(clipBounds)) {
                paintTableRow(graphics, rowIndex);
            }

            rowRect.y += rowRect.height;
        }

        graphics.setClip(oldClipBounds);
    }

    private void paintTableRow(final Graphics graphics, final int rowIndex) {
        final Rectangle clipBounds = graphics.getClipBounds();
        boolean drawn = false;

        final SpanCellTableModel tableModel = (SpanCellTableModel) table.getModel();
        final TableSpans tableSpans = tableModel.getTableSpans();
        final int columnCount = table.getColumnCount();

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final Rectangle cellRect = table.getCellRect(rowIndex, columnIndex, true);

            final boolean visible = tableSpans.isVisible(rowIndex, columnIndex);
            final SpanCounts spanCounts = tableSpans.getSpan(rowIndex, columnIndex);
            final int cellRow = rowIndex + (visible ? 0 : spanCounts.getRowSpanNumber());
            final int cellColumn = columnIndex + (visible ? 0 : spanCounts.getColumnSpanNumber());

            if (cellRect.intersects(clipBounds)) {
                drawn = true;
                paintTableCell(graphics, cellRect, cellRow, cellColumn);
            } else if (drawn) {
                break;
            }
        }
    }

    private void paintTableCell(final Graphics graphics, final Rectangle cellRect, final int rowIndex,
                                final int columnIndex) {
        final int spacingHeight = table.getRowMargin();
        final int spacingWidth = table.getColumnModel().getColumnMargin();

        final Color contentColor = graphics.getColor();
        graphics.setColor(table.getGridColor());
        graphics.drawRect(cellRect.x, cellRect.y, cellRect.width - 1, cellRect.height - 1);
        graphics.setColor(contentColor);

        cellRect.setBounds(cellRect.x + spacingWidth / 2, cellRect.y + spacingHeight / 2,
                           cellRect.width - spacingWidth, cellRect.height - spacingHeight);

        if (table.isEditing() && table.getEditingRow() == rowIndex
            && table.getEditingColumn() == columnIndex) {
            final Component component = table.getEditorComponent();
            component.setBounds(cellRect);
            component.validate();
        } else {
            final TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
            final Component component = table.prepareRenderer(renderer, rowIndex, columnIndex);

            if (component.getParent() == null) {
                rendererPane.add(component);
            }

            rendererPane.paintComponent(graphics, component, table, cellRect.x, cellRect.y,
                                        cellRect.width, cellRect.height, true);
        }
    }
}

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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * @version 1.0 11/26/98
 */
public class SpanCellTable extends JTable {
    private SpanCellTableModel tableModel;

    public SpanCellTable(TableModel tableModel) {
        super(tableModel);

        this.tableModel = (SpanCellTableModel) tableModel;

        setUI(new SpanCellTableUI());

        // todo: Is the call to setReorderingAllowed necessary?
        getTableHeader().setReorderingAllowed(true);
        setAutoCreateRowSorter(true);

        //setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }

    public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
        if ((row < 0) || (column < 0) || (getRowCount() <= row) || (getColumnCount() <= column)) {
            return super.getCellRect(row, column, includeSpacing);
        }
        else {
            Rectangle cellRect = new Rectangle();
            TableSpans tableSpans = tableModel.getTableSpans();
            int[] spanCounts = tableSpans.getSpan(row, column);

            // Adjust row and column for spanned cells.
            if (!tableSpans.isVisible(row, column)) {
                row += spanCounts[TableSpans.ROW];
                column += spanCounts[TableSpans.COLUMN];
            }

            int columnMargin = getColumnModel().getColumnMargin();
            int cellHeight = rowHeight + rowMargin;
            cellRect.y = row * cellHeight;
            cellRect.height = spanCounts[TableSpans.ROW] * cellHeight;

            Enumeration columnEnumeration = getColumnModel().getColumns();
            int columnIndex = 0;

            // First determine cellRect.x and the start value for cellRect.width.
            while (columnEnumeration.hasMoreElements()) {
                TableColumn tableColumn = (TableColumn) columnEnumeration.nextElement();
                cellRect.width = tableColumn.getWidth() + columnMargin;

                if (columnIndex == column)
                    break;

                cellRect.x += cellRect.width;
                columnIndex++;
            }

            // Now determine the total cellRect.width by including all spanned columns.
            for (int spanColumnIndex = 0; spanColumnIndex < spanCounts[TableSpans.COLUMN] - 1; spanColumnIndex++) {
                TableColumn tableColumn = (TableColumn) columnEnumeration.nextElement();
                cellRect.width += tableColumn.getWidth() + columnMargin;
            }

            if (!includeSpacing) {
                // Exclude spacing margins.
                Dimension spacing = getIntercellSpacing();

                cellRect.setBounds(cellRect.x + spacing.width / 2, cellRect.y + spacing.height / 2,
                                   cellRect.width - spacing.width, cellRect.height - spacing.height);
            }

            return cellRect;
        }
    }

    public int rowAtPoint(Point point) {
        return rowColumnAtPoint(point)[TableSpans.ROW];
    }

    public int columnAtPoint(Point point) {
        return rowColumnAtPoint(point)[TableSpans.COLUMN];
    }

    private int[] rowColumnAtPoint(Point point) {
        int[] rowColumn = {-1, -1};

        int row = point.y / (rowHeight + rowMargin);

        if ((row < 0) || (getRowCount() <= row)) {
            return rowColumn;
        }
        else {
            int column = getColumnModel().getColumnIndexAtX(point.x);
            TableSpans tableSpans = tableModel.getTableSpans();
            boolean visible = tableSpans.isVisible(row, column);
            int[] spanCounts = tableSpans.getSpan(row, column);

            rowColumn[TableSpans.COLUMN] = column + (visible ? 0 : spanCounts[TableSpans.COLUMN]);
            rowColumn[TableSpans.ROW] = row + (visible ? 0 : spanCounts[TableSpans.ROW]);

            return rowColumn;
        }
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
        repaint();
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int firstIndex = listSelectionEvent.getFirstIndex();
        int lastIndex = listSelectionEvent.getLastIndex();

        if (firstIndex == -1 && lastIndex == -1) {
            // Selection cleared.
            repaint();

            // todo: Can we end the method execution (by calling return) here, since the entire component is repainted?
        }

        Rectangle dirtyRegion = getCellRect(firstIndex, 0, false);
        int numColumns = getColumnCount();
        int index = firstIndex;

        for (int i = 0; i < numColumns; i++) {
            dirtyRegion.add(getCellRect(index, i, false));
        }

        index = lastIndex;

        for (int i = 0; i < numColumns; i++) {
            dirtyRegion.add(getCellRect(index, i, false));
        }

        repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
    }
}

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

import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * GUI table that supports combining multiple cells into a large rectangle.
 *
 * @author <a href="mailto:unknown@unknown.org">Nobuo Tamemasa</a>
 * @version 1.0 11/26/98
 */
public class SpanCellTable extends JTable {
    /**
     * Specialized table model for this GUI table.
     */
    private SpanCellTableModel tableModel;

    /**
     * Construct a span cell GUI table.
     *
     * @param tableModel specialized table model for this GUI table.
     */
    public SpanCellTable(final TableModel tableModel) {
        super(tableModel);

        this.tableModel = (SpanCellTableModel) tableModel;

        setUI(new SpanCellTableUI());

        getTableHeader().setReorderingAllowed(true);
        setAutoCreateRowSorter(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }

    @Override
    @Nonnull
    public Rectangle getCellRect(final int row, final int column, final boolean includeSpacing) {
        if ((row < 0) || (column < 0) || (row >= getRowCount()) || (column >= getColumnCount())) {
            return super.getCellRect(row, column, includeSpacing);
        } else {
            final Rectangle cellRect = new Rectangle();
            final TableSpans tableSpans = tableModel.getTableSpans();
            final SpanCounts spanCounts = tableSpans.getSpan(row, column);

            // Adjust row and column for spanned cells.
            int adjustedRowIndex = row;
            int adjustedColumnIndex = column;
            if (!tableSpans.isVisible(adjustedRowIndex, adjustedColumnIndex)) {
                adjustedRowIndex += spanCounts.getRowSpanNumber();
                adjustedColumnIndex += spanCounts.getColumnSpanNumber();
            }

            final int columnMargin = getColumnModel().getColumnMargin();
            final int cellHeight = rowHeight + rowMargin;
            cellRect.y = adjustedRowIndex * cellHeight;
            cellRect.height = spanCounts.getRowSpanNumber() * cellHeight;

            determineCellRectWidthAndX(cellRect, columnMargin, adjustedColumnIndex, spanCounts, includeSpacing);

            return cellRect;
        }
    }

    /**
     * Determine the width and x coordinate for the specific cell rect.
     *
     * @param cellRect            cell rect being constructed.
     * @param columnMargin        width between cells.
     * @param adjustedColumnIndex column index adjusted for spanned cells.
     * @param spanCounts          span counts for the rectangle that dictates the width.
     * @param includeSpacing      whether to include the intercell spacing.
     */
    private void determineCellRectWidthAndX(final Rectangle cellRect, final int columnMargin,
                                            final int adjustedColumnIndex, final SpanCounts spanCounts,
                                            final boolean includeSpacing) {
        final Enumeration columnEnumeration = getColumnModel().getColumns();
        int columnIndex = 0;

        // First determine cellRect.x and the start value for cellRect.width.
        while (columnEnumeration.hasMoreElements()) {
            final TableColumn tableColumn = (TableColumn) columnEnumeration.nextElement();
            cellRect.width = tableColumn.getWidth() + columnMargin;

            if (columnIndex == adjustedColumnIndex) {
                break;
            }

            cellRect.x += cellRect.width;
            columnIndex++;
        }

        // Now determine the total cellRect.width by including all spanned columns.
        for (int spanColumnIndex = 0; spanColumnIndex < spanCounts.getColumnSpanNumber() - 1; spanColumnIndex++) {
            final TableColumn tableColumn = (TableColumn) columnEnumeration.nextElement();
            cellRect.width += tableColumn.getWidth() + columnMargin;
        }

        if (!includeSpacing) {
            // Exclude spacing margins.
            final Dimension spacing = getIntercellSpacing();

            cellRect.setBounds(cellRect.x + spacing.width / 2, cellRect.y + spacing.height / 2,
                               cellRect.width - spacing.width, cellRect.height - spacing.height);
        }
    }

    @Override
    public int rowAtPoint(@Nonnull final Point point) {
        return rowColumnAtPoint(point).getRowSpanNumber();
    }

    @Override
    public int columnAtPoint(@Nonnull final Point point) {
        return rowColumnAtPoint(point).getColumnSpanNumber();
    }

    /**
     * Determine the span counts for a specific point.
     *
     * @param point the location of interest.
     * @return the span counts for point.
     */
    private SpanCounts rowColumnAtPoint(final Point point) {
        final SpanCounts spanCountsPoint = new SpanCounts(-1, -1);

        final int row = point.y / (rowHeight + rowMargin);

        if ((row >= 0) && (row < getRowCount())) {
            final int column = getColumnModel().getColumnIndexAtX(point.x);
            final TableSpans tableSpans = tableModel.getTableSpans();
            final SpanCounts spanCounts = tableSpans.getSpan(row, column);
            final boolean visible = tableSpans.isVisible(row, column);

            spanCountsPoint.setColumnSpanNumber(column + (visible ? 0 : spanCounts.getColumnSpanNumber()));
            spanCountsPoint.setRowSpanNumber(row + (visible ? 0 : spanCounts.getRowSpanNumber()));
        }

        return spanCountsPoint;
    }

    @Override
    public void columnSelectionChanged(final ListSelectionEvent e) {
        repaint();
    }

    @Override
    public void valueChanged(final ListSelectionEvent listSelectionEvent) {
        final int firstIndex = listSelectionEvent.getFirstIndex();
        final int lastIndex = listSelectionEvent.getLastIndex();

        if (firstIndex == -1 && lastIndex == -1) {
            // Selection cleared.
            repaint();
        } else {
            final Rectangle dirtyRegion = getCellRect(firstIndex, 0, false);

            final int columnCount = getColumnCount();

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                dirtyRegion.add(getCellRect(firstIndex, columnIndex, false));
            }

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                dirtyRegion.add(getCellRect(lastIndex, columnIndex, false));
            }

            repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
        }
    }
}

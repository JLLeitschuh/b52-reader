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
import java.util.Arrays;

/**
 * @version 1.0 11/22/98
 */
public class DefaultTableSpans implements TableSpans {
    //
    // !!!! CAUTION !!!!!
    // these values must be synchronized to Table data
    //
    private int rowCount;
    private int columnCount;

    /**
     * For each cell (a row and column position), a span number is stored for the row and column directions. This span
     * number is one for cells that are not combined. For a group of cells that are combined, the top left cell contains
     * span numbers equal to the number of rows and columns in the group, while the other cells have span numbers
     * smaller than or equal to zero.
     */
    private SpanCounts[][] span;

    DefaultTableSpans(final int rowCount, final int columnCount) {
        setSize(new Dimension(columnCount, rowCount));
    }

    public boolean isVisible(final int rowIndex, final int columnIndex) {
        return !isOutOfBounds(rowIndex, columnIndex)
               && span[rowIndex][columnIndex].getRowSpanNumber() >= 1
               && span[rowIndex][columnIndex].getColumnSpanNumber() >= 1;
    }

    public SpanCounts getSpan(final int rowIndex, final int columnIndex) {
        return !isOutOfBounds(rowIndex, columnIndex) ? span[rowIndex][columnIndex] : new SpanCounts();
    }

    public void combine(final int[] rowIndices, final int[] columnIndices) {
        if (!isOutOfBounds(rowIndices, columnIndices)) {
            final int startRowIndex = rowIndices[0];
            final int startColumnIndex = columnIndices[0];
            final int combineRowCount = rowIndices.length;
            final int combineColumnCount = columnIndices.length;

            if (isValidCombinationArea(startRowIndex, startColumnIndex, combineRowCount, combineColumnCount)) {
                int rowSpanNumber = 0;

                for (int rowOffset = 0; rowOffset < combineRowCount; rowOffset++) {
                    final int cellRowIndex = startRowIndex + rowOffset;
                    int columnSpanNumber = 0;

                    for (int columnOffset = 0; columnOffset < combineColumnCount; columnOffset++) {
                        setSpanCellNumbers(cellRowIndex, startColumnIndex + columnOffset,
                                           rowSpanNumber, columnSpanNumber);

                        columnSpanNumber--;
                    }

                    rowSpanNumber--;
                }

                setSpanCellNumbers(startRowIndex, startColumnIndex, combineRowCount, combineColumnCount);
            }
        }
    }

    private boolean isValidCombinationArea(final int startRowIndex, final int startColumnIndex,
                                           final int combinationRowCount, final int combinationColumnCount) {
        for (int rowIndex = 0; rowIndex < combinationRowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < combinationColumnCount; columnIndex++) {
                if (span[startRowIndex + rowIndex][startColumnIndex + columnIndex].getRowSpanNumber() != 1
                    || span[startRowIndex + rowIndex][startColumnIndex + columnIndex].getColumnSpanNumber() != 1) {
                    return false;
                }
            }
        }

        return true;
    }

    private void setSpanCellNumbers(final int cellRowIndex, final int cellColumnIndex, final int rowSpanNumber,
                                    final int columnSpanNumber) {
        span[cellRowIndex][cellColumnIndex].setRowSpanNumber(rowSpanNumber);
        span[cellRowIndex][cellColumnIndex].setColumnSpanNumber(columnSpanNumber);
    }

    public void setSize(final Dimension size) {
        columnCount = size.width;
        rowCount = size.height;

        span = new SpanCounts[rowCount][columnCount];

        for (int rowIndex = 0; rowIndex < span.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < span[rowIndex].length; columnIndex++) {
                span[rowIndex][columnIndex] = new SpanCounts();
            }
        }
    }

    public void addColumn() {
        final SpanCounts[][] oldSpan = span;
        final int currentRowCount = oldSpan.length;
        final int oldColumnCount = oldSpan[0].length;

        span = new SpanCounts[currentRowCount][oldColumnCount + 1];

        for (int rowIndex = 0; rowIndex < currentRowCount; rowIndex++) {
            span[rowIndex] = Arrays.copyOf(oldSpan[rowIndex], oldColumnCount + 1);
            span[rowIndex][oldColumnCount] = new SpanCounts();
        }
    }

    public void addRow() {
        final SpanCounts[][] oldSpan = span;
        final int oldRowCount = oldSpan.length;
        final int currentColumnCount = oldSpan[0].length;

        span = new SpanCounts[oldRowCount + 1][currentColumnCount];

        System.arraycopy(oldSpan, 0, span, 0, oldRowCount);

        for (int columnIndex = 0; columnIndex < currentColumnCount; columnIndex++) {
            span[oldRowCount][columnIndex] = new SpanCounts();
        }
    }

    public void insertRow(final int rowIndex) {
        final SpanCounts[][] oldSpan = span;
        final int oldRowCount = oldSpan.length;
        final int currentColumnCount = oldSpan[0].length;

        span = new SpanCounts[oldRowCount + 1][currentColumnCount];

        if (rowIndex > 0) {
            System.arraycopy(oldSpan, 0, span, 0, rowIndex);
        }

        System.arraycopy(oldSpan, 0, span, rowIndex + 1, oldRowCount - rowIndex);

        for (int columnIndex = 0; columnIndex < currentColumnCount; columnIndex++) {
            span[rowIndex][columnIndex] = new SpanCounts();
        }
    }

    private boolean isOutOfBounds(final int row, final int column) {
        return (row < 0) || (row >= rowCount) || (column < 0) || (column >= columnCount);
    }

    private boolean isOutOfBounds(final int[] rows, final int[] columns) {
        return Arrays.stream(rows).anyMatch(row -> row < 0 || row >= rowCount)
               || Arrays.stream(columns).anyMatch(column -> column < 0 || column >= columnCount);
    }
}

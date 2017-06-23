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
    private int[][][] span;

    DefaultTableSpans(int rowCount, int columnCount) {
        setSize(new Dimension(columnCount, rowCount));
    }

    public boolean isVisible(int rowIndex, int columnIndex) {
        return !isOutOfBounds(rowIndex, columnIndex) &&
               span[rowIndex][columnIndex][TableSpans.ROW] >= 1 &&
               span[rowIndex][columnIndex][TableSpans.COLUMN] >= 1;
    }

    public int[] getSpan(int rowIndex, int columnIndex) {
        return !isOutOfBounds(rowIndex, columnIndex) ? span[rowIndex][columnIndex] : new int[]{1, 1};
    }

    public void combine(int[] rowIndices, int[] columnIndices) {
        if (!isOutOfBounds(rowIndices, columnIndices)) {
            int startRowIndex = rowIndices[0];
            int startColumnIndex = columnIndices[0];
            int combineRowCount = rowIndices.length;
            int combineColumnCount = columnIndices.length;

            if (isValidCombinationArea(startRowIndex, startColumnIndex, combineRowCount, combineColumnCount)) {
                int rowSpanNumber = 0;

                for (int rowOffset = 0; rowOffset < combineRowCount; rowOffset++) {
                    int cellRowIndex = startRowIndex + rowOffset;
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

    private boolean isValidCombinationArea(int startRowIndex, int startColumnIndex,
                                           int combinationRowCount, int combinationColumnCount) {
        for (int rowIndex = 0; rowIndex < combinationRowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < combinationColumnCount; columnIndex++) {
                if (span[startRowIndex + rowIndex][startColumnIndex + columnIndex][TableSpans.ROW] != 1 ||
                    span[startRowIndex + rowIndex][startColumnIndex + columnIndex][TableSpans.COLUMN] != 1) {
                    return false;
                }
            }
        }

        return true;
    }

    private void setSpanCellNumbers(int cellRowIndex, int cellColumnIndex) {
        setSpanCellNumbers(cellRowIndex, cellColumnIndex, 1, 1);
    }

    private void setSpanCellNumbers(int cellRowIndex, int cellColumnIndex, int rowSpanNumber, int columnSpanNumber) {
        span[cellRowIndex][cellColumnIndex][TableSpans.ROW] = rowSpanNumber;
        span[cellRowIndex][cellColumnIndex][TableSpans.COLUMN] = columnSpanNumber;
    }

    public void setSize(Dimension size) {
        columnCount = size.width;
        rowCount = size.height;
        span = new int[rowCount][columnCount][2];   // 2: COLUMN,ROW

        for (int rowIndex = 0; rowIndex < span.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < span[rowIndex].length; columnIndex++) {
                setSpanCellNumbers(rowIndex, columnIndex);
            }
        }
    }

    public void addColumn() {
        int[][][] oldSpan = span;
        int currentRowCount = oldSpan.length;
        int oldColumnCount = oldSpan[0].length;

        span = new int[currentRowCount][oldColumnCount + 1][2];

        for (int rowIndex = 0; rowIndex < currentRowCount; rowIndex++) {
            span[rowIndex] = Arrays.copyOf(oldSpan[rowIndex], oldColumnCount + 1);
            span[rowIndex][oldColumnCount] = new int[2];

            setSpanCellNumbers(rowIndex, oldColumnCount);
        }
    }

    public void addRow() {
        int[][][] oldSpan = span;
        int oldRowCount = oldSpan.length;
        int currentColumnCount = oldSpan[0].length;

        span = new int[oldRowCount + 1][currentColumnCount][2];

        System.arraycopy(oldSpan, 0, span, 0, oldRowCount);

        for (int columnIndex = 0; columnIndex < currentColumnCount; columnIndex++) {
            setSpanCellNumbers(oldRowCount, columnIndex);
        }
    }

    public void insertRow(int rowIndex) {
        int[][][] oldSpan = span;
        int oldRowCount = oldSpan.length;
        int currentColumnCount = oldSpan[0].length;

        span = new int[oldRowCount + 1][currentColumnCount][2];

        if (rowIndex > 0) {
            System.arraycopy(oldSpan, 0, span, 0, rowIndex);
        }

        System.arraycopy(oldSpan, 0, span, rowIndex, oldRowCount - rowIndex);

        for (int columnIndex = 0; columnIndex < currentColumnCount; columnIndex++) {
            setSpanCellNumbers(rowIndex, columnIndex);
        }
    }

    private boolean isOutOfBounds(int row, int column) {
        return (row < 0) || (row >= rowCount) || (column < 0) || (column >= columnCount);
    }

    private boolean isOutOfBounds(int[] rows, int[] columns) {
        return Arrays.stream(rows).anyMatch(row -> row < 0 || row >= rowCount) ||
               Arrays.stream(columns).anyMatch(column -> column < 0 || column >= columnCount);
    }
}

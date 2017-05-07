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
    private int[][][] span;                   // TableSpans
//    protected Color[][] foreground;             // ColoredCell
//    protected Color[][] background;             //

    DefaultTableSpans(int rowCount, int columnCount) {
        setSize(new Dimension(columnCount, rowCount));
    }

    public boolean isVisible(int rowIndex, int columnIndex) {
        return !isOutOfBounds(rowIndex, columnIndex) &&
               (span[rowIndex][columnIndex][TableSpans.COLUMN] >= 1) &&
               (span[rowIndex][columnIndex][TableSpans.ROW] >= 1);
    }

    public int[] getSpan(int rowIndex, int columnIndex) {
        if (!isOutOfBounds(rowIndex, columnIndex)) {
            return span[rowIndex][columnIndex];
        } else {
            return new int[]{1, 1};
        }
    }

    public void combine(int[] rowIndices, int[] columnIndices) {
        if (!isOutOfBounds(rowIndices, columnIndices)) {
            int rowSpan = rowIndices.length;
            int columnSpan = columnIndices.length;
            int startRow = rowIndices[0];
            int startColumn = columnIndices[0];

            for (int i = 0; i < rowSpan; i++) {
                for (int j = 0; j < columnSpan; j++) {
                    if ((span[startRow + i][startColumn + j][TableSpans.COLUMN] != 1)
                        || (span[startRow + i][startColumn + j][TableSpans.ROW] != 1)) {
                        return;
                    }
                }
            }

            for (int i = 0, ii = 0; i < rowSpan; i++, ii--) {
                for (int j = 0, jj = 0; j < columnSpan; j++, jj--) {
                    span[startRow + i][startColumn + j][TableSpans.COLUMN] = jj;
                    span[startRow + i][startColumn + j][TableSpans.ROW] = ii;
                }
            }

            span[startRow][startColumn][TableSpans.COLUMN] = columnSpan;
            span[startRow][startColumn][TableSpans.ROW] = rowSpan;
        }
    }

//    public void split(int rowIndex, int columnIndex) {
//        if (!isOutOfBounds(rowIndex, columnIndex)) {
//            int spanRowCount = span[rowIndex][columnIndex][TableSpans.ROW];
//            int spanColumnCount = span[rowIndex][columnIndex][TableSpans.COLUMN];
//
//            for (int spanRowIndex = 0; spanRowIndex < spanRowCount; spanRowIndex++) {
//                for (int spanColumnIndex = 0; spanColumnIndex < spanColumnCount; spanColumnIndex++) {
//                    span[rowIndex + spanRowIndex][columnIndex + spanColumnIndex][TableSpans.COLUMN] = 1;
//                    span[rowIndex + spanRowIndex][columnIndex + spanColumnIndex][TableSpans.ROW] = 1;
//                }
//            }
//        }
//    }

    public void setSize(Dimension size) {
        columnCount = size.width;
        rowCount = size.height;
        span = new int[rowCount][columnCount][2];   // 2: COLUMN,ROW

//        foreground = new Color[rowCount][columnCount];
//        background = new Color[rowCount][columnCount];

        for (int rowIndex = 0; rowIndex < span.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < span[rowIndex].length; columnIndex++) {
                span[rowIndex][columnIndex][TableSpans.COLUMN] = 1;
                span[rowIndex][columnIndex][TableSpans.ROW] = 1;
            }
        }
    }

    public void addColumn() {
        int[][][] oldSpan = span;
        int rowCount = oldSpan.length;
        int columnCount = oldSpan[0].length;

        span = new int[rowCount][columnCount + 1][2];

        System.arraycopy(oldSpan, 0, span, 0, rowCount);

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            span[rowIndex][columnCount][TableSpans.COLUMN] = 1;
            span[rowIndex][columnCount][TableSpans.ROW] = 1;
        }
    }

    public void addRow() {
        int[][][] oldSpan = span;
        int rowCount = oldSpan.length;
        int columnCount = oldSpan[0].length;

        span = new int[rowCount + 1][columnCount][2];

        System.arraycopy(oldSpan, 0, span, 0, rowCount);

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            span[rowCount][columnIndex][TableSpans.COLUMN] = 1;
            span[rowCount][columnIndex][TableSpans.ROW] = 1;
        }
    }

    public void insertRow(int rowIndex) {
        int[][][] oldSpan = span;
        int rowCount = oldSpan.length;
        int columnCount = oldSpan[0].length;

        span = new int[rowCount + 1][columnCount][2];

        if (rowIndex > 0) {
            System.arraycopy(oldSpan, 0, span, 0, rowIndex - 1);
        }

        System.arraycopy(oldSpan, 0, span, rowIndex, rowCount - rowIndex);

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            span[rowIndex][columnIndex][TableSpans.COLUMN] = 1;
            span[rowIndex][columnIndex][TableSpans.ROW] = 1;
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

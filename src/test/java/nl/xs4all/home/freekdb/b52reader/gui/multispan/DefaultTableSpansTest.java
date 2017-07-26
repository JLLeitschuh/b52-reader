/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link DefaultTableSpans} class.
 */
public class DefaultTableSpansTest {
    @Test
    public void testCombineInvalid() {
        int rowCount = 28;
        int columnCount = 6;

        DefaultTableSpans tableSpans = new DefaultTableSpans(rowCount, columnCount);

        tableSpans.combine(new int[]{0, 1}, new int[]{0, 1});
        tableSpans.combine(new int[]{0, 1, 2, 3}, new int[]{0, 1, 2, 3});

        assertEquals(new SpanCounts(2, 2), tableSpans.getSpan(0, 0));
        assertEquals(new SpanCounts(1, 1), tableSpans.getSpan(2, 2));
    }

    @Test
    public void testAddColumn() {
        int rowCount = 28;
        int columnCount = 6;

        DefaultTableSpans tableSpans = new DefaultTableSpans(rowCount, columnCount);

        tableSpans.addColumn();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            assertEquals(new SpanCounts(1, 1), tableSpans.getSpan(rowIndex, columnCount));
        }
    }

    @Test
    public void testAddRow() {
        int rowCount = 28;
        int columnCount = 6;

        DefaultTableSpans tableSpans = new DefaultTableSpans(rowCount, columnCount);

        tableSpans.addRow();

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            assertEquals(new SpanCounts(1, 1), tableSpans.getSpan(rowCount, columnIndex));
        }
    }

    @Test
    public void testInsertRowFirst() {
        testInsertRow(28, 0);
    }

    @Test
    public void testInsertRowMiddle() {
        int rowCount = 28;

        testInsertRow(rowCount, rowCount / 2);
    }

    @Test
    public void testInsertRowLast() {
        int rowCount = 28;

        testInsertRow(rowCount, rowCount);
    }

    private void testInsertRow(int rowCount, int newRowIndex) {
        int columnCount = 6;

        int[] columnIndices = new int[columnCount];
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            columnIndices[columnIndex] = columnIndex;
        }

        DefaultTableSpans tableSpans = new DefaultTableSpans(rowCount, columnCount);

        tableSpans.insertRow(newRowIndex);
        tableSpans.combine(new int[]{newRowIndex}, columnIndices);

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            if (rowIndex == newRowIndex) {
                assertEquals(new SpanCounts(1, columnCount), tableSpans.getSpan(newRowIndex, 0));
            } else {
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    assertEquals(new SpanCounts(1, 1), tableSpans.getSpan(rowIndex, columnIndex));
                }
            }
        }
    }
}

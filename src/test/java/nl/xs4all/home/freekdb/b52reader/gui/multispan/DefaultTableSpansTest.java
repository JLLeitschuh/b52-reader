/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.gui.multispan;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for the {@link DefaultTableSpans} class.
 */
public class DefaultTableSpansTest {
    @Test
    public void testAddColumn() {
        int rowCount = 28;
        int columnIndex = 6;

        DefaultTableSpans tableSpans = new DefaultTableSpans(rowCount, columnIndex);

        tableSpans.addColumn();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            assertArrayEquals(new int[]{1, 1}, tableSpans.getSpan(rowIndex, columnIndex));
        }
    }

    @Test
    public void testAddRow() {
        int rowIndex = 28;
        int columnCount = 6;

        DefaultTableSpans tableSpans = new DefaultTableSpans(rowIndex, columnCount);

        tableSpans.addRow();

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            assertArrayEquals(new int[]{1, 1}, tableSpans.getSpan(rowIndex, columnIndex));
        }
    }
}

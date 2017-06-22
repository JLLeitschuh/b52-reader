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
    public void testAddColumn() throws IllegalAccessException {
        DefaultTableSpans tableSpans = new DefaultTableSpans(28, 6);

        tableSpans.addColumn();

        assertArrayEquals(new int[]{1, 1}, tableSpans.getSpan(2, 6));
    }
}
